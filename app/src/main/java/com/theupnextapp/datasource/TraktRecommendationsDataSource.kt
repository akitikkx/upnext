/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
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

package com.theupnextapp.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.database.DatabaseTraktMostAnticipated
import com.theupnextapp.database.DatabaseTraktPopularShows
import com.theupnextapp.database.DatabaseTraktTrendingShows
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.domain.TraktCast
import com.theupnextapp.domain.TraktRelatedShows
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.domain.TraktShowStats
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.asDatabaseModel
import com.theupnextapp.network.models.trakt.NetworkTraktCast
import com.theupnextapp.network.models.trakt.NetworkTraktMostAnticipatedResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktPersonResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonShowCreditsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktTrendingShowsResponseItem
import com.theupnextapp.network.models.trakt.asDatabaseModel
import com.theupnextapp.network.models.trakt.asDomainModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

open class TraktRecommendationsDataSource
@Inject
constructor(
    private val traktDao: TraktDao,
    private val traktService: TraktService,
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    firebaseCrashlytics: FirebaseCrashlytics,
) : BaseTraktDataSource(upnextDao, tvMazeService, firebaseCrashlytics) {
    suspend fun refreshTraktTrendingShows(forceRefresh: Boolean): Result<Unit> {
        val tableName = DatabaseTables.TABLE_TRAKT_TRENDING.tableName
        val isEmpty = traktDao.checkIfTrendingShowsIsEmpty()
        val needsUpdate = isEmpty || isUpdateNeededByDay(tableName) || forceRefresh

        if (!needsUpdate) {
            return Result.success(Unit)
        }

        return withContext(Dispatchers.IO) {
            try {
                val networkTrendingShowsResponse = traktService.getTrendingShowsAsync().await()
                if (networkTrendingShowsResponse.isEmpty()) {
                    traktDao.clearTrendingShows()
                    logTableUpdateTimestamp(tableName)
                    return@withContext Result.success(Unit)
                }

                val newShowDatabaseModels: List<DatabaseTraktTrendingShows> =
                    networkTrendingShowsResponse.mapNotNull { responseItem: NetworkTraktTrendingShowsResponseItem ->
                        responseItem.show.asDatabaseModel()
                    }

                if (newShowDatabaseModels.isEmpty()) {
                    return@withContext Result.failure(Exception("Failed to map network items to database models"))
                }

                val networkShowIds = newShowDatabaseModels.map { it.id }.toSet()
                val localShowsList = traktDao.getTraktTrendingRaw()
                val localShowsMap = localShowsList.associateBy { it.id }

                val showsToUpsert = mutableListOf<DatabaseTraktTrendingShows>()
                val showsMissingImages = mutableListOf<DatabaseTraktTrendingShows>()

                for (networkShowModel in newShowDatabaseModels) {
                    val existingLocalShow = localShowsMap[networkShowModel.id]

                    if (existingLocalShow != null) {
                        val updatedShow =
                            existingLocalShow.copy(
                                title = networkShowModel.title,
                                year = networkShowModel.year,
                                slug = networkShowModel.slug,
                                tmdbID = networkShowModel.tmdbID,
                                traktID = networkShowModel.traktID,
                                tvdbID = networkShowModel.tvdbID,
                            )
                        showsToUpsert.add(updatedShow)

                        if (existingLocalShow.original_image_url.isNullOrEmpty() ||
                            existingLocalShow.medium_image_url.isNullOrEmpty() ||
                            forceRefresh
                        ) {
                            showsMissingImages.add(updatedShow)
                        }
                    } else {
                        showsToUpsert.add(networkShowModel)
                        showsMissingImages.add(networkShowModel)
                    }
                }

                if (showsToUpsert.isNotEmpty()) {
                    traktDao.insertAllTraktTrending(*showsToUpsert.toTypedArray())
                }

                val showsToDelete = localShowsList.filter { it.id !in networkShowIds }
                if (showsToDelete.isNotEmpty()) {
                    traktDao.deleteSpecificTrendingShows(showsToDelete.map { it.id })
                }

                if (showsMissingImages.isNotEmpty()) {
                    val showsToUpdateWithFetchedImages = mutableListOf<DatabaseTraktTrendingShows>()
                    for (showNeedingImage in showsMissingImages) {
                        showNeedingImage.imdbID?.let { imdbId ->
                            val (newTvMazeId, newPoster, newHeroImage) = getImages(imdbId)
                            var imageUpdatedShow = showNeedingImage
                            var changed = false

                            if (newPoster != null && newPoster != imageUpdatedShow.original_image_url) {
                                imageUpdatedShow = imageUpdatedShow.copy(original_image_url = newPoster)
                                changed = true
                            }
                            if (newHeroImage != null && newHeroImage != imageUpdatedShow.medium_image_url) {
                                imageUpdatedShow = imageUpdatedShow.copy(medium_image_url = newHeroImage)
                                changed = true
                            }
                            if (newTvMazeId != null && newTvMazeId != imageUpdatedShow.tvMazeID) {
                                imageUpdatedShow = imageUpdatedShow.copy(tvMazeID = newTvMazeId)
                                changed = true
                            }

                            if (changed) {
                                showsToUpdateWithFetchedImages.add(imageUpdatedShow)
                            }
                        }
                    }

                    if (showsToUpdateWithFetchedImages.isNotEmpty()) {
                        traktDao.insertAllTraktTrending(*showsToUpdateWithFetchedImages.toTypedArray())
                    }
                }

                logTableUpdateTimestamp(tableName)
                Result.success(Unit)
            } catch (e: Exception) {
                logTraktException("refreshTraktTrendingShows failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun refreshTraktPopularShows(forceRefresh: Boolean): Result<Unit> {
        val tableName = DatabaseTables.TABLE_TRAKT_POPULAR.tableName
        val isEmpty = traktDao.checkIfPopularShowsIsEmpty()
        val needsUpdate = isEmpty || isUpdateNeededByDay(tableName) || forceRefresh

        if (!needsUpdate) {
            return Result.success(Unit)
        }

        return withContext(Dispatchers.IO) {
            try {
                val networkPopularShows = traktService.getPopularShowsAsync().await()
                if (networkPopularShows.isEmpty()) {
                    traktDao.clearPopularShows()
                    logTableUpdateTimestamp(tableName)
                    return@withContext Result.success(Unit)
                }

                val networkShowIds = networkPopularShows.map { it.ids.trakt }.toSet()
                val newShowDatabaseModels = networkPopularShows.map { it.asDatabaseModel() }

                val localShowsList = traktDao.getTraktPopularRaw()
                val localShowsMap = localShowsList.associateBy { it.id }

                val showsToUpsert = mutableListOf<DatabaseTraktPopularShows>()
                val showsMissingImages = mutableListOf<DatabaseTraktPopularShows>()

                for (networkShowModel in newShowDatabaseModels) {
                    val existingLocalShow = localShowsMap[networkShowModel.id]

                    if (existingLocalShow != null) {
                        val updatedShow =
                            existingLocalShow.copy(
                                title = networkShowModel.title,
                                year = networkShowModel.year,
                                slug = networkShowModel.slug,
                                tmdbID = networkShowModel.tmdbID,
                                traktID = networkShowModel.traktID,
                                tvdbID = networkShowModel.tvdbID,
                            )
                        showsToUpsert.add(updatedShow)

                        if (existingLocalShow.original_image_url.isNullOrEmpty() ||
                            existingLocalShow.medium_image_url.isNullOrEmpty() ||
                            forceRefresh
                        ) {
                            showsMissingImages.add(updatedShow)
                        }
                    } else {
                        showsToUpsert.add(networkShowModel)
                        showsMissingImages.add(networkShowModel)
                    }
                }

                if (showsToUpsert.isNotEmpty()) {
                    traktDao.insertAllTraktPopular(*showsToUpsert.toTypedArray())
                }

                val showsToDelete = localShowsList.filter { it.id !in networkShowIds }
                if (showsToDelete.isNotEmpty()) {
                    traktDao.deleteSpecificPopularShows(showsToDelete.map { it.id })
                }

                if (showsMissingImages.isNotEmpty()) {
                    val showsToUpdateWithFetchedImages = mutableListOf<DatabaseTraktPopularShows>()
                    for (showNeedingImage in showsMissingImages) {
                        showNeedingImage.imdbID?.let { imdbId ->
                            val (newTvMazeId, newPoster, newHeroImage) = getImages(imdbId)
                            var imageUpdatedShow = showNeedingImage
                            var changed = false

                            if (newPoster != null && newPoster != imageUpdatedShow.original_image_url) {
                                imageUpdatedShow = imageUpdatedShow.copy(original_image_url = newPoster)
                                changed = true
                            }
                            if (newHeroImage != null && newHeroImage != imageUpdatedShow.medium_image_url) {
                                imageUpdatedShow = imageUpdatedShow.copy(medium_image_url = newHeroImage)
                                changed = true
                            }
                            if (newTvMazeId != null && newTvMazeId != imageUpdatedShow.tvMazeID) {
                                imageUpdatedShow = imageUpdatedShow.copy(tvMazeID = newTvMazeId)
                                changed = true
                            }

                            if (changed) {
                                showsToUpdateWithFetchedImages.add(imageUpdatedShow)
                            }
                        }
                    }

                    if (showsToUpdateWithFetchedImages.isNotEmpty()) {
                        traktDao.insertAllTraktPopular(*showsToUpdateWithFetchedImages.toTypedArray())
                    }
                }

                logTableUpdateTimestamp(tableName)
                Result.success(Unit)
            } catch (e: Exception) {
                logTraktException("refreshTraktPopularShows failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun refreshTraktMostAnticipatedShows(forceRefresh: Boolean): Result<Unit> {
        val tableName = DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName
        val isEmpty = traktDao.checkIfMostAnticipatedShowsIsEmpty()
        val needsUpdate = isEmpty || isUpdateNeededByDay(tableName) || forceRefresh

        if (!needsUpdate) {
            return Result.success(Unit)
        }

        return withContext(Dispatchers.IO) {
            try {
                val networkResponseItems: List<NetworkTraktMostAnticipatedResponseItem> =
                    traktService.getMostAnticipatedShowsAsync().await()

                if (networkResponseItems.isEmpty()) {
                    traktDao.clearMostAnticipatedShows()
                    logTableUpdateTimestamp(tableName)
                    return@withContext Result.success(Unit)
                }

                val newShowDatabaseModels: List<DatabaseTraktMostAnticipated> =
                    networkResponseItems.map { networkItem: NetworkTraktMostAnticipatedResponseItem ->
                        networkItem.show.asDatabaseModel()
                    }

                if (newShowDatabaseModels.isEmpty()) {
                    return@withContext Result.failure(
                        Exception("Failed to map anticipated shows to database models")
                    )
                }

                val networkShowIds = newShowDatabaseModels.map { it.id }.toSet()

                val localShowsList = traktDao.getTraktMostAnticipatedRaw()
                val localShowsMap = localShowsList.associateBy { it.id }

                val showsToUpsert = mutableListOf<DatabaseTraktMostAnticipated>()
                val showsMissingImages = mutableListOf<DatabaseTraktMostAnticipated>()

                for (networkShowModel in newShowDatabaseModels) {
                    val existingLocalShow = localShowsMap[networkShowModel.id]

                    if (existingLocalShow != null) {
                        val updatedShow =
                            existingLocalShow.copy(
                                title = networkShowModel.title,
                                year = networkShowModel.year,
                                slug = networkShowModel.slug,
                                tmdbID = networkShowModel.tmdbID,
                                traktID = networkShowModel.traktID,
                                tvdbID = networkShowModel.tvdbID,
                                imdbID = networkShowModel.imdbID,
                            )
                        showsToUpsert.add(updatedShow)

                        if (existingLocalShow.original_image_url.isNullOrEmpty() ||
                            existingLocalShow.medium_image_url.isNullOrEmpty() ||
                            existingLocalShow.tvMazeID == null ||
                            forceRefresh
                        ) {
                            showsMissingImages.add(updatedShow)
                        }
                    } else {
                        showsToUpsert.add(networkShowModel)
                        showsMissingImages.add(networkShowModel)
                    }
                }

                if (showsToUpsert.isNotEmpty()) {
                    traktDao.insertAllTraktMostAnticipated(*showsToUpsert.toTypedArray())
                }

                val showsToDelete = localShowsList.filter { it.id !in networkShowIds }
                if (showsToDelete.isNotEmpty()) {
                    traktDao.deleteSpecificMostAnticipatedShows(showsToDelete.map { it.id })
                }

                if (showsMissingImages.isNotEmpty()) {
                    val showsToUpdateWithFetchedImages = mutableListOf<DatabaseTraktMostAnticipated>()
                    for (showNeedingImage in showsMissingImages) {
                        showNeedingImage.imdbID?.let { imdbId ->
                            val (newTvMazeId, newPoster, newHeroImage) = getImages(imdbId)
                            var imageUpdatedShow = showNeedingImage
                            var changed = false

                            if (newPoster != null && newPoster != imageUpdatedShow.original_image_url) {
                                imageUpdatedShow = imageUpdatedShow.copy(original_image_url = newPoster)
                                changed = true
                            }
                            if (newHeroImage != null && newHeroImage != imageUpdatedShow.medium_image_url) {
                                imageUpdatedShow = imageUpdatedShow.copy(medium_image_url = newHeroImage)
                                changed = true
                            }
                            if (newTvMazeId != null && newTvMazeId != imageUpdatedShow.tvMazeID) {
                                imageUpdatedShow = imageUpdatedShow.copy(tvMazeID = newTvMazeId)
                                changed = true
                            }

                            if (changed) {
                                showsToUpdateWithFetchedImages.add(imageUpdatedShow)
                            }
                        }
                    }

                    if (showsToUpdateWithFetchedImages.isNotEmpty()) {
                        traktDao.insertAllTraktMostAnticipated(*showsToUpdateWithFetchedImages.toTypedArray())
                    }
                }

                logTableUpdateTimestamp(tableName)
                Result.success(Unit)
            } catch (e: Exception) {
                logTraktException("refreshTraktMostAnticipatedShows failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getTraktShowRating(imdbID: String): Result<TraktShowRating> {
        return safeApiCall {
            val networkResponse = traktService.getShowRatingsAsync(imdbID).await()
            TraktShowRating(
                rating = networkResponse.rating,
                votes = networkResponse.votes,
                distribution = networkResponse.distribution,
            )
        }
    }

    suspend fun getTraktShowStats(imdbID: String): Result<TraktShowStats> {
        return safeApiCall {
            val networkResponse = traktService.getShowStatsAsync(imdbID).await()
            TraktShowStats(
                watchers = networkResponse.watchers,
                plays = networkResponse.plays,
                collectors = networkResponse.collectors,
                collected_episodes = networkResponse.collected_episodes,
                comments = networkResponse.comments,
                lists = networkResponse.lists,
                votes = networkResponse.votes,
            )
        }
    }

    suspend fun getTraktIdFromImdbId(imdbID: String): Result<Int?> {
        return safeApiCall {
            val networkResponse = traktService.idLookupAsync("imdb", imdbID).await()
            networkResponse.firstOrNull()?.show?.ids?.trakt
        }
    }

    suspend fun getPersonSummary(id: String): Result<NetworkTraktPersonResponse> {
        return safeApiCall {
            traktService.getPersonSummaryAsync(id).await()
        }
    }

    suspend fun getPersonShowCredits(id: String): Result<NetworkTraktPersonShowCreditsResponse> {
        return safeApiCall {
            traktService.getPersonShowCreditsAsync(id).await()
        }
    }

    suspend fun getTraktPersonIdFromTvMazeId(tvMazeId: String): Result<Int?> {
        return safeApiCall {
            val networkResponse = traktService.idLookupAsync("tvmaze", tvMazeId, type = "person").await()
            networkResponse.firstOrNull()?.person?.ids?.trakt
        }
    }
    suspend fun getTraktPersonIdFromSearch(name: String): Result<Int?> {
        return safeApiCall {
            val networkResponse = traktService.searchPeopleAsync(name).await()
            networkResponse.firstOrNull()?.person?.ids?.trakt
        }
    }

    suspend fun getShowCast(imdbID: String): Result<List<TraktCast>> {
        return safeApiCall {
            kotlinx.coroutines.coroutineScope {
                val traktRequest = async { traktService.getShowPeopleAsync(imdbID).await() }

                val tvMazeImagesRequest: kotlinx.coroutines.Deferred<Map<String, Pair<String?, String?>>> = async {
                    try {
                        val lookup = tvMazeService.getShowLookupAsync(imdbID).await()
                        val tvMazeId = lookup.id
                        if (tvMazeId != null) {
                            val tvMazeCast = tvMazeService.getShowCastAsync(tvMazeId.toString()).await()
                            tvMazeCast.associate { castItem ->
                                (castItem.person?.name ?: "") to (castItem.person?.image?.original to castItem.person?.image?.medium)
                            }
                        } else {
                            emptyMap<String, Pair<String?, String?>>()
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to fetch TvMaze images for cast")
                        emptyMap<String, Pair<String?, String?>>()
                    }
                }

                val networkResponse = traktRequest.await()
                val imagesMap = tvMazeImagesRequest.await()

                networkResponse.cast?.map { castMember: NetworkTraktCast ->
                    val personName = castMember.person?.name
                    val (originalImage, mediumImage) = imagesMap[personName] ?: (null to null)

                    TraktCast(
                        character = castMember.characters?.firstOrNull() ?: "",
                        name = personName,
                        originalImageUrl = originalImage,
                        mediumImageUrl = mediumImage,
                        traktId = castMember.person?.ids?.trakt,
                        imdbId = castMember.person?.ids?.imdb,
                        slug = castMember.person?.ids?.slug
                    )
                } ?: emptyList()
            }
        }
    }

    suspend fun getRelatedShows(imdbID: String): Result<List<TraktRelatedShows>> {
        return safeApiCall {
            kotlinx.coroutines.coroutineScope {
                val networkResponse = traktService.getRelatedShowsAsync(imdbID).await()
                networkResponse.map { item ->
                    async {
                        item.ids.imdb?.let { imdbId ->
                            val (_, original, medium) = getImages(imdbId)
                            item.mediumImageUrl = medium
                            item.originalImageUrl = original
                        }
                        item
                    }
                }.awaitAll()
                    .filter { !it.mediumImageUrl.isNullOrEmpty() || !it.originalImageUrl.isNullOrEmpty() }
                    .map { it.asDomainModel() }
            }
        }
    }
}
