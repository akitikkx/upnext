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
import com.theupnextapp.database.DatabaseFavoriteShows
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
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequestShow
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequestShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktUserListItemResponse
import com.theupnextapp.network.models.trakt.TraktConflictErrorResponse
import com.theupnextapp.network.models.trakt.TraktErrorResponse
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

    suspend fun refreshFavoriteShows(token: String): Result<Unit> {
        if (token.isEmpty()) {
            val message = "Cannot refresh favorite shows: token is empty."
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

                // 2. Get or Create "UpnextApp Favorites" List ID (Slug)
                val listIdSlugResult = getOrCreateFavoritesListId(token, userSlug)
                if (listIdSlugResult.isFailure) {
                    val error =
                        listIdSlugResult.exceptionOrNull()
                            ?: Exception("Unknown error getting/creating favorites list ID.")
                    return@withContext Result.failure(error)
                }
                val favoritesListSlug = listIdSlugResult.getOrThrow()

                // 3. Fetch items from this list and update DB
                val fetchResult = fetchAndStoreFavoriteShows(token, userSlug, favoritesListSlug)
                if (fetchResult.isSuccess) {
                    // 4. Update table timestamp
                    logTableUpdateTimestamp(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
                    Result.success(Unit)
                } else {
                    val error =
                        fetchResult.exceptionOrNull()
                            ?: Exception("Unknown error fetching favorite show items.")
                    Result.failure(error)
                }
            } catch (e: Exception) {
                logTraktException("Error refreshing favorite shows", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun getOrCreateFavoritesListId(
        token: String,
        userSlug: String,
    ): Result<String> {
        val bearerToken = "Bearer $token"
        return safeApiCall {
            val userCustomLists = traktService.getUserCustomListsAsync(bearerToken, userSlug).await()
            val favoritesList = userCustomLists.find { it.name == FAVORITES_LIST_NAME }

            if (favoritesList?.ids?.slug != null) {
                favoritesList.ids.slug
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
                    ?: throw Exception("Failed to create favorites list '$FAVORITES_LIST_NAME', or slug was null after creation.")
            }
        }
    }

    private suspend fun fetchAndStoreFavoriteShows(
        token: String,
        userSlug: String,
        listSlug: String,
    ): Result<Unit> {
        val bearerToken = "Bearer $token"

        return try {
            // Get all items from the "UpnextApp Favorites" list
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
            val detailedMessage = "HTTP error fetching favorite show items from list $listSlug: ${e.code()} - $errorBody"
            logTraktException(detailedMessage, e)
            val userMessage = parseTraktApiError(errorBody, "Server error fetching favorite items.", moshi)
            Result.failure(Exception(userMessage, e))
        } catch (e: Exception) {
            logTraktException("Error fetching and storing items from Trakt list $listSlug", e)
            Result.failure(e)
        }
    }

    private suspend fun handleTraktUserListItemsResponse(customListItemsResponse: NetworkTraktUserListItemResponse?) {
        if (customListItemsResponse == null) {
            traktDao.deleteAllFavoriteShows()
            return
        }

        val showsFromNetworkTraktIds = customListItemsResponse.mapNotNull { it.show?.ids?.trakt }.toSet()

        val showsToInsertOrUpdateInDb = withContext(Dispatchers.IO) {
            val imageFetchAndUpdateJobs =
                customListItemsResponse.mapNotNull { networkListItem ->
                    networkListItem.show?.ids?.trakt?.let { traktId ->
                        async {
                            var dbShow = networkListItem.asDatabaseModel()
                            val existingLocalShow = traktDao.getFavoriteShowByTraktId(traktId)

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

        val localFavoriteTraktIds = traktDao.getAllFavoriteShowTraktIds()
        val showsToDeleteTraktIds = localFavoriteTraktIds.filter { it !in showsFromNetworkTraktIds }

        if (showsToDeleteTraktIds.isNotEmpty()) {
            traktDao.deleteFavoriteShowsByTraktIds(showsToDeleteTraktIds)
        }

        if (showsToInsertOrUpdateInDb.isNotEmpty()) {
            traktDao.insertAllFavoriteShows(*showsToInsertOrUpdateInDb.toTypedArray())
        }
    }

    suspend fun addShowToFavorites(
        imdbId: String,
        token: String,
    ): Result<Unit> {
        if (imdbId.isEmpty() || token.isEmpty()) {
            val message = "Cannot add show to favorites: IMDb ID or token is empty."
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

                val listIdSlugResult = getOrCreateFavoritesListId(token, userSlug)
                if (listIdSlugResult.isFailure) {
                    return@withContext Result.failure(listIdSlugResult.exceptionOrNull()!!)
                }
                val favoritesListSlug = listIdSlugResult.getOrThrow()

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
                        traktId = favoritesListSlug,
                        networkTraktAddShowToListRequest = addRequest,
                    ).await()

                val showSuccessfullyAdded = addResponse.added.shows == 1
                val showAlreadyExistsOnList = addResponse.existing.shows == 1

                if (showSuccessfullyAdded || showAlreadyExistsOnList) {
                    var dbShow =
                        DatabaseFavoriteShows(
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
                        )
                    val (tvMazeIdResult, poster, heroImage) = getImages(imdbId)
                    dbShow =
                        dbShow.copy(
                            tvMazeID = tvMazeIdResult,
                            originalImageUrl = poster,
                            mediumImageUrl = heroImage,
                        )
                    traktDao.insertFavoriteShow(dbShow)
                    logTableUpdateTimestamp(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Show not added/found. Response: $addResponse"))
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.stringSuspending()
                val userMessage = parseTraktApiError(errorBody, "Server error adding show to favorites.", moshi)
                Result.failure(Exception(userMessage, e))
            } catch (e: Exception) {
                logTraktException("Error adding show to favorites", e)
                Result.failure(e)
            }
        }
    }

    suspend fun removeShowFromFavorites(
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

                val listIdSlugResult = getOrCreateFavoritesListId(token, userSlug)
                if (listIdSlugResult.isFailure) {
                    return@withContext Result.failure(listIdSlugResult.exceptionOrNull()!!)
                }
                val favoritesListSlug = listIdSlugResult.getOrThrow()

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
                        traktId = favoritesListSlug,
                        networkTraktRemoveShowFromListRequest = request,
                    ).await()

                val showSuccessfullyRemovedOnTrakt = removeResponse.deleted.shows == 1
                val showNotFoundOnTraktList =
                    removeResponse.not_found.shows?.any { it.ids.trakt == traktId } == true

                if (showSuccessfullyRemovedOnTrakt || showNotFoundOnTraktList) {
                    traktDao.deleteFavoriteShowByTraktId(traktId)
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

    suspend fun checkInToShow(
        showSeasonEpisode: ShowSeasonEpisode,
        token: String?,
    ): TraktCheckInStatus {
        if (token.isNullOrEmpty()) {
            return TraktCheckInStatus(message = "Authentication token is missing.")
        }
        val imdbId = showSeasonEpisode.imdbID
        val seasonNumber = showSeasonEpisode.season
        val episodeNumber = showSeasonEpisode.number

        if (imdbId.isNullOrEmpty()) {
            return TraktCheckInStatus(message = "Show information (IMDB ID) is missing.")
        }
        if (seasonNumber == null || episodeNumber == null) {
            return TraktCheckInStatus(message = "Episode information incomplete.")
        }

        return withContext(Dispatchers.IO) {
            try {
                val idLookupResponse = traktService.idLookupAsync(idType = "imdb", id = imdbId).await()
                val traktShowId =
                    idLookupResponse.firstNotNullOfOrNull { result ->
                        if (result.type == "show" && result.show?.ids?.trakt != null) {
                            result.show.ids.trakt
                        } else {
                            null
                        }
                    }

                if (traktShowId == null) {
                    return@withContext TraktCheckInStatus(
                        message = "Could not find the show on Trakt using IMDB ID."
                    )
                }

                val bearerToken = "Bearer $token"
                val request =
                    NetworkTraktCheckInRequest(
                        show =
                        NetworkTraktCheckInRequestShow(
                            ids = NetworkTraktCheckInRequestShowIds(trakt = traktShowId),
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
                if (e.code() == 409 && errorBody != null) {
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
    }
}
