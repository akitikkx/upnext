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
import com.squareup.moshi.Moshi
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.database.DatabaseWatchlistShows
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.TraktCheckInStatus
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.trakt.NetworkTraktAddShowToListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAddShowToListRequestShow
import com.theupnextapp.network.models.trakt.NetworkTraktAddShowToListRequestShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInRequest
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInRequestEpisode
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInRequestShow
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInRequestShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktCreateCustomListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRatingRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRatingShow
import com.theupnextapp.network.models.trakt.NetworkTraktRatingShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequestShow
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequestShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktUserListItemResponse
import com.theupnextapp.network.models.trakt.TraktConflictErrorResponse
import com.theupnextapp.network.models.trakt.TraktErrorResponse
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktShowProgressResponse
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowInfo
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedSeason
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedEpisode
import com.theupnextapp.network.models.trakt.asDatabaseModel
import com.theupnextapp.network.models.trakt.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.nio.charset.Charset
import javax.inject.Inject

open class TraktAccountDataSource
@Inject
constructor(
    private val traktDao: TraktDao,
    private val traktService: TraktService,
    private val moshi: Moshi,
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    firebaseCrashlytics: FirebaseCrashlytics,
) : BaseTraktDataSource(upnextDao, tvMazeService, firebaseCrashlytics) {
    private val traktConflictErrorAdapter = moshi.adapter(TraktConflictErrorResponse::class.java)

    suspend fun refreshWatchlistShows(token: String): Result<Unit> {
        if (token.isEmpty()) {
            val message = "Cannot refresh watchlist shows: token is empty."
            logTraktException(message)
            return Result.failure(IllegalArgumentException(message))
        }

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                // 1. Get User Slug
                val userSettings = traktService.getUserSettingsAsync(bearerToken).await()
                val userSlug =
                    userSettings.user?.ids?.slug
                        ?: return@withContext Result.failure<Unit>(
                            Exception("User slug not found in Trakt settings.").also {
                                logTraktException(it.message!!)
                            },
                        )

                // 2. Get or Create "UpnextApp Watchlists" List ID (Slug)
                val listIdSlugResult = getOrCreateWatchlistsListId(token, userSlug)
                if (listIdSlugResult.isFailure) {
                    val error =
                        listIdSlugResult.exceptionOrNull()
                            ?: Exception("Unknown error getting/creating watchlists list ID.")
                    return@withContext Result.failure(error)
                }
                val watchlistsListSlug = listIdSlugResult.getOrThrow()

                // 3. Fetch items from this list and update DB
                val fetchResult = fetchAndStoreWatchlistShows(token, userSlug, watchlistsListSlug)
                if (fetchResult.isSuccess) {
                    // 4. Update table timestamp
                    logTableUpdateTimestamp(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
                    Result.success(Unit)
                } else {
                    val error =
                        fetchResult.exceptionOrNull()
                            ?: Exception("Unknown error fetching watchlist show items.")
                    Result.failure(error)
                }
            } catch (e: Exception) {
                logTraktException("Error refreshing watchlist shows", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun getOrCreateWatchlistsListId(
        token: String,
        userSlug: String,
    ): Result<String> {
        val bearerToken = "Bearer $token"
        return safeApiCall {
            val userCustomLists = traktService.getUserCustomListsAsync(bearerToken, userSlug).await()
            val watchlistsList = userCustomLists.find { it.name == FAVORITES_LIST_NAME }

            if (watchlistsList?.ids?.slug != null) {
                watchlistsList.ids.slug
            } else {
                val createRequest =
                    NetworkTraktCreateCustomListRequest(
                        name = FAVORITES_LIST_NAME,
                        privacy = "private",
                    )
                val createdList =
                    traktService.createCustomListAsync(
                        token = bearerToken,
                        userSlug = userSlug,
                        createCustomListRequest = createRequest,
                    ).await()
                createdList.ids?.slug
                    ?: throw Exception("Failed to create watchlists list '$FAVORITES_LIST_NAME', or slug was null after creation.")
            }
        }
    }

    /**
     * Migrates shows from the "Upnext Watchlists" custom list to the native
     * Trakt watchlist. This is idempotent — Trakt ignores duplicate adds.
     * If the custom list doesn't exist, this is a no-op.
     */
    suspend fun migrateWatchlistsToWatchlist(token: String): Result<Unit> {
        if (token.isEmpty()) return Result.success(Unit)

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"

                // 1. Get user slug
                val userSettings = traktService.getUserSettingsAsync(bearerToken).await()
                val userSlug = userSettings.user?.ids?.slug
                    ?: return@withContext Result.success(Unit) // Can't look up lists without slug

                // 2. Find the "Upnext Watchlists" list (without creating it)
                val userCustomLists = traktService.getUserCustomListsAsync(bearerToken, userSlug).await()
                val watchlistsList = userCustomLists.find { it.name == FAVORITES_LIST_NAME }

                if (watchlistsList?.ids?.slug == null) {
                    // No custom list exists — nothing to migrate
                    return@withContext Result.success(Unit)
                }

                // 3. Fetch all items from the custom list
                val customListItems = traktService.getCustomListItemsAsync(
                    token = bearerToken,
                    userSlug = userSlug,
                    traktId = watchlistsList.ids.slug,
                    limit = 1000,
                ).await()

                if (customListItems.isNullOrEmpty()) {
                    return@withContext Result.success(Unit)
                }

                // 4. Extract traktIds and bulk-add to native watchlist
                val showsToMigrate = customListItems.mapNotNull { item ->
                    item.show?.ids?.trakt?.let { traktId ->
                        com.theupnextapp.network.models.trakt.NetworkTraktWatchlistRequestShow(
                            ids = com.theupnextapp.network.models.trakt.NetworkTraktWatchlistRequestShowIds(
                                trakt = traktId
                            )
                        )
                    }
                }

                if (showsToMigrate.isNotEmpty()) {
                    val request = com.theupnextapp.network.models.trakt.NetworkTraktWatchlistRequest(
                        shows = showsToMigrate,
                    )
                    traktService.addToWatchlistAsync(bearerToken, request).await()
                    timber.log.Timber.i("Migrated ${showsToMigrate.size} shows from '$FAVORITES_LIST_NAME' to native watchlist.")
                }

                Result.success(Unit)
            } catch (e: Exception) {
                // Migration failure is non-fatal — log and continue
                timber.log.Timber.w(e, "Failed to migrate '$FAVORITES_LIST_NAME' to native watchlist. Will retry next session.")
                Result.success(Unit) // Return success so refreshWatchlist continues
            }
        }
    }

    private suspend fun fetchAndStoreWatchlistShows(
        token: String,
        userSlug: String,
        listSlug: String,
    ): Result<Unit> {
        val bearerToken = "Bearer $token"

        return try {
            // Get all items from the "UpnextApp Watchlists" list
            val customListItemsResponse =
                traktService.getCustomListItemsAsync(
                    token = bearerToken,
                    userSlug = userSlug,
                    traktId = listSlug,
                    limit = 1000,
                ).await()

            handleTraktUserListItemsResponse(customListItemsResponse)
            logTableUpdateTimestamp(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
            Result.success(Unit)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.stringSuspending()
            val detailedMessage = "HTTP error fetching watchlist show items from list $listSlug: ${e.code()} - $errorBody"
            logTraktException(detailedMessage, e)
            val userMessage = parseTraktApiError(errorBody, "Server error fetching watchlist items.", moshi)
            Result.failure(Exception(userMessage, e))
        } catch (e: Exception) {
            logTraktException("Error fetching and storing items from Trakt list $listSlug", e)
            Result.failure(e)
        }
    }

    private suspend fun handleTraktUserListItemsResponse(customListItemsResponse: NetworkTraktUserListItemResponse?) {
        if (customListItemsResponse == null) {
            traktDao.deleteAllWatchlistShows()
            return
        }

        val showsFromNetworkTraktIds = customListItemsResponse.mapNotNull { it.show?.ids?.trakt }.toSet()

        val showsToInsertOrUpdateInDb = withContext(Dispatchers.IO) {
            val imageFetchAndUpdateJobs =
                customListItemsResponse.mapNotNull { networkListItem ->
                    networkListItem.show?.ids?.trakt?.let { traktId ->
                        async {
                            var dbShow = networkListItem.asDatabaseModel()
                            val existingLocalShow = traktDao.getWatchlistShowByTraktId(traktId)

                            val needsImageFetch =
                                existingLocalShow == null ||
                                    existingLocalShow.originalImageUrl.isNullOrEmpty() ||
                                    existingLocalShow.mediumImageUrl.isNullOrEmpty() ||
                                    existingLocalShow.tvMazeID == null

                            if (needsImageFetch) {
                                networkListItem.show.ids.imdb?.let { imdbId ->
                                    val (tvMazeIdResult, poster, heroImage) = getImages(imdbId)
                                    dbShow =
                                        dbShow.copy(
                                            originalImageUrl = poster ?: existingLocalShow?.originalImageUrl,
                                            mediumImageUrl = heroImage ?: existingLocalShow?.mediumImageUrl,
                                            tvMazeID = tvMazeIdResult ?: existingLocalShow?.tvMazeID,
                                        )
                                }
                            } else if (existingLocalShow != null) {
                                dbShow =
                                    dbShow.copy(
                                        id = existingLocalShow.id,
                                        originalImageUrl = existingLocalShow.originalImageUrl,
                                        mediumImageUrl = existingLocalShow.mediumImageUrl,
                                        tvMazeID = existingLocalShow.tvMazeID,
                                    )
                            }
                            dbShow
                        }
                    }
                }
            imageFetchAndUpdateJobs.awaitAll()
        }

        val localWatchlistTraktIds = traktDao.getAllWatchlistShowTraktIds()
        val showsToDeleteTraktIds = localWatchlistTraktIds.filter { it !in showsFromNetworkTraktIds }

        if (showsToDeleteTraktIds.isNotEmpty()) {
            traktDao.deleteWatchlistShowsByTraktIds(showsToDeleteTraktIds)
        }

        if (showsToInsertOrUpdateInDb.isNotEmpty()) {
            traktDao.insertAllWatchlistShows(*showsToInsertOrUpdateInDb.toTypedArray())
        }
    }

    suspend fun addShowToWatchlists(
        imdbId: String,
        token: String,
    ): Result<Unit> {
        if (imdbId.isEmpty() || token.isEmpty()) {
            val message = "Cannot add show to watchlists: IMDb ID or token is empty."
            logTraktException(message)
            return Result.failure(IllegalArgumentException(message))
        }

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val showSummaryResponse = traktService.getShowInfoAsync(imdbID = imdbId).await()

                val traktShowId = showSummaryResponse.ids?.trakt
                if (traktShowId == null) {
                    val noTraktIdMessage = "Could not retrieve Trakt ID for show with IMDb ID $imdbId"
                    return@withContext Result.failure(Exception(noTraktIdMessage))
                }

                val userSettings = traktService.getUserSettingsAsync(bearerToken).await()
                val userSlug =
                    userSettings.user?.ids?.slug
                        ?: return@withContext Result.failure(Exception("User slug not found"))

                val listIdSlugResult = getOrCreateWatchlistsListId(token, userSlug)
                if (listIdSlugResult.isFailure) {
                    return@withContext Result.failure(listIdSlugResult.exceptionOrNull()!!)
                }
                val watchlistsListSlug = listIdSlugResult.getOrThrow()

                val addRequest =
                    NetworkTraktAddShowToListRequest(
                        shows =
                        listOf(
                            NetworkTraktAddShowToListRequestShow(
                                ids = NetworkTraktAddShowToListRequestShowIds(trakt = traktShowId),
                            ),
                        ),
                    )

                val addResponse =
                    traktService.addShowToCustomListAsync(
                        token = bearerToken,
                        userSlug = userSlug,
                        traktId = watchlistsListSlug,
                        networkTraktAddShowToListRequest = addRequest,
                    ).await()

                val showSuccessfullyAdded = addResponse.added.shows == 1
                val showAlreadyExistsOnList = addResponse.existing.shows == 1

                if (showSuccessfullyAdded || showAlreadyExistsOnList) {
                    var dbShow =
                        DatabaseWatchlistShows(
                            id = null,
                            title = showSummaryResponse.title,
                            year = showSummaryResponse.year?.toString(),
                            imdbID = imdbId,
                            slug = showSummaryResponse.ids.slug,
                            tmdbID = showSummaryResponse.ids.tmdb,
                            traktID = traktShowId,
                            tvdbID = showSummaryResponse.ids.tvdb,
                            mediumImageUrl = null,
                            originalImageUrl = null,
                            tvMazeID = null,
                            network = null,
                            status = null,
                            rating = null,
                        )
                    val (tvMazeIdResult, poster, heroImage) = getImages(imdbId)
                    dbShow =
                        dbShow.copy(
                            tvMazeID = tvMazeIdResult,
                            originalImageUrl = poster,
                            mediumImageUrl = heroImage,
                        )
                    traktDao.insertWatchlistShow(dbShow)
                    logTableUpdateTimestamp(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Show not added/found. Response: $addResponse"))
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.stringSuspending()
                val userMessage = parseTraktApiError(errorBody, "Server error adding show to watchlists.", moshi)
                Result.failure(Exception(userMessage, e))
            } catch (e: Exception) {
                logTraktException("Error adding show to watchlists", e)
                Result.failure(e)
            }
        }
    }

    suspend fun removeShowFromWatchlists(
        traktId: Int,
        imdbId: String,
        token: String,
    ): Result<Unit> {
        if (token.isEmpty()) {
            return Result.failure(IllegalArgumentException("Token is empty"))
        }

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val userSettings = traktService.getUserSettingsAsync(bearerToken).await()
                val userSlug =
                    userSettings.user?.ids?.slug
                        ?: return@withContext Result.failure(Exception("User slug not found"))

                val listIdSlugResult = getOrCreateWatchlistsListId(token, userSlug)
                if (listIdSlugResult.isFailure) {
                    return@withContext Result.failure(listIdSlugResult.exceptionOrNull()!!)
                }
                val watchlistsListSlug = listIdSlugResult.getOrThrow()

                val request =
                    NetworkTraktRemoveShowFromListRequest(
                        shows =
                        listOf(
                            NetworkTraktRemoveShowFromListRequestShow(
                                ids = NetworkTraktRemoveShowFromListRequestShowIds(trakt = traktId),
                            ),
                        ),
                    )

                val removeResponse =
                    traktService.removeShowFromCustomListAsync(
                        token = bearerToken,
                        userSlug = userSlug,
                        traktId = watchlistsListSlug,
                        networkTraktRemoveShowFromListRequest = request,
                    ).await()

                val showSuccessfullyRemovedOnTrakt = removeResponse.deleted.shows == 1
                val showNotFoundOnTraktList =
                    removeResponse.not_found.shows?.any { it.ids.trakt == traktId } == true

                if (showSuccessfullyRemovedOnTrakt || showNotFoundOnTraktList) {
                    traktDao.deleteWatchlistShowByTraktId(traktId)
                    logTableUpdateTimestamp(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to remove show from Trakt."))
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.stringSuspending()
                val userMessage = parseTraktApiError(errorBody, "Failed to remove show.", moshi)
                Result.failure(Exception(userMessage, e))
            } catch (e: Exception) {
                logTraktException("Error removing show", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getWatchlist(token: String): Result<com.theupnextapp.network.models.trakt.NetworkTraktWatchlistResponse> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Token is empty"))
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val response = traktService.getWatchlistAsync(bearerToken).await()
                Result.success(response)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.stringSuspending()
                val userMessage = parseTraktApiError(errorBody, "Failed to fetch watchlist.", moshi)
                Result.failure(Exception(userMessage, e))
            } catch (e: Exception) {
                logTraktException("Error fetching watchlist", e)
                Result.failure(e)
            }
        }
    }

    suspend fun addToWatchlist(
        traktId: Int,
        token: String,
    ): Result<Unit> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Token is empty"))
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val request = com.theupnextapp.network.models.trakt.NetworkTraktWatchlistRequest(
                    shows = listOf(
                        com.theupnextapp.network.models.trakt.NetworkTraktWatchlistRequestShow(
                            ids = com.theupnextapp.network.models.trakt.NetworkTraktWatchlistRequestShowIds(trakt = traktId)
                        )
                    )
                )

                val response = traktService.addToWatchlistAsync(bearerToken, request).await()
                if (response.added.shows == 1 || response.existing.shows == 1) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to add show to watchlist. Trakt response: $response"))
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.stringSuspending()
                val userMessage = parseTraktApiError(errorBody, "Failed to add to watchlist.", moshi)
                Result.failure(Exception(userMessage, e))
            } catch (e: Exception) {
                logTraktException("Error adding to watchlist", e)
                Result.failure(e)
            }
        }
    }

    suspend fun removeFromWatchlist(
        traktId: Int,
        token: String,
    ): Result<Unit> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Token is empty"))
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val request = com.theupnextapp.network.models.trakt.NetworkTraktWatchlistRequest(
                    shows = listOf(
                        com.theupnextapp.network.models.trakt.NetworkTraktWatchlistRequestShow(
                            ids = com.theupnextapp.network.models.trakt.NetworkTraktWatchlistRequestShowIds(trakt = traktId)
                        )
                    )
                )

                val response = traktService.removeFromWatchlistAsync(bearerToken, request).await()
                if (response.deleted.shows == 1 || (response.not_found.shows?.any { it.ids.trakt == traktId } == true)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to remove show from watchlist."))
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.stringSuspending()
                val userMessage = parseTraktApiError(errorBody, "Failed to remove from watchlist.", moshi)
                Result.failure(Exception(userMessage, e))
            } catch (e: Exception) {
                logTraktException("Error removing from watchlist", e)
                Result.failure(e)
            }
        }
    }

    suspend fun checkInToShow(
        showTraktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        token: String?,
    ): TraktCheckInStatus {
        if (token.isNullOrEmpty()) {
            return TraktCheckInStatus(message = "Authentication token is missing.")
        }

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val request =
                    NetworkTraktCheckInRequest(
                        show =
                        NetworkTraktCheckInRequestShow(
                            ids = NetworkTraktCheckInRequestShowIds(trakt = showTraktId),
                            title = null,
                            year = null,
                        ),
                        episode =
                        NetworkTraktCheckInRequestEpisode(
                            season = seasonNumber,
                            number = episodeNumber,
                        ),
                    )

                val checkInApiResponse = traktService.checkInAsync(bearerToken, request).await()
                checkInApiResponse.asDomainModel()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                if (e.code() == HTTP_CONFLICT && errorBody != null) {
                    try {
                        val conflictResponse = traktConflictErrorAdapter.fromJson(errorBody)
                        if (conflictResponse?.expiresAt != null) {
                            TraktCheckInStatus(
                                message = "Check-in conflict. Expires at: ${conflictResponse.expiresAt}",
                            )
                        } else {
                            TraktCheckInStatus(message = "Check-in conflict (Code 409).")
                        }
                    } catch (parseEx: Exception) {
                        TraktCheckInStatus(message = "Check-in conflict (Code 409).")
                    }
                } else {
                    val msg =
                        when (e.code()) {
                            401 -> "Unauthorized."
                            else -> "Check-in failed with Code ${e.code()}"
                        }
                    TraktCheckInStatus(message = msg)
                }
            } catch (e: Exception) {
                logTraktException("Generic error during Trakt check-in.", e)
                TraktCheckInStatus(message = "An unexpected error occurred.")
            }
        }
    }

    suspend fun cancelCheckIn(token: String?): Result<Unit> {
        if (token.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException("Authentication token is missing."))
        }

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val response = traktService.cancelCheckInAsync(bearerToken).await()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to cancel check-in. HTTP ${response.code()}"))
                }
            } catch (e: HttpException) {
                Result.failure(Exception("Failed to cancel check-in.", e))
            } catch (e: Exception) {
                logTraktException("Generic error during Trakt check-in cancellation.", e)
                Result.failure(e)
            }
        }
    }

    suspend fun rateShow(
        imdbId: String,
        rating: Int,
        token: String?,
    ): Result<Unit> {
        if (token.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException("Authentication token is missing."))
        }

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val request =
                    NetworkTraktRatingRequest(
                        shows =
                            listOf(
                                NetworkTraktRatingShow(
                                    rating = rating,
                                    ids = NetworkTraktRatingShowIds(imdb = imdbId),
                                ),
                            ),
                    )
                val response = traktService.rateShowAsync(bearerToken, request).await()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to rate show (Code ${response.code()})"))
                }
            } catch (e: HttpException) {
                Result.failure(Exception("Failed to rate show.", e))
            } catch (e: Exception) {
                logTraktException("Generic error during Trakt show rating.", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getUserShowRating(
        imdbId: String,
        token: String?,
    ): Int? {
        if (token.isNullOrEmpty()) return null

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val ratings = traktService.getUserShowRatingsAsync(bearerToken).await()
                ratings.firstOrNull { it.show?.ids?.imdb == imdbId }?.rating
            } catch (e: Exception) {
                logTraktException("Error fetching user show rating", e)
                null
            }
        }
    }

    suspend fun getTraktMySchedule(
        token: String,
        startDate: String,
        days: Int,
    ): Result<NetworkTraktMyScheduleResponse> {
        if (token.isEmpty()) {
            return Result.failure(IllegalArgumentException("Token is empty"))
        }

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val response = traktService.getMyCalendarAsync(bearerToken, startDate, days).await()
                Result.success(response)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.stringSuspending()
                val userMessage = parseTraktApiError(errorBody, "Failed to fetch calendar.", moshi)
                Result.failure(Exception(userMessage, e))
            } catch (e: Exception) {
                logTraktException("Error fetching calendar", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getTraktPlaybackProgress(token: String): Result<List<com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse>> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Token is empty"))
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val pausedDeferred = traktService.getPlaybackProgressAsync(bearerToken)
                val watchedDeferred = traktService.getWatchedShowsAsync(bearerToken)

                val pausedItems: List<com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse> = try {
                    pausedDeferred.await()
                } catch (e: Exception) {
                    emptyList<com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse>()
                }
                val watchedShowsResponse: List<com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowsResponse> = try {
                    watchedDeferred.await()
                } catch (e: Exception) {
                    emptyList<com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowsResponse>()
                }

                val topWatched = watchedShowsResponse
                    .sortedByDescending { it.lastUpdatedAt ?: it.lastWatchedAt ?: "" }
                    .take(15)

                val progressDeferreds = topWatched.mapNotNull { showItem ->
                    showItem.show?.ids?.trakt?.let { traktId ->
                        async {
                            try {
                                val progress = traktService.getShowProgressAsync(
                                    token = bearerToken,
                                    id = traktId.toString(),
                                ).await()
                                Pair<com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowsResponse, com.theupnextapp.network.models.trakt.NetworkTraktShowProgressResponse>(showItem, progress)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                }

                val progressResults = progressDeferreds.awaitAll().filterNotNull()
                val upNextItems = mutableListOf<com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse>()
                val pausedShowIds = pausedItems.mapNotNull { it.show?.ids?.trakt }.toSet()
                
                upNextItems.addAll(pausedItems)

                for ((watchedShow, progress) in progressResults) {
                    val traktId = watchedShow.show?.ids?.trakt
                    if (traktId != null && !pausedShowIds.contains(traktId)) {
                        val nextEp = progress.nextEpisode
                        if (nextEp != null) {
                            val computedProgress = if ((progress.aired ?: 0) > 0 && progress.completed != null) {
                                (progress.completed!!.toFloat() / progress.aired!!.toFloat()) * 100f
                            } else {
                                0f
                            }
                            upNextItems.add(
                                com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse(
                                    progress = computedProgress,
                                    action = null,
                                    type = "episode",
                                    show = watchedShow.show,
                                    episode = nextEp
                                )
                            )
                        }
                    }
                }
                
                Result.success(upNextItems.take(20))
            } catch (e: Exception) {
                logTraktException("Error fetching playback progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getTraktWatchedShows(token: String): Result<List<com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowsResponse>> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Token is empty"))
        return withContext(Dispatchers.IO) {
            try {
                val response = traktService.getWatchedShowsAsync("Bearer $token").await()
                Result.success(response)
            } catch (e: Exception) {
                logTraktException("Error fetching watched shows", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getTraktRecentHistory(token: String): Result<List<com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse>> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Token is empty"))
        return withContext(Dispatchers.IO) {
            try {
                val response = traktService.getRecentHistoryAsync("Bearer $token").await()
                Result.success(response)
            } catch (e: Exception) {
                logTraktException("Error fetching recent history", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getTraktShowProgress(token: String, showId: String): Result<com.theupnextapp.network.models.trakt.NetworkTraktShowProgressResponse> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Token is empty"))
        return withContext(Dispatchers.IO) {
            try {
                val response = traktService.getShowProgressAsync("Bearer $token", showId).await()
                Result.success(response)
            } catch (e: Exception) {
                logTraktException("Error fetching show progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getTraktRecommendations(token: String): Result<com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = traktService.getRecommendationsAsync(token = token).await()
                Result.success(response)
            } catch (e: Exception) {
                logTraktException("Error fetching recommendations", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun ResponseBody?.stringSuspending(charset: Charset = Charsets.UTF_8): String? {
        return this?.source()?.use { source ->
            withContext(Dispatchers.IO) {
                source.request(Long.MAX_VALUE)
                source.buffer.clone().readString(charset)
            }
        }
    }

    private fun parseTraktApiError(
        errorBodyString: String?,
        defaultMessage: String,
        moshi: Moshi,
    ): String {
        if (errorBodyString.isNullOrBlank()) return defaultMessage
        return try {
            val errorAdapter = moshi.adapter(TraktErrorResponse::class.java)
            val traktError = errorAdapter.fromJson(errorBodyString)
            traktError?.errorDescription ?: traktError?.error ?: defaultMessage
        } catch (e: Exception) {
            defaultMessage
        }
    }

    companion object {
        const val FAVORITES_LIST_NAME = "Upnext Favorites"
        private const val HTTP_CONFLICT = 409
    }
}
