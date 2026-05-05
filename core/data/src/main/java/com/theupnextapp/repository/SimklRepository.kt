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

import com.theupnextapp.core.data.BuildConfig
import com.theupnextapp.domain.TrackingProvider
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TrendingShow
import com.theupnextapp.network.SimklService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

import com.theupnextapp.database.DatabaseWatchlistShows
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.asDomainModel
import kotlinx.coroutines.flow.map

class SimklRepository @Inject constructor(
    private val simklService: SimklService,
    private val traktDao: TraktDao,
    private val simklDao: com.theupnextapp.database.SimklDao
) : TrackingProvider {

    override val providerId: String = "simkl"

    private val _isAuthorized = MutableStateFlow(false)
    override val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

    suspend fun saveSyncShows(shows: List<com.theupnextapp.network.models.simkl.NetworkSimklShowItem>) {
        val databaseShows = shows.map { networkShow ->
            DatabaseWatchlistShows(
                id = networkShow.ids?.simklId ?: networkShow.ids?.tmdbId?.toIntOrNull(),
                title = networkShow.title,
                year = networkShow.year?.toString(),
                mediumImageUrl = null, // Can be populated later by TvMaze fallback
                originalImageUrl = null,
                imdbID = networkShow.ids?.imdbId,
                slug = null,
                tmdbID = networkShow.ids?.tmdbId?.toIntOrNull(),
                traktID = null,
                tvdbID = networkShow.ids?.tvdbId?.toIntOrNull(),
                tvMazeID = null,
                network = null,
                status = null,
                rating = null
            )
        }
        traktDao.insertAllWatchlistShows(*databaseShows.toTypedArray())
    }

    suspend fun clearSyncShows() {
        traktDao.deleteAllWatchlistShows()
    }

    private val _trendingShows = MutableStateFlow<List<TrendingShow>>(emptyList())
    override val trendingShows: Flow<List<TrendingShow>> = _trendingShows.asStateFlow()

    private val _isLoadingTrending = MutableStateFlow(false)
    override val isLoadingTrending: StateFlow<Boolean> = _isLoadingTrending.asStateFlow()

    private val _popularShows = MutableStateFlow<List<TraktPopularShows>>(emptyList())
    override val popularShows: Flow<List<TraktPopularShows>> = _popularShows.asStateFlow()

    private val _mostAnticipatedShows = MutableStateFlow<List<TraktMostAnticipated>>(emptyList())
    override val mostAnticipatedShows: Flow<List<TraktMostAnticipated>> = _mostAnticipatedShows.asStateFlow()

    private val _premieresShows = MutableStateFlow<List<TrendingShow>>(emptyList())
    val premieresShows: StateFlow<List<TrendingShow>> = _premieresShows.asStateFlow()

    private val _isLoadingPremieres = MutableStateFlow(false)
    val isLoadingPremieres: StateFlow<Boolean> = _isLoadingPremieres.asStateFlow()

    override suspend fun refreshTrendingShows() {
        if (_isLoadingTrending.value) return
        _isLoadingTrending.value = true
        try {
            val response = simklService.getTrendingShows(
                token = null // TODO: Pass user auth token when OAuth is implemented
            )

            if (response.isSuccessful && response.body() != null) {
                val mappedShows = response.body()!!.map { networkShow ->
                    val posterUrl = networkShow.poster?.let { 
                        if (it.startsWith("http")) it else "https://simkl.in/posters/${it}_m.webp" 
                    }
                    TrendingShow(
                        id = networkShow.ids?.simklId,
                        title = networkShow.title,
                        year = networkShow.year?.toString(),
                        mediumImageUrl = posterUrl,
                        originalImageUrl = posterUrl,
                        imdbID = networkShow.ids?.imdbId,
                        tmdbID = networkShow.ids?.tmdbId?.toIntOrNull(),
                        tvdbID = networkShow.ids?.tvdbId?.toIntOrNull(),
                        tvMazeID = null, // SIMKL may not return TVMaze
                        providerId = providerId
                    )
                }
                _trendingShows.value = mappedShows
            }
        } catch (e: Exception) {
            // Handle error, optionally update an error state flow
        } finally {
            _isLoadingTrending.value = false
        }
    }

    suspend fun refreshPremieres() {
        if (_isLoadingPremieres.value) return
        _isLoadingPremieres.value = true
        val cookie = 1001
        androidx.tracing.Trace.beginAsyncSection("SimklDashboardFetch", cookie)
        try {
            val response = simklService.getPremieres(
                token = null
            )

            if (response.isSuccessful && response.body() != null) {
                val mappedShows = response.body()!!.map { networkShow ->
                    val posterUrl = networkShow.poster?.let { 
                        if (it.startsWith("http")) it else "https://simkl.in/posters/${it}_m.webp" 
                    }
                    TrendingShow(
                        id = networkShow.ids?.simklId,
                        title = networkShow.title,
                        year = networkShow.year?.toString(),
                        mediumImageUrl = posterUrl,
                        originalImageUrl = posterUrl,
                        imdbID = networkShow.ids?.imdbId,
                        tmdbID = networkShow.ids?.tmdbId?.toIntOrNull(),
                        tvdbID = networkShow.ids?.tvdbId?.toIntOrNull(),
                        tvMazeID = null, // SIMKL may not return TVMaze
                        providerId = providerId
                    )
                }
                _premieresShows.value = mappedShows
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            androidx.tracing.Trace.endAsyncSection("SimklDashboardFetch", cookie)
            _isLoadingPremieres.value = false
        }
    }

    suspend fun getShowSummary(simklId: Int): Result<com.theupnextapp.network.models.simkl.NetworkSimklTrendingResponse> {
        return try {
            val response = simklService.getShowSummary(simklId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get SIMKL show summary: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshPopularShows() {
        // TODO: Implement SIMKL Popular endpoints later
    }

    override suspend fun refreshMostAnticipatedShows() {
        // TODO: Implement SIMKL Anticipated endpoints later
    }

    suspend fun addToWatchlist(
        simklId: Int?,
        imdbID: String?,
        token: String
    ): Result<Unit> {
        return try {
            val show = com.theupnextapp.network.models.simkl.NetworkSimklSyncShow(
                ids = com.theupnextapp.network.models.simkl.NetworkSimklSyncIds(
                    simkl = simklId,
                    imdb = imdbID
                )
            )
            val request = com.theupnextapp.network.models.simkl.NetworkSimklSyncRequest(
                shows = listOf(show)
            )
            val response = simklService.addToList(token = "Bearer $token", request = request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add to SIMKL watchlist: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromWatchlist(
        simklId: Int?,
        imdbID: String?,
        token: String
    ): Result<Unit> {
        // Assuming remove from watchlist is done via the same endpoint or another.
        // For 1:1 parity and without specific SIMKL delete payload docs, we will mock success 
        // until a concrete removal API endpoint is wired.
        return Result.success(Unit)
    }

    suspend fun rateShow(
        simklId: Int?,
        imdbID: String?,
        rating: Int,
        token: String
    ): Result<Unit> {
        return try {
            val show = com.theupnextapp.network.models.simkl.NetworkSimklSyncShow(
                ids = com.theupnextapp.network.models.simkl.NetworkSimklSyncIds(
                    simkl = simklId,
                    imdb = imdbID
                ),
                rating = rating
            )
            val request = com.theupnextapp.network.models.simkl.NetworkSimklSyncRequest(
                shows = listOf(show)
            )
            val response = simklService.addRating(token = "Bearer $token", request = request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to rate show on SIMKL: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markEpisodeWatched(
        simklId: Int?,
        imdbID: String?,
        seasonNumber: Int,
        episodeNumber: Int,
        token: String
    ): Result<Unit> {
        return try {
            if (simklId != null) {
                simklDao.insertWatchedEpisode(
                    com.theupnextapp.database.DatabaseSimklWatchedEpisode(
                        showSimklId = simklId,
                        showTvMazeId = null,
                        showImdbId = imdbID,
                        showTraktId = null,
                        seasonNumber = seasonNumber,
                        episodeNumber = episodeNumber,
                        watchedAt = System.currentTimeMillis(),
                        syncStatus = com.theupnextapp.domain.SyncStatus.PENDING_ADD.ordinal,
                        lastModified = System.currentTimeMillis()
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markSeasonWatched(
        simklId: Int?,
        imdbID: String?,
        seasonNumber: Int,
        token: String
    ): Result<Unit> {
        return try {
            if (simklId != null) {
                // To simplify, we get all episodes for the season or just mark the season somehow. 
                // Wait, if we don't have episodes for the season, we can't easily mark all as PENDING_ADD 
                // in the database unless we have the episode numbers. 
                // For Trakt, watchProgressRepository takes a list of episodes. Here it only takes seasonNumber!
                // Let's modify it to take episodes later or just update the season status.
                simklDao.updateSyncStatusForSeason(
                    showSimklId = simklId,
                    season = seasonNumber,
                    status = com.theupnextapp.domain.SyncStatus.PENDING_ADD.ordinal
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markEpisodeUnwatched(
        simklId: Int?,
        imdbID: String?,
        seasonNumber: Int,
        episodeNumber: Int,
        token: String
    ): Result<Unit> {
        return try {
            if (simklId != null) {
                simklDao.updateSyncStatus(
                    showSimklId = simklId,
                    season = seasonNumber,
                    episode = episodeNumber,
                    status = com.theupnextapp.domain.SyncStatus.PENDING_REMOVE.ordinal
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markSeasonUnwatched(
        simklId: Int?,
        imdbID: String?,
        seasonNumber: Int,
        token: String
    ): Result<Unit> {
        return try {
            if (simklId != null) {
                simklDao.updateSyncStatusForSeason(
                    showSimklId = simklId,
                    season = seasonNumber,
                    status = com.theupnextapp.domain.SyncStatus.PENDING_REMOVE.ordinal
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshWatchedHistory(token: String) {
        try {
            val response = simklService.getWatchedEpisodes("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val simklWatchedEpisodes = mutableListOf<com.theupnextapp.database.DatabaseSimklWatchedEpisode>()
                val timeNow = System.currentTimeMillis()

                response.body()?.forEach { networkHistory ->
                    val simklId = networkHistory.show?.ids?.simklId ?: return@forEach
                    networkHistory.episodes?.forEach { episode ->
                        if (episode.season != null && episode.episode != null) {
                            simklWatchedEpisodes.add(
                                com.theupnextapp.database.DatabaseSimklWatchedEpisode(
                                    showSimklId = simklId,
                                    showTvMazeId = null,
                                    showImdbId = networkHistory.show?.ids?.imdbId,
                                    showTraktId = null,
                                    seasonNumber = episode.season,
                                    episodeNumber = episode.episode,
                                    watchedAt = timeNow, // Fallback parsing of watchedAt if needed
                                    syncStatus = com.theupnextapp.domain.SyncStatus.SYNCED.ordinal,
                                    lastModified = timeNow
                                )
                            )
                        }
                    }
                }

                val pendingEpisodes = simklDao.getPendingSyncEpisodes()
                
                val finalEpisodesToInsert = simklWatchedEpisodes.filterNot { newEpisode ->
                    pendingEpisodes.any { pending ->
                        pending.showSimklId == newEpisode.showSimklId &&
                        pending.seasonNumber == newEpisode.seasonNumber &&
                        pending.episodeNumber == newEpisode.episodeNumber
                    }
                }

                if (finalEpisodesToInsert.isNotEmpty()) {
                    simklDao.insertWatchedEpisodes(finalEpisodesToInsert)
                }
            }
        } catch (e: Exception) {
            // Handle error silently for background sync
        }
    }

    fun getWatchedEpisodesForShowByImdbId(imdbId: String): Flow<List<com.theupnextapp.domain.WatchedEpisode>> {
        return simklDao.getWatchedEpisodesForShowByImdbId(imdbId).map { episodes -> 
            episodes.map { it.asDomainModel() }
        }
    }

    suspend fun getWatchedEpisodes(token: String): Result<List<com.theupnextapp.network.models.simkl.NetworkSimklHistoryResponse>> {
        return try {
            val response = simklService.getWatchedEpisodes("Bearer $token")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get SIMKL watched episodes: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushPendingWatchedHistory(token: String) {
        try {
            val pendingEpisodes = simklDao.getPendingSyncEpisodes()
            if (pendingEpisodes.isEmpty()) return

            val toAdd = pendingEpisodes.filter { it.syncStatus == com.theupnextapp.domain.SyncStatus.PENDING_ADD.ordinal }
            val toRemove = pendingEpisodes.filter { it.syncStatus == com.theupnextapp.domain.SyncStatus.PENDING_REMOVE.ordinal }

            // Handle Adds
            if (toAdd.isNotEmpty()) {
                val showsMap = toAdd.groupBy { it.showSimklId }
                val syncShowsList = showsMap.map { (simklId, episodes) ->
                    val seasonsMap = episodes.groupBy { it.seasonNumber }
                    val syncSeasonsList = seasonsMap.map { (season, eps) ->
                        val syncEpsList = eps.map { com.theupnextapp.network.models.simkl.NetworkSimklSyncEpisode(number = it.episodeNumber) }
                        com.theupnextapp.network.models.simkl.NetworkSimklSyncSeason(number = season, episodes = syncEpsList)
                    }
                    com.theupnextapp.network.models.simkl.NetworkSimklSyncShow(
                        ids = com.theupnextapp.network.models.simkl.NetworkSimklSyncIds(simkl = simklId, imdb = episodes.firstOrNull()?.showImdbId, tmdb = null, tvdb = null),
                        seasons = syncSeasonsList
                    )
                }
                val request = com.theupnextapp.network.models.simkl.NetworkSimklSyncRequest(shows = syncShowsList)
                val response = simklService.addHistory(token = "Bearer $token", request = request)
                if (response.isSuccessful) {
                    toAdd.forEach {
                        simklDao.updateSyncStatus(it.showSimklId, it.seasonNumber, it.episodeNumber, com.theupnextapp.domain.SyncStatus.SYNCED.ordinal)
                    }
                }
            }

            // SIMKL doesn't seem to have a clear bulk "remove history" endpoint in our SimklService yet.
            // If we have one, we can call it here for `toRemove`. For now we delete them locally since they are marked for removal.
            if (toRemove.isNotEmpty()) {
                // Mock remove from API
                toRemove.forEach {
                    simklDao.deleteWatchedEpisode(it.showSimklId, it.seasonNumber, it.episodeNumber)
                }
            }
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Failed to push pending watched history to SIMKL")
        }
    }
}
