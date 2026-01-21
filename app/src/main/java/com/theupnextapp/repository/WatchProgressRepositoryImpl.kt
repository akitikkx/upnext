/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.repository

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.DatabaseWatchedEpisode
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.domain.ShowWatchProgress
import com.theupnextapp.domain.SyncStatus
import com.theupnextapp.domain.WatchedEpisode
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.models.trakt.NetworkTraktSyncHistoryEpisode
import com.theupnextapp.network.models.trakt.NetworkTraktSyncHistoryRequest
import com.theupnextapp.network.models.trakt.NetworkTraktSyncHistorySeason
import com.theupnextapp.network.models.trakt.NetworkTraktSyncHistoryShow
import com.theupnextapp.network.models.trakt.NetworkTraktSyncHistoryShowIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class WatchProgressRepositoryImpl(
    private val traktDao: TraktDao,
    private val traktService: TraktService,
    private val firebaseCrashlytics: FirebaseCrashlytics,
) : WatchProgressRepository {
    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    override val syncError: StateFlow<String?> = _syncError.asStateFlow()

    override suspend fun markEpisodeWatched(
        showTraktId: Int,
        showTvMazeId: Int?,
        showImdbId: String?,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val episode =
                    DatabaseWatchedEpisode(
                        showTraktId = showTraktId,
                        showTvMazeId = showTvMazeId,
                        showImdbId = showImdbId,
                        seasonNumber = seasonNumber,
                        episodeNumber = episodeNumber,
                        episodeTraktId = null,
                        watchedAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.PENDING_ADD.ordinal,
                        lastModified = System.currentTimeMillis(),
                    )
                traktDao.insertWatchedEpisode(episode)
                Timber.d("Marked episode as watched: S${seasonNumber}E$episodeNumber for show $showTraktId")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark episode as watched")
                firebaseCrashlytics.recordException(e)
                Result.failure(e)
            }
        }

    override suspend fun markEpisodeUnwatched(
        showTraktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val existingEpisode =
                    traktDao.getWatchedEpisode(
                        showTraktId,
                        seasonNumber,
                        episodeNumber,
                    )

                if (existingEpisode != null) {
                    if (existingEpisode.syncStatus == SyncStatus.SYNCED.ordinal) {
                        traktDao.updateSyncStatus(
                            showTraktId,
                            seasonNumber,
                            episodeNumber,
                            SyncStatus.PENDING_REMOVE.ordinal,
                        )
                    } else {
                        traktDao.deleteWatchedEpisode(showTraktId, seasonNumber, episodeNumber)
                    }
                }
                Timber.d("Marked episode as unwatched: S${seasonNumber}E$episodeNumber for show $showTraktId")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark episode as unwatched")
                firebaseCrashlytics.recordException(e)
                Result.failure(e)
            }
        }

    override fun getWatchedEpisodesForShow(showTraktId: Int): Flow<List<WatchedEpisode>> =
        traktDao
            .getWatchedEpisodesForShow(showTraktId)
            .map { episodes ->
                episodes
                    .filter { it.syncStatus != SyncStatus.PENDING_REMOVE.ordinal }
                    .asDomainModel()
            }.flowOn(Dispatchers.IO)

    override suspend fun isEpisodeWatched(
        showTraktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val episode = traktDao.getWatchedEpisode(showTraktId, seasonNumber, episodeNumber)
            episode != null && episode.syncStatus != SyncStatus.PENDING_REMOVE.ordinal
        }

    override suspend fun getShowWatchProgress(
        showTraktId: Int,
        totalEpisodes: Int,
    ): ShowWatchProgress =
        withContext(Dispatchers.IO) {
            val watchedCount = traktDao.getWatchedCountForShow(showTraktId)
            ShowWatchProgress(
                showTraktId = showTraktId,
                watchedCount = watchedCount,
                totalCount = totalEpisodes,
            )
        }

    override suspend fun syncWithTrakt(token: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            _isSyncing.value = true
            _syncError.value = null

            try {
                val pendingEpisodes = traktDao.getPendingSyncEpisodes()

                val pendingAdd =
                    pendingEpisodes.filter {
                        it.syncStatus == SyncStatus.PENDING_ADD.ordinal
                    }
                val pendingRemove =
                    pendingEpisodes.filter {
                        it.syncStatus == SyncStatus.PENDING_REMOVE.ordinal
                    }

                if (pendingAdd.isNotEmpty()) {
                    val request = buildSyncRequest(pendingAdd)
                    val response =
                        traktService
                            .addToHistoryAsync("Bearer $token", request)
                            .await()

                    Timber.d("Trakt sync add response: added ${response.added?.episodes} episodes")

                    pendingAdd.forEach {
                        traktDao.updateSyncStatus(
                            it.showTraktId,
                            it.seasonNumber,
                            it.episodeNumber,
                            SyncStatus.SYNCED.ordinal,
                        )
                    }
                }

                if (pendingRemove.isNotEmpty()) {
                    val request = buildSyncRequest(pendingRemove)
                    val response =
                        traktService
                            .removeFromHistoryAsync("Bearer $token", request)
                            .await()

                    Timber.d("Trakt sync remove response: deleted ${response.deleted?.episodes} episodes")

                    pendingRemove.forEach {
                        traktDao.confirmRemoval(
                            it.showTraktId,
                            it.seasonNumber,
                            it.episodeNumber,
                        )
                    }
                }

                _isSyncing.value = false
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync with Trakt")
                firebaseCrashlytics.recordException(e)
                _isSyncing.value = false
                _syncError.value = e.message
                Result.failure(e)
            }
        }

    override suspend fun refreshWatchedFromTrakt(
        token: String,
        showTraktId: Int?,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            _isSyncing.value = true
            _syncError.value = null

            try {
                val watchedShows =
                    traktService
                        .getWatchedShowsAsync("Bearer $token")
                        .await()

                val episodesToInsert = mutableListOf<DatabaseWatchedEpisode>()

                watchedShows.forEach { showResponse ->
                    val traktId = showResponse.show?.ids?.trakt ?: return@forEach

                    if (showTraktId != null && traktId != showTraktId) {
                        return@forEach
                    }

                    showResponse.seasons?.forEach { season ->
                        season.episodes?.forEach { episode ->
                            val watchedAt =
                                parseIso8601Date(episode.lastWatchedAt)
                                    ?: System.currentTimeMillis()

                            episodesToInsert.add(
                                DatabaseWatchedEpisode(
                                    showTraktId = traktId,
                                    showTvMazeId = null,
                                    showImdbId = showResponse.show?.ids?.imdb,
                                    seasonNumber = season.number ?: 0,
                                    episodeNumber = episode.number ?: 0,
                                    episodeTraktId = null,
                                    watchedAt = watchedAt,
                                    syncStatus = SyncStatus.SYNCED.ordinal,
                                    lastModified = System.currentTimeMillis(),
                                ),
                            )
                        }
                    }
                }

                if (episodesToInsert.isNotEmpty()) {
                    traktDao.insertWatchedEpisodes(episodesToInsert)
                    Timber.d("Refreshed ${episodesToInsert.size} watched episodes from Trakt")
                }

                _isSyncing.value = false
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh watched episodes from Trakt")
                firebaseCrashlytics.recordException(e)
                _isSyncing.value = false
                _syncError.value = e.message
                Result.failure(e)
            }
        }

    private fun buildSyncRequest(episodes: List<DatabaseWatchedEpisode>): NetworkTraktSyncHistoryRequest {
        val showsMap = episodes.groupBy { it.showTraktId }

        val shows =
            showsMap.map { (showTraktId, showEpisodes) ->
                val seasonsMap = showEpisodes.groupBy { it.seasonNumber }

                NetworkTraktSyncHistoryShow(
                    ids = NetworkTraktSyncHistoryShowIds(trakt = showTraktId),
                    seasons =
                        seasonsMap.map { (seasonNum, seasonEps) ->
                            NetworkTraktSyncHistorySeason(
                                number = seasonNum,
                                episodes =
                                    seasonEps.map { ep ->
                                        NetworkTraktSyncHistoryEpisode(
                                            number = ep.episodeNumber,
                                            watchedAt = toIso8601(ep.watchedAt),
                                        )
                                    },
                            )
                        },
                )
            }

        return NetworkTraktSyncHistoryRequest(shows)
    }

    private fun toIso8601(timestamp: Long): String {
        val sdf =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        return sdf.format(Date(timestamp))
    }

    private fun parseIso8601Date(dateString: String?): Long? {
        if (dateString.isNullOrEmpty()) return null

        return try {
            val sdf =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            sdf.parse(dateString)?.time
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse date: $dateString")
            null
        }
    }
}
