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
import com.squareup.moshi.Moshi
import com.theupnextapp.BuildConfig
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.database.DatabaseFavoriteShows
import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.DatabaseTraktMostAnticipated
import com.theupnextapp.database.DatabaseTraktPopularShows
import com.theupnextapp.database.DatabaseTraktTrendingShows
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktCheckInStatus
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.domain.TraktShowStats
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.domain.TraktUserList
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAddShowToListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAddShowToListRequestShow
import com.theupnextapp.network.models.trakt.NetworkTraktAddShowToListRequestShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInRequest
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInRequestEpisode
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInRequestShow
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInRequestShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInResponse
import com.theupnextapp.network.models.trakt.NetworkTraktCreateCustomListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktMostAnticipatedResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequestShow
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequestShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktTrendingShowsResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktUserListItemResponse
import com.theupnextapp.network.models.trakt.TraktConflictErrorResponse
import com.theupnextapp.network.models.trakt.TraktErrorResponse
import com.theupnextapp.network.models.trakt.asDatabaseModel
import com.theupnextapp.network.models.trakt.asDomainModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import timber.log.Timber
import java.nio.charset.Charset

class TraktRepositoryImpl(
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    private val traktDao: TraktDao,
    private val traktService: TraktService,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val moshi: Moshi,
) : BaseRepository(upnextDao = upnextDao, tvMazeService = tvMazeService), TraktRepository {
    // Scope for long-running repository tasks, uses SupervisorJob to prevent one failure from cancelling all
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun tableUpdate(tableName: String): Flow<TableUpdate?> =
        upnextDao.getTableLastUpdate(tableName).map { it?.asDomainModel() }.flowOn(Dispatchers.IO)

    override val traktPopularShows: Flow<List<TraktPopularShows>> =
        traktDao.getTraktPopular().map { it.asDomainModel() }.flowOn(Dispatchers.IO)

    override val traktTrendingShows: Flow<List<TraktTrendingShows>> =
        traktDao.getTraktTrending().map { it.asDomainModel() }.flowOn(Dispatchers.IO)

    override val traktMostAnticipatedShows: Flow<List<TraktMostAnticipated>> =
        traktDao.getTraktMostAnticipated().map { it.asDomainModel() }.flowOn(Dispatchers.IO)

    override val traktFavoriteShows: Flow<List<TraktUserListItem>> =
        traktDao.getFavoriteShows().map { it.asDomainModel() }.flowOn(Dispatchers.IO)

    override val traktAccessToken: StateFlow<TraktAccessToken?> =
        traktDao.getTraktAccessData()
            .map { it?.asDomainModel() }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = repositoryScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    // StateFlows for UI State
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingTraktTrending = MutableStateFlow(false)
    override val isLoadingTraktTrending: StateFlow<Boolean> = _isLoadingTraktTrending.asStateFlow()

    private val _isLoadingTraktPopular = MutableStateFlow(false)
    override val isLoadingTraktPopular: StateFlow<Boolean> = _isLoadingTraktPopular.asStateFlow()

    private val _isLoadingTraktMostAnticipated = MutableStateFlow(false)
    override val isLoadingTraktMostAnticipated: StateFlow<Boolean> =
        _isLoadingTraktMostAnticipated.asStateFlow()

    private val _isLoadingFavoriteShows = MutableStateFlow(false)
    override val isLoadingFavoriteShows: StateFlow<Boolean> = _isLoadingFavoriteShows.asStateFlow()

    private val _favoriteShowsError = MutableStateFlow<String?>(null)
    override val favoriteShowsError: StateFlow<String?> = _favoriteShowsError.asStateFlow()

    private val _isLoadingUserCustomLists = MutableStateFlow(false)
    override val isLoadingUserCustomLists: StateFlow<Boolean> =
        _isLoadingUserCustomLists.asStateFlow()

    private val _userCustomListsError = MutableStateFlow<String?>(null)
    override val userCustomListsError: StateFlow<String?> = _userCustomListsError.asStateFlow()

    override val traktUserCustomLists: Flow<List<TraktUserList>> =
        emptyFlow()
    private val _traktShowRating = MutableStateFlow<TraktShowRating?>(null)
    override val traktShowRating: StateFlow<TraktShowRating?> = _traktShowRating.asStateFlow()

    private val _traktShowStats = MutableStateFlow<TraktShowStats?>(null)
    override val traktShowStats: StateFlow<TraktShowStats?> = _traktShowStats.asStateFlow()

    private val _favoriteShow = MutableStateFlow<TraktUserListItem?>(null)
    override val favoriteShow: StateFlow<TraktUserListItem?> = _favoriteShow.asStateFlow()

    private val _traktCheckInEvent =
        MutableSharedFlow<TraktCheckInStatus>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    private val traktConflictErrorAdapter = moshi.adapter(TraktConflictErrorResponse::class.java)

    override val traktCheckInEvent: SharedFlow<TraktCheckInStatus> =
        _traktCheckInEvent.asSharedFlow()

    override fun getTraktAccessTokenRaw(): DatabaseTraktAccess? = traktDao.getTraktAccessDataRaw()

    /**
     * Placeholder for your actual logging function.
     * This function should ideally log to Timber and/or a remote crash reporting service.
     */
    private fun logTraktException(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            Timber.e(throwable, "Trakt Error: $message")
            firebaseCrashlytics.recordException(throwable)
        } else {
            Timber.e("Trakt Error: $message")
            // Optionally log a non-fatal to Firebase here if it's an error state without an exception
        }
    }

    private fun handleGenericException(
        e: Exception,
        messagePrefix: String = "An error occurred",
    ) {
        Timber.e(e, "$messagePrefix: ${e.message}")
        firebaseCrashlytics.recordException(e)
    }

    private suspend fun <T> safeApiCall(
        loadingStateFlow: MutableStateFlow<Boolean> = _isLoading,
        errorStateFlow: MutableStateFlow<String?>? = null,
        apiCall: suspend () -> T,
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            loadingStateFlow.value = true
            errorStateFlow?.value = null
            try {
                Result.success(apiCall())
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val specificMessage = "HTTP ${e.code()}: ${e.message()}. Body: $errorBody"
                logTraktException(specificMessage, e)
                errorStateFlow?.value = specificMessage
                Result.failure(e)
            } catch (e: Exception) {
                logTraktException("API call failed: ${e.message}", e)
                errorStateFlow?.value = e.localizedMessage ?: "An unknown error occurred."
                Result.failure(e)
            } finally {
                loadingStateFlow.value = false
            }
        }
    }

    override suspend fun getTraktShowStats(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            logTraktException("Cannot get show stats: imdbID is null or empty.")
            _traktShowStats.value = null
            return
        }
        // Assuming _isLoading can be used or a more specific one if created for show details
        safeApiCall {
            traktService.getShowStatsAsync(imdbID).await()
        }.onSuccess { stats ->
            _traktShowStats.value = stats.asDomainModel()
        }.onFailure {
            _traktShowStats.value = null
        }
    }

    override suspend fun getTraktAccessToken(code: String): Result<TraktAccessToken> {
        if (code.isEmpty()) {
            val message = "Attempted to get access token with empty code."
            logTraktException(message)
            return Result.failure(IllegalArgumentException(message))
        }
        return safeApiCall(_isLoading) {
            val request =
                NetworkTraktAccessTokenRequest(
                    code = code,
                    client_id = BuildConfig.TRAKT_CLIENT_ID,
                    client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                    redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                    grant_type = "authorization_code",
                )
            val response = traktService.getAccessTokenAsync(request).await()
            traktDao.deleteTraktAccessData()
            traktDao.insertAllTraktAccessData(response.asDatabaseModel())
            response.asDomainModel()
        }.onFailure { error ->
            logTraktException("Failed to get Trakt access token.", error)
        }
    }

    @Deprecated(message = "Use revokeTraktAccessToken(token: String) instead.")
    override suspend fun revokeTraktAccessToken(traktAccessToken: TraktAccessToken) {
        val tokenToRevoke = traktAccessToken.access_token
        if (tokenToRevoke.isNullOrEmpty()) {
            logTraktException("Attempted to revoke access token with null or empty token string.")
            return
        }
        val result = revokeTraktAccessToken(tokenToRevoke)
        if (result.isFailure) {
            logTraktException(
                "Failed to revoke Trakt access token (via old method).",
                result.exceptionOrNull(),
            )
        }
    }

    override suspend fun revokeTraktAccessToken(token: String): Result<Unit> {
        if (token.isEmpty()) {
            val message = "Attempted to revoke access token with empty token string."
            logTraktException(message)
            return Result.failure(IllegalArgumentException(message))
        }
        return safeApiCall(_isLoading) {
            val request =
                NetworkTraktRevokeAccessTokenRequest(
                    client_id = BuildConfig.TRAKT_CLIENT_ID,
                    client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                    token = token,
                )
            traktService.revokeAccessTokenAsync(request).await()
            traktDao.deleteTraktAccessData() // Clear local token data
            Unit // Success
        }.onFailure { error ->
            logTraktException("Failed to revoke Trakt access token (token: $token).", error)
        }
    }

    override suspend fun getTraktAccessRefreshToken(refreshToken: String?): Result<TraktAccessToken> {
        if (refreshToken.isNullOrEmpty()) {
            val message = "Attempted to refresh access token with null or empty refresh token."
            logTraktException(message)
            return Result.failure(IllegalArgumentException(message))
        }
        return safeApiCall(_isLoading) {
            val request =
                NetworkTraktAccessRefreshTokenRequest(
                    refresh_token = refreshToken,
                    client_id = BuildConfig.TRAKT_CLIENT_ID,
                    client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                    redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                    grant_type = "refresh_token",
                )
            val response = traktService.getAccessRefreshTokenAsync(request).await()
            traktDao.deleteTraktAccessData()
            traktDao.insertAllTraktAccessData(response.asDatabaseModel())
            response.asDomainModel()
        }.onFailure { error ->
            logTraktException("Failed to refresh Trakt access token.", error)
        }
    }

    override fun isAuthorizedOnTrakt(): StateFlow<Boolean> {
        return traktAccessToken.map { it != null }
            .stateIn(
                scope = repositoryScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = getTraktAccessTokenRaw() != null, // Initial check
            )
    }

    private suspend fun getOrCreateFavoritesListId(
        token: String,
        userSlug: String,
    ): Result<String> {
        val bearerToken = "Bearer $token"
        return safeApiCall(_isLoadingUserCustomLists, _userCustomListsError) {
            val userCustomLists =
                traktService.getUserCustomListsAsync(bearerToken, userSlug).await()
            Timber.d("User custom lists fetched: ${userCustomLists.map { it.name }}")

            val favoritesList = userCustomLists.find { it.name == FAVORITES_LIST_NAME }

            if (favoritesList?.ids?.slug != null) {
                Timber.d("Found existing favorites list: '${favoritesList.name}' with slug: ${favoritesList.ids.slug}")
                favoritesList.ids.slug // Safe call due to check
            } else {
                Timber.d("Favorites list '$FAVORITES_LIST_NAME' not found. Creating it.")
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
                Timber.d("Created new favorites list: '${createdList.name}' with slug: ${createdList.ids?.slug}")
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
        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null
        val bearerToken = "Bearer $token"

        return try {
            Timber.d("Fetching items from Trakt list: User: $userSlug, List Slug: $listSlug")
            // Get all items from the "UpnextApp Favorites" list
            val customListItemsResponse =
                traktService.getCustomListItemsAsync(
                    token = bearerToken,
                    userSlug = userSlug,
                    traktId = listSlug,
                    limit = 1000 // Ensure we get all items (or at least a large page)
                ).await()

            Timber.d("Fetched ${customListItemsResponse.size} items from Trakt favorites list ($listSlug).")

            handleTraktUserListItemsResponse(customListItemsResponse)

            logTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS)
            Timber.d("Successfully fetched and stored favorite shows from list $listSlug.")
            Result.success(Unit)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.stringSuspending()
            val detailedMessage =
                "HTTP error fetching favorite show items from list $listSlug: ${e.code()} - $errorBody"
            logTraktException(detailedMessage, e)
            _favoriteShowsError.value =
                parseTraktApiError(
                    errorBody,
                    defaultMessage = "Server error fetching favorite items.",
                    moshi = moshi,
                )
            Result.failure(Exception(detailedMessage, e))
        } catch (e: Exception) {
            val errorMessage = "Error fetching and storing items from Trakt list $listSlug"
            handleGenericException(e, errorMessage)
            _favoriteShowsError.value = e.localizedMessage ?: errorMessage
            Result.failure(e)
        } finally {
            // isLoadingFavoriteShows should be managed by the calling function (refreshFavoriteShows)
            // or set to false here if this is the end of the chain for a specific operation.
            // Given refreshFavoriteShows wraps this, it will handle the final loading state.
        }
    }

    override suspend fun refreshFavoriteShows(
        forceRefresh: Boolean,
        token: String?,
    ) {
        if (token.isNullOrEmpty()) {
            logTraktException("Cannot refresh favorite shows: token is null or empty.")
            _favoriteShowsError.value = "Authentication token is missing."
            return
        }

        val result = refreshFavoriteShows(token)
        if (result.isFailure) {
            Timber.w("refreshFavoriteShows (legacy signature) failed: ${result.exceptionOrNull()?.message}")
        } else {
            Timber.d("refreshFavoriteShows (legacy signature) succeeded.")
        }
    }

    override suspend fun refreshFavoriteShows(token: String): Result<Unit> {
        if (token.isEmpty()) {
            val message = "Cannot refresh favorite shows: token is empty."
            logTraktException(message)
            _favoriteShowsError.value = message // Set error state
            return Result.failure(IllegalArgumentException(message))
        }

        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null

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
                                _favoriteShowsError.value = it.message
                            },
                        )
                Timber.d("User slug for refresh: $userSlug")

                // 2. Get or Create "UpnextApp Favorites" List ID (Slug)
                val listIdSlugResult = getOrCreateFavoritesListId(token, userSlug)
                if (listIdSlugResult.isFailure) {
                    val error =
                        listIdSlugResult.exceptionOrNull()
                            ?: Exception("Unknown error getting/creating favorites list ID.")
                    _favoriteShowsError.value =
                        error.message ?: "Failed to get/create favorites list ID."
                    return@withContext Result.failure(error)
                }
                val favoritesListSlug = listIdSlugResult.getOrThrow()
                Timber.d("Favorites list slug for refresh: $favoritesListSlug")

                // 3. Fetch items from this list and update DB
                val fetchResult = fetchAndStoreFavoriteShows(token, userSlug, favoritesListSlug)
                if (fetchResult.isSuccess) {
                    // 4. Refresh images for all favorite shows now in the DB (including newly added ones)
                    // This internal call ensures all local favorites have up-to-date images.
                    logTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS)
                    Timber.d("Favorite shows refreshed successfully (Result version).")
                    Result.success(Unit)
                } else {
                    val error =
                        fetchResult.exceptionOrNull()
                            ?: Exception("Unknown error fetching favorite show items.")
                    _favoriteShowsError.value =
                        error.message ?: "Failed to fetch favorite show items."
                    Result.failure(error)
                }
            } catch (e: Exception) {
                val errorMessage = "Error refreshing favorite shows (Result version)"
                handleGenericException(e, errorMessage)
                _favoriteShowsError.value = e.localizedMessage ?: errorMessage
                Result.failure(e)
            } finally {
                _isLoadingFavoriteShows.value = false
            }
        }
    }

    private suspend fun handleTraktUserListItemsResponse(customListItemsResponse: NetworkTraktUserListItemResponse?) {
        if (customListItemsResponse == null) {
            Timber.d("handleTraktUserListItemsResponse: Received null response. Clearing local favorites.")
            traktDao.deleteAllFavoriteShows()
            // If the list is empty on Trakt, we reflect that locally.
            // This effectively means all previous favorites are gone from this list.
            return
        }

        val showsToInsertOrUpdateInDb = mutableListOf<DatabaseFavoriteShows>()
        val showsFromNetworkTraktIds =
            customListItemsResponse.mapNotNull { it.show?.ids?.trakt }.toSet()

        // Create a list of async jobs for fetching images where needed
        val imageFetchAndUpdateJobs =
            customListItemsResponse.mapNotNull { networkListItem ->
                networkListItem.show?.ids?.trakt?.let { traktId ->
                    repositoryScope.async { // Perform image fetching and DB model creation concurrently
                        var dbShow = networkListItem.asDatabaseModel() // Basic mapping

                        // Check if local version (if any) has images, or fetch them
                        val existingLocalShow =
                            traktDao.getFavoriteShowByTraktId(traktId) // Needs this DAO method

                        // Determine if image fetch is needed
                        val needsImageFetch =
                            existingLocalShow == null || // New show
                                existingLocalShow.originalImageUrl.isNullOrEmpty() ||
                                existingLocalShow.mediumImageUrl.isNullOrEmpty() ||
                                existingLocalShow.tvMazeID == null // Also fetch if tvMazeID is missing

                        if (needsImageFetch) {
                            networkListItem.show.ids.imdb?.let { imdbId ->
                                Timber.d("Fetching images for favorite show (during list sync): '${dbShow.title}' (IMDb: $imdbId)")
                                val (tvMazeIdResult, poster, heroImage) = getImages(imdbId) // From BaseRepository

                                dbShow =
                                    dbShow.copy(
                                        originalImageUrl =
                                            poster
                                                ?: existingLocalShow?.originalImageUrl,
                                        // Keep old if new is null
                                        mediumImageUrl =
                                            heroImage
                                                ?: existingLocalShow?.mediumImageUrl,
                                        // Keep old if new is null
                                        tvMazeID =
                                            tvMazeIdResult
                                                ?: existingLocalShow?.tvMazeID, // Keep old if new is null
                                    )
                            }
                                ?: Timber.w("Favorite show '${dbShow.title}' (TraktID: $traktId) missing imdbID for image fetch during list sync.")
                        } else if (existingLocalShow != null) {
                            // Images and TvMazeID exist locally, preserve them
                            // Also ensure we keep the local DB ID if we're updating
                            dbShow =
                                dbShow.copy(
                                    id = existingLocalShow.id, // Keep the auto-generated primary key
                                    originalImageUrl = existingLocalShow.originalImageUrl,
                                    mediumImageUrl = existingLocalShow.mediumImageUrl,
                                    tvMazeID = existingLocalShow.tvMazeID,
                                )
                        }
                        // Add to a temporary list; will be processed after all jobs complete
                        showsToInsertOrUpdateInDb.add(dbShow)
                        dbShow // Return the processed dbShow
                    }
                }
            }

        // Wait for all image fetching and model preparation jobs to complete
        val processedDbShows = imageFetchAndUpdateJobs.awaitAll()

        // Now, perform database operations
        // 1. Delete shows from DB that are no longer in the Trakt favorites list
        // This requires getting all current local favorites' Trakt IDs
        val localFavoriteTraktIds =
            traktDao.getAllFavoriteShowTraktIds() // You'll need this DAO method: fun getAllFavoriteShowTraktIds(): List<Int>
        val showsToDeleteTraktIds = localFavoriteTraktIds.filter { it !in showsFromNetworkTraktIds }

        if (showsToDeleteTraktIds.isNotEmpty()) {
            traktDao.deleteFavoriteShowsByTraktIds(showsToDeleteTraktIds)
            Timber.d("Deleted ${showsToDeleteTraktIds.size} shows from local favorites as they are no longer in the Trakt list.")
        }

        // 2. Insert or Update shows from the network
        if (processedDbShows.isNotEmpty()) {
            traktDao.insertAllFavoriteShows(*processedDbShows.toTypedArray())
            Timber.d("Upserted ${processedDbShows.size} favorite shows from Trakt list after image processing.")
        } else if (customListItemsResponse.isEmpty()) {
            // If the Trakt list was empty and we didn't delete anything above (because local was already empty)
            Timber.d("No favorite shows to insert/update as the Trakt list is empty.")
        } else {
            Timber.d(
                "No favorite shows required updating or inserting after processing Trakt list items (possibly all images were already present).",
            )
        }
    }

    override suspend fun clearFavorites() {
        withContext(Dispatchers.IO) {
            _isLoadingFavoriteShows.value = true
            try {
                traktDao.deleteAllFavoriteShows()
                upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
                _favoriteShow.value = null
                Timber.d("Local favorites cleared.")
            } catch (e: Exception) {
                handleGenericException(e, "Failed to clear local favorites")
                _favoriteShowsError.value = e.localizedMessage ?: "Failed to clear local favorites."
            } finally {
                _isLoadingFavoriteShows.value = false
            }
        }
    }

    override suspend fun checkIfShowIsFavorite(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            _favoriteShow.value = null
            return
        }
        withContext(Dispatchers.IO) {
            try {
                val show = traktDao.getFavoriteShow(imdbID)
                _favoriteShow.value = show?.asDomainModel()
            } catch (e: Exception) {
                handleGenericException(e, "Failed to check if show is favorite")
                _favoriteShow.value = null
            }
        }
    }

    override suspend fun addShowToList(
        imdbID: String?,
        token: String?,
    ) {
        if (imdbID.isNullOrEmpty() || token.isNullOrEmpty()) {
            logTraktException("Cannot add show to list: imdbID or token is null or empty.")
            _favoriteShowsError.value = "IMDb ID or token is invalid."
            return
        }
        val result = addShowToFavorites(imdbID, token)
        if (result.isFailure) {
            Timber.w("addShowToList (legacy) failed for IMDb $imdbID: ${result.exceptionOrNull()?.message}")
        } else {
            Timber.d("addShowToList (legacy) succeeded for IMDb $imdbID.")
        }
    }

    override suspend fun addShowToFavorites(
        imdbId: String,
        token: String,
    ): Result<Unit> {
        if (imdbId.isEmpty() || token.isEmpty()) {
            val message = "Cannot add show to favorites: IMDb ID or token is empty."
            logTraktException(message)
            _favoriteShowsError.value = message
            return Result.failure(IllegalArgumentException(message))
        }

        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"

                // 1. Get Trakt Show ID and other details by IMDb ID
                val showSummaryResponse =
                    try {
                        traktService.getShowInfoAsync(imdbID = imdbId)
                            .await()
                    } catch (e: Exception) {
                        val notFoundMessage =
                            "Show with IMDb ID $imdbId not found on Trakt during initial ID lookup."
                        Timber.w(e, notFoundMessage)
                        _favoriteShowsError.value = notFoundMessage
                        return@withContext Result.failure(Exception(notFoundMessage, e))
                    }

                val traktShowId = showSummaryResponse.ids?.trakt
                if (traktShowId == null) {
                    val noTraktIdMessage =
                        "Could not retrieve Trakt ID for show with IMDb ID $imdbId from Trakt summary."
                    Timber.w(noTraktIdMessage)
                    _favoriteShowsError.value = noTraktIdMessage
                    return@withContext Result.failure(Exception(noTraktIdMessage))
                }

                // 2. Get User Slug
                val userSettings = traktService.getUserSettingsAsync(bearerToken).await()
                val userSlug =
                    userSettings.user?.ids?.slug
                        ?: return@withContext Result.failure<Unit>(
                            Exception("User slug not found in Trakt settings for adding show.").also {
                                logTraktException(it.message ?: "User slug not found")
                                _favoriteShowsError.value = it.message
                            },
                        )

                // 3. Get or Create "UpnextApp Favorites" List ID (Slug)
                val listIdSlugResult =
                    getOrCreateFavoritesListId(token, userSlug)
                if (listIdSlugResult.isFailure) {
                    val error =
                        listIdSlugResult.exceptionOrNull()
                            ?: Exception("Failed to get/create favorites list ID.")
                    _favoriteShowsError.value = error.message
                    return@withContext Result.failure(error)
                }
                val favoritesListSlug = listIdSlugResult.getOrThrow()

                // 4. Prepare request to add show to the Trakt List
                val addRequest =
                    NetworkTraktAddShowToListRequest(
                        shows =
                            listOf(
                                NetworkTraktAddShowToListRequestShow(
                                    ids =
                                        NetworkTraktAddShowToListRequestShowIds(
                                            trakt = traktShowId,
                                        ),
                                ),
                            ),
                    )

                Timber.d("Attempting to add show (IMDb: $imdbId, Trakt: $traktShowId) to list $favoritesListSlug for user $userSlug")
                val addResponse =
                    traktService.addShowToCustomListAsync(
                        token = bearerToken,
                        userSlug = userSlug,
                        traktId = favoritesListSlug, // This is the slug of the "UpnextApp Favorites" list
                        networkTraktAddShowToListRequest = addRequest,
                    ).await()

                // 5. Process Trakt's response & Update Local DB
                val showSuccessfullyAdded = addResponse.added.shows == 1
                val showAlreadyExistsOnList = addResponse.existing.shows == 1

                if (showSuccessfullyAdded || showAlreadyExistsOnList) {
                    Timber.d(
                        "Show (IMDb: $imdbId, Trakt: $traktShowId) successfully added or already present on Trakt list $favoritesListSlug. " +
                            "Added: ${addResponse.added.shows}, Existing: ${addResponse.existing.shows}, Not Found (on Trakt): ${addResponse.not_found.shows.size}",
                    )

                    // Construct DatabaseFavoriteShows from the showSummaryResponse and fetched images
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
                            mediumImageUrl = null, // Placeholder, will be updated by getImages
                            originalImageUrl = null, // Placeholder, will be updated by getImages
                            tvMazeID = null, // Placeholder, will be updated by getImages
                        )

                    // Fetch images for this newly added favorite
                    // Assuming getImages returns (tvMazeId: Int?, poster: String?, heroImage: String?)
                    val (tvMazeIdResult, poster, heroImage) = getImages(imdbId)
                    dbShow =
                        dbShow.copy(
                            tvMazeID = tvMazeIdResult,
                            originalImageUrl = poster,
                            mediumImageUrl = heroImage,
                        )

                    traktDao.insertFavoriteShow(dbShow)
                    Timber.d("Show (IMDb: $imdbId) saved/updated in local favorites DB.")

                    checkIfShowIsFavorite(imdbId)
                    logTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS)
                    Result.success(Unit)
                } else if (addResponse.not_found.shows.any { notFoundItem ->
                        notFoundItem.ids.trakt == traktShowId
                    }
                ) {
                    val notFoundMessage =
                        "Show with IMDb ID $imdbId (Trakt ID: $traktShowId) reported as 'not found' by Trakt during add to list. This is unexpected."
                    Timber.w(notFoundMessage)
                    _favoriteShowsError.value = notFoundMessage
                    Result.failure(Exception(notFoundMessage))
                } else {
                    val unexpectedResponseMessage =
                        "Show not added/found for unknown reason. Response: Added: ${addResponse.added.shows}, Existing: ${addResponse.existing.shows}, Not Found: ${addResponse.not_found.shows?.size}"
                    Timber.w("$unexpectedResponseMessage \nFull Response: $addResponse")
                    _favoriteShowsError.value = unexpectedResponseMessage
                    Result.failure(Exception(unexpectedResponseMessage))
                }
            } catch (e: HttpException) {
                val errorBody =
                    e.response()?.errorBody()
                        ?.stringSuspending()
                val detailedMessage =
                    "HTTP error adding show (IMDb: $imdbId) to favorites: ${e.code()} - $errorBody"
                logTraktException(detailedMessage, e)
                _favoriteShowsError.value =
                    parseTraktApiError(
                        errorBody,
                        defaultMessage = "Server error adding show to favorites.",
                        moshi = moshi,
                    )
                Result.failure(Exception(detailedMessage, e))
            } catch (e: Exception) {
                val errorMessage =
                    "Error adding show (IMDb: $imdbId) to favorites list: ${e.javaClass.simpleName}"
                handleGenericException(e, "Error adding show (IMDb: $imdbId) to favorites list")
                _favoriteShowsError.value = e.localizedMessage ?: errorMessage
                Result.failure(e)
            } finally {
                _isLoadingFavoriteShows.value = false
            }
        }
    }

    /**
     * Reads the ResponseBody to a String in a non-blocking way.
     * This should be called from a coroutine.
     *
     * @param charset The charset to use for decoding the string, defaults to UTF-8.
     * @return The string content of the ResponseBody, or null if the ResponseBody is null.
     */
    suspend fun ResponseBody?.stringSuspending(charset: Charset = Charsets.UTF_8): String? {
        return this?.source()?.use { source ->
            withContext(Dispatchers.IO) {
                source.request(Long.MAX_VALUE)
                source.buffer.clone().readString(charset)
            }
        }
    }

    /**
     * Parses a JSON error string from Trakt API into a more readable message.
     *
     * @param errorBodyString The JSON string from the error response body.
     * @param defaultMessage A default message to return if parsing fails or the body is empty.
     * @param moshi An instance of Moshi for JSON parsing.
     * @return A user-friendly error message.
     */
    fun parseTraktApiError(
        errorBodyString: String?,
        defaultMessage: String,
        moshi: Moshi,
    ): String {
        if (errorBodyString.isNullOrBlank()) {
            return defaultMessage
        }

        return try {
            val errorAdapter = moshi.adapter(TraktErrorResponse::class.java)
            val traktError = errorAdapter.fromJson(errorBodyString)

            val description = traktError?.errorDescription
            val errorType = traktError?.error

            if (!description.isNullOrBlank()) {
                description
            } else if (!errorType.isNullOrBlank()) {
                // Capitalize the first letter of errorType and replace underscores
                errorType.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    .replace("_", " ")
            } else {
                Timber.w("Trakt error body did not match expected TraktErrorResponse structure: $errorBodyString")
                defaultMessage // Or return errorBodyString if you want to show the raw JSON snippet
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse Trakt API error JSON: $errorBodyString")
            // If parsing fails, it might be a non-JSON error or a different structure
            // Return the original body if it's somewhat readable, or the default
            if (errorBodyString.length < 100) errorBodyString else defaultMessage
        }
    }

    override suspend fun removeShowFromList(
        traktId: Int?,
        imdbID: String?,
        token: String?,
    ) {
        if (traktId == null || imdbID.isNullOrEmpty() || token.isNullOrEmpty()) {
            logTraktException("Cannot remove show: traktId, imdbID, or token is invalid.")
            _favoriteShowsError.value = "Cannot remove show: Invalid parameters."
            return
        }
        val result =
            removeShowFromFavorites(
                traktId,
                imdbID,
                token,
            ) // imdbID used for local DB update after successful removal
        if (result.isFailure) {
            Timber.w("removeShowFromList (legacy) failed for Trakt ID $traktId: ${result.exceptionOrNull()?.message}")
        } else {
            Timber.d("removeShowFromList (legacy) succeeded for Trakt ID $traktId.")
        }
    }

    override suspend fun removeShowFromFavorites(
        traktId: Int,
        imdbId: String,
        token: String,
    ): Result<Unit> {
        if (token.isEmpty()) {
            val message = "Cannot remove show from favorites: token is empty."
            logTraktException(message)
            _favoriteShowsError.value = message
            return Result.failure(IllegalArgumentException(message))
        }
        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null

        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"

                // 1. Get User Slug
                val userSettings = traktService.getUserSettingsAsync(bearerToken).await()
                val userSlug =
                    userSettings.user?.ids?.slug
                        ?: return@withContext Result.failure<Unit>(
                            Exception("User slug not found in Trakt settings for removal.").also {
                                logTraktException(it.message ?: "User slug not found")
                                _favoriteShowsError.value = it.message
                            },
                        )

                // 2. Get "UpnextApp Favorites" List ID (Slug)
                // We need the list's slug to remove items from it.
                val listIdSlugResult =
                    getOrCreateFavoritesListId(
                        token,
                        userSlug,
                    ) // false: don't create if not found for removal
                if (listIdSlugResult.isFailure) {
                    val error =
                        listIdSlugResult.exceptionOrNull()
                            ?: Exception("Failed to get favorites list ID for removal.")
                    _favoriteShowsError.value = error.message
                    return@withContext Result.failure(error)
                }
                val favoritesListSlug =
                    listIdSlugResult.getOrThrow()

                // 3. Prepare request to remove show from the Trakt List
                // Trakt API removes items based on their Trakt IDs.
                val request =
                    NetworkTraktRemoveShowFromListRequest(
                        shows =
                            listOf(
                                NetworkTraktRemoveShowFromListRequestShow(
                                    ids =
                                        NetworkTraktRemoveShowFromListRequestShowIds(
                                            trakt = traktId,
                                        ),
                                ),
                            ),
                    )

                Timber.d("Attempting to remove show Trakt ID $traktId from list $favoritesListSlug for user $userSlug")
                val removeResponse =
                    traktService.removeShowFromCustomListAsync(
                        token = bearerToken,
                        userSlug = userSlug,
                        traktId = favoritesListSlug, // This is the slug of the "UpnextApp Favorites" list
                        networkTraktRemoveShowFromListRequest = request,
                    ).await()

                Timber.d(
                    "Remove show response: Deleted: ${removeResponse.deleted.shows}, Not Found: ${removeResponse.not_found.shows?.size}",
                )

                val showSuccessfullyRemovedOnTrakt = removeResponse.deleted.shows == 1
                val showNotFoundOnTraktList =
                    removeResponse.not_found.shows?.any { it.ids.trakt == traktId } == true ||
                        removeResponse.not_found.shows?.any { it.ids.trakt == traktId } == true

                if (showSuccessfullyRemovedOnTrakt || showNotFoundOnTraktList) {
                    Timber.d("Show with Trakt ID $traktId successfully removed or confirmed not on Trakt list $favoritesListSlug.")
                    // 4. Remove from local DB using Trakt ID
                    val deletedRows =
                        traktDao.deleteFavoriteShowByTraktId(traktId)
                    if (deletedRows > 0) {
                        Timber.d("Show with Trakt ID $traktId also removed from local DB.")
                    } else {
                        Timber.w("Show with Trakt ID $traktId was not found in local DB for removal, or already removed.")
                    }

                    checkIfShowIsFavorite(imdbId)
                    logTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS)

                    if (showNotFoundOnTraktList && !showSuccessfullyRemovedOnTrakt) {
                        _favoriteShowsError.value = null
                    }
                    Result.success(Unit)
                } else {
                    // This case means the show wasn't deleted for some other reason (e.g., Trakt API error not caught above)
                    // or the response was unexpected.
                    val unexpectedRemoveMsg =
                        "Show Trakt ID $traktId not deleted from Trakt list $favoritesListSlug for an unknown reason. Full response: $removeResponse"
                    Timber.w(unexpectedRemoveMsg)
                    _favoriteShowsError.value =
                        "Failed to remove show from Trakt. Reason: API reported not deleted and not 'not found'."
                    Result.failure(Exception(unexpectedRemoveMsg))
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.stringSuspending()
                val detailedMessage =
                    "HTTP error removing show (TraktID: $traktId, IMDb: $imdbId) from favorites: ${e.code()} - $errorBody"
                logTraktException(detailedMessage, e)
                _favoriteShowsError.value =
                    parseTraktApiError(
                        errorBody,
                        defaultMessage = "Failed to remove show due to server error.",
                        moshi = moshi,
                    )
                Result.failure(Exception(detailedMessage, e))
            } catch (e: Exception) {
                val errorMessage =
                    "Error removing show (TraktID: $traktId, IMDb: $imdbId) from favorites list"
                handleGenericException(
                    e,
                    errorMessage,
                ) // This logs and potentially formats the message
                _favoriteShowsError.value = e.localizedMessage ?: errorMessage
                Result.failure(e)
            } finally {
                _isLoadingFavoriteShows.value = false
            }
        }
    }

    override suspend fun getTraktShowRating(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            logTraktException("Cannot get show rating: imdbID is null or empty.")
            _traktShowRating.value = null
            return
        }
    }

    /**
     * Refreshes the list of trending shows from the Trakt API and updates the local database.
     *
     * This function will first check if a refresh is necessary based on whether the local data
     * is empty, if it's due for a scheduled update (checked daily by default), or if a
     * `forceRefresh` is explicitly requested.
     *
     * If an update is needed and not already in progress (unless forced):
     * 1. Sets an internal loading state to `true`.
     * 2. Fetches trending shows from the Trakt API via [TraktService.getTrendingShowsAsync].
     * 3. If the API response is empty, the local table for trending shows is cleared.
     * 4. Otherwise, the network response is mapped to database models:
     *     a. Existing local shows are updated with new metadata from the network.
     *     b. New shows from the network are added.
     *     c. Shows present locally but not in the network response are deleted from the local database
     *        to maintain synchronization.
     * 5. For any new shows or existing shows that are missing image URLs (or if `forceRefresh` is true),
     *    it attempts to fetch poster and hero images using their IMDb ID via the
     *    [BaseRepository.getImages] (which typically calls a service like TvMaze).
     * 6. Updated show information (including new image URLs) is persisted to the local database
     *    using [TraktDao.insertAllTraktTrending].
     * 7. The timestamp for the last update of the trending shows table is recorded.
     * 8. Finally, the internal loading state is set to `false`.
     *
     * Any exceptions during the process are logged, and the loading state is ensured to be `false`
     * in a `finally` block.
     *
     * @param forceRefresh If `true`, the refresh will be performed regardless of whether the
     * data is considered stale or if another refresh is already in progress. Defaults to `false`.
     */
    override suspend fun refreshTraktTrendingShows(forceRefresh: Boolean) {
        if (_isLoadingTraktTrending.value && !forceRefresh) {
            Timber.d("refreshTraktTrendingShows: Already in progress, and not a force refresh.")
            return
        }

        val tableName = DatabaseTables.TABLE_TRAKT_TRENDING.tableName
        val isEmpty =
            traktDao.checkIfTrendingShowsIsEmpty()
        val needsUpdate = isEmpty || isUpdateNeededByDay(tableName) || forceRefresh

        if (!needsUpdate) {
            Timber.d("refreshTraktTrendingShows: Update not needed. Empty: $isEmpty, Force: $forceRefresh.")
            _isLoadingTraktTrending.value = false
            return
        }

        _isLoadingTraktTrending.value = true
        Timber.d(
            "refreshTraktTrendingShows: STARTING. IsEmpty: $isEmpty, NeedsUpdate: ${
                isUpdateNeededByDay(
                    tableName,
                )
            }, Force: $forceRefresh",
        )

        try {
            withContext(Dispatchers.IO) {
                val networkTrendingShowsResponse =
                    traktService.getTrendingShowsAsync().await()
                if (networkTrendingShowsResponse.isEmpty()) {
                    Timber.d("refreshTraktTrendingShows: No trending shows from Trakt API. Clearing local table.")
                    traktDao.clearTrendingShows()
                    logTableUpdateTimestamp(tableName)
                    _isLoadingTraktTrending.value = false
                    return@withContext
                }

                val newShowDatabaseModels: List<DatabaseTraktTrendingShows> =
                    networkTrendingShowsResponse.mapNotNull { responseItem: NetworkTraktTrendingShowsResponseItem ->
                        responseItem.show.asDatabaseModel()
                    }

                if (newShowDatabaseModels.isEmpty()) {
                    Timber.w("refreshTraktTrendingShows: All network items failed to map to database models or responseItem.show was null.")
                    _isLoadingTraktTrending.value = false
                    return@withContext
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
                    Timber.d("refreshTraktTrendingShows: Upserted ${showsToUpsert.size} shows.")
                }

                val showsToDelete = localShowsList.filter { it.id !in networkShowIds }
                if (showsToDelete.isNotEmpty()) {
                    traktDao.deleteSpecificTrendingShows(showsToDelete.map { it.id })
                    Timber.d("refreshTraktTrendingShows: Deleted ${showsToDelete.size} old shows.")
                }

                // Image fetching part
                if (showsMissingImages.isNotEmpty()) {
                    val showsToUpdateWithFetchedImages =
                        mutableListOf<DatabaseTraktTrendingShows>()
                    for (showNeedingImage in showsMissingImages) {
                        showNeedingImage.imdbID?.let { imdbId ->
                            Timber.d("Fetching images for trending show: '${showNeedingImage.title}' (IMDb: $imdbId)")
                            val (newTvMazeId, newPoster, newHeroImage) = getImages(imdbId)

                            var imageUpdatedShow = showNeedingImage
                            var changed = false

                            if (newPoster != null && newPoster != imageUpdatedShow.original_image_url) {
                                imageUpdatedShow =
                                    imageUpdatedShow.copy(original_image_url = newPoster)
                                changed = true
                            }
                            if (newHeroImage != null && newHeroImage != imageUpdatedShow.medium_image_url) {
                                imageUpdatedShow =
                                    imageUpdatedShow.copy(medium_image_url = newHeroImage)
                                changed = true
                            }
                            if (newTvMazeId != null && newTvMazeId != imageUpdatedShow.tvMazeID) {
                                imageUpdatedShow =
                                    imageUpdatedShow.copy(tvMazeID = newTvMazeId)
                                changed = true
                            }

                            if (changed) {
                                showsToUpdateWithFetchedImages.add(imageUpdatedShow)
                            }
                        }
                            ?: Timber.w("Trending show '${showNeedingImage.title}' (TraktID: ${showNeedingImage.id}) missing imdbID for image fetch.")
                    }

                    if (showsToUpdateWithFetchedImages.isNotEmpty()) {
                        traktDao.insertAllTraktTrending(*showsToUpdateWithFetchedImages.toTypedArray())
                        Timber.d("refreshTraktTrendingShows: Updated images for ${showsToUpdateWithFetchedImages.size} shows.")
                    }
                } else {
                    Timber.d("refreshTraktTrendingShows: No shows required image fetching/updating.")
                }

                logTableUpdateTimestamp(tableName)
                Timber.d("refreshTraktTrendingShows: COMPLETED SUCCESSFULLY.")
            }
        } catch (e: Exception) {
            Timber.e(e, "refreshTraktTrendingShows: FAILED.")
            firebaseCrashlytics.recordException(e)
        } finally {
            _isLoadingTraktTrending.value = false
            Timber.d("refreshTraktTrendingShows: FINISHED.")
        }
    }

    /**
     * Refreshes the local cache of popular Trakt shows.
     *
     * This function performs the following operations:
     * 1. Checks if a refresh is currently in progress (unless [forceRefresh] is true).
     * 2. Determines if an update is needed based on whether the local table is empty,
     *    if the data is stale (older than a day), or if [forceRefresh] is true.
     * 3. If an update is needed:
     *    a. Sets an internal loading state to true.
     *    b. Fetches the list of popular shows from the Trakt API.
     *    c. If the API returns no shows, clears the local popular shows table.
     *    d. Otherwise, maps the network response items to [DatabaseTraktPopularShow] entities.
     *    e. Compares the fetched shows with the existing shows in the local database:
     *        i. Identifies new shows to insert and existing shows to update.
     *        ii. Preserves existing image URLs and TVMaze IDs for shows already in the database
     *           unless a [forceRefresh] is requested or image data is missing.
     *        iii. Adds shows that are missing image URLs, missing TVMaze IDs, or are part of a
     *            [forceRefresh] to a separate list for image fetching.
     *    f. Upserts the new/updated show data (without new images yet) into the local database.
     *    g. Identifies and deletes any shows from the local database that are no longer present
     *       in the fetched popular list.
     *    h. For shows identified as needing image updates:
     *        i. Iterates through them and attempts to fetch poster and hero images (and TVMaze ID)
     *           using their IMDb ID via the `getImages` helper method.
     *        ii. If new image URLs or a new TVMaze ID are found and differ from existing ones,
     *            the local show entity is updated.
     *    i. Upserts the shows that had their image information updated.
     *    j. Logs the timestamp of the successful table update.
     * 4. Catches any exceptions during the process, logs them, and records them with Firebase Crashlytics.
     * 5. Resets the internal loading state to false in a `finally` block.
     *
     * All database and network operations are performed on an IO-optimized dispatcher.
     *
     * @param forceRefresh If true, the refresh will be performed regardless of whether the
     *                     data is considered stale or if another refresh is in progress. This also
     *                     triggers re-fetching of images for all shows.
     */
    override suspend fun refreshTraktPopularShows(forceRefresh: Boolean) {
        if (_isLoadingTraktPopular.value && !forceRefresh) {
            Timber.d(
                "refreshTraktPopularShows: Already in progress, " +
                    "and not a force refresh.",
            )
            return
        }

        val tableName = DatabaseTables.TABLE_TRAKT_POPULAR.tableName
        val isEmpty = traktDao.checkIfPopularShowsIsEmpty()
        val needsUpdate = isEmpty || isUpdateNeededByDay(tableName) || forceRefresh

        if (!needsUpdate) {
            Timber.d(
                "refreshTraktPopularShows: Update not needed. Empty: $isEmpty, " +
                    "Force: $forceRefresh. Last update was today.",
            )
            _isLoadingTraktPopular.value = false
            return
        }

        _isLoadingTraktPopular.value = true
        Timber.d(
            "refreshTraktPopularShows: STARTING. IsEmpty: $isEmpty, NeedsUpdateByDay: ${
                isUpdateNeededByDay(
                    tableName,
                )
            }, ForceRefresh: $forceRefresh",
        )

        try {
            withContext(Dispatchers.IO) {
                // 1. Fetch current popular shows from Trakt API
                val networkPopularShows = traktService.getPopularShowsAsync().await()
                if (networkPopularShows.isEmpty()) {
                    Timber.d("refreshTraktPopularShows: No popular shows from Trakt API. Clearing local table.")
                    traktDao.clearPopularShows()
                    logTableUpdateTimestamp(tableName)
                    _isLoadingTraktPopular.value = false
                    return@withContext
                }

                val networkShowIds = networkPopularShows.map { it.ids.trakt }.toSet()
                val newShowDatabaseModels = networkPopularShows.map { it.asDatabaseModel() }

                // 2. Get current shows from the local database
                val localShowsList =
                    traktDao.getTraktPopularRaw() // Fetches all current shows
                val localShowsMap = localShowsList.associateBy { it.id }

                val showsToUpsert = mutableListOf<DatabaseTraktPopularShows>()
                val showsMissingImages = mutableListOf<DatabaseTraktPopularShows>()

                for (networkShowModel in newShowDatabaseModels) {
                    val existingLocalShow = localShowsMap[networkShowModel.id]

                    if (existingLocalShow != null) {
                        // Show exists locally. Update its Trakt data but preserve existing
                        // images for now. Only copy Trakt-specific fields. Images
                        // will be handled later.
                        val updatedShow =
                            existingLocalShow.copy(
                                title = networkShowModel.title,
                                year = networkShowModel.year,
                                slug = networkShowModel.slug,
                                tmdbID = networkShowModel.tmdbID,
                                traktID = networkShowModel.traktID,
                                tvdbID = networkShowModel.tvdbID,
                                // not copying medium_image_url, original_image_url, tvMazeID
                                // from networkShowModel as networkShowModel has them as null
                                // initially from asDatabaseModel()
                            )
                        showsToUpsert.add(updatedShow)

                        // Check if images are missing or if forceRefresh (which implies refreshing images too)
                        if (existingLocalShow.original_image_url.isNullOrEmpty() ||
                            existingLocalShow.medium_image_url.isNullOrEmpty() ||
                            forceRefresh // If forcing a full refresh, re-fetch images
                        ) {
                            showsMissingImages.add(updatedShow) // Add the updated version
                        }
                    } else {
                        showsToUpsert.add(networkShowModel)
                        showsMissingImages.add(networkShowModel)
                    }
                }

                // 3. Upsert (Insert or Update) the shows based on Trakt data
                if (showsToUpsert.isNotEmpty()) {
                    traktDao.insertAllTraktPopular(*showsToUpsert.toTypedArray())
                    Timber.d("refreshTraktPopularShows: Upserted ${showsToUpsert.size} shows with latest Trakt data.")
                }

                // 4. Remove shows from DB that are no longer in the popular list from Trakt
                val showsToDelete = localShowsList.filter { it.id !in networkShowIds }
                if (showsToDelete.isNotEmpty()) {
                    traktDao.deleteSpecificPopularShows(showsToDelete.map { it.id }) // New DAO method
                    Timber.d("refreshTraktPopularShows: Deleted ${showsToDelete.size} old popular shows.")
                }

                // 5. Fetch and update images ONLY for shows that need them
                if (showsMissingImages.isNotEmpty()) {
                    val showsToUpdateWithFetchedImages =
                        mutableListOf<DatabaseTraktPopularShows>()
                    // It's important to read the shows again from DB after upsert,
                    // or ensure showsMissingImages contains objects that Room can update by PK.
                    // For simplicity, re-fetching the specific items that need images, or use
                    // the items in showsMissingImages if they are suitable for being passed
                    // to 'copy()' and then to insertAll for update.

                    // use the items already in showsMissingImages, assuming their
                    // PK (id) is correct.
                    for (showNeedingImage in showsMissingImages) {
                        showNeedingImage.imdbID?.let { imdbId ->
                            Timber.d("Fetching images for popular show (missing/forced): '${showNeedingImage.title}' (IMDb: $imdbId)")
                            val (newTvMazeId, newPoster, newHeroImage) = getImages(imdbId)

                            var imageUpdatedShow =
                                showNeedingImage
                            var changed = false

                            if (newPoster != null && newPoster != imageUpdatedShow.original_image_url) {
                                imageUpdatedShow =
                                    imageUpdatedShow.copy(original_image_url = newPoster)
                                changed = true
                            }
                            if (newHeroImage != null && newHeroImage != imageUpdatedShow.medium_image_url) {
                                imageUpdatedShow =
                                    imageUpdatedShow.copy(medium_image_url = newHeroImage)
                                changed = true
                            }
                            // Only update tvMazeID if it's new, getImages provides it
                            if (newTvMazeId != null && newTvMazeId != imageUpdatedShow.tvMazeID) {
                                imageUpdatedShow =
                                    imageUpdatedShow.copy(tvMazeID = newTvMazeId)
                                changed = true
                            }

                            if (changed) {
                                showsToUpdateWithFetchedImages.add(imageUpdatedShow)
                            }
                        }
                            ?: Timber.w("Popular show '${showNeedingImage.title}' (TraktID: ${showNeedingImage.traktID}) missing imdbID for image fetch.")
                    }

                    if (showsToUpdateWithFetchedImages.isNotEmpty()) {
                        traktDao.insertAllTraktPopular(
                            *showsToUpdateWithFetchedImages.toTypedArray(),
                        ) // This will update existing rows by PK
                        Timber.d(
                            "refreshTraktPopularShows: Updated image information for ${showsToUpdateWithFetchedImages.size} popular shows.",
                        )
                    }
                } else {
                    Timber.d("refreshTraktPopularShows: No shows required image fetching/updating.")
                }

                logTableUpdateTimestamp(tableName)
                Timber.d("refreshTraktPopularShows: COMPLETED SUCCESSFULLY (sync and image updates).")
            }
        } catch (e: Exception) {
            Timber.e(e, "refreshTraktPopularShows: FAILED.")
            firebaseCrashlytics.recordException(e)
        } finally {
            _isLoadingTraktPopular.value = false
            Timber.d("refreshTraktPopularShows: FINISHED. isLoadingTraktPopular set to false.")
        }
    }

    /**
     * Refreshes the list of most anticipated shows from the Trakt API and updates the local database.
     *
     * This function checks if a refresh is needed based on local data presence, a daily update
     * schedule, or if `forceRefresh` is true.
     *
     * If an update is warranted and not already in progress (unless forced):
     * 1. Sets an internal loading state to `true`.
     * 2. Fetches most anticipated shows from the Trakt API ([TraktService.getMostAnticipatedShowsAsync]).
     * 3. If the API response is empty, the local table for most anticipated shows is cleared.
     * 4. Otherwise, the network response is mapped to database models:
     *    a. Existing local shows are updated with new metadata from the Trakt API.
     *    b. New shows from the API are added to the local database.
     *    c. Shows present locally but not in the fresh API response are removed to maintain sync.
     * 5. For new shows, shows missing image URLs, or if `forceRefresh` is true, it fetches
     *    poster and hero images using their IMDb ID via [BaseRepository.getImages]
     *    (which typically calls a service like TvMaze).
     * 6. Updated show information, including any newly fetched image URLs, is persisted
     *    to the local database using [TraktDao.insertAllTraktMostAnticipated].
     * 7. Records the timestamp of this update for the most anticipated shows table.
     * 8. Ensures the internal loading state is set to `false` upon completion or error.
     *
     * Exceptions during the process are logged. The loading state is managed in a `finally`
     * block to ensure it's always reset.
     *
     * @param forceRefresh If `true`, the refresh is performed regardless of data staleness
     *                     or if another refresh is in progress. Defaults to `false`.
     */
    override suspend fun refreshTraktMostAnticipatedShows(forceRefresh: Boolean) {
        if (_isLoadingTraktMostAnticipated.value && !forceRefresh) {
            Timber.d("refreshTraktMostAnticipatedShows: Already in progress, and not a force refresh.")
            return
        }

        val tableName =
            DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName // Ensure this enum value exists
        val isEmpty =
            traktDao.checkIfMostAnticipatedShowsIsEmpty() // Ensure DAO method exists
        val needsUpdate = isEmpty || isUpdateNeededByDay(tableName) || forceRefresh

        if (!needsUpdate) {
            Timber.d("refreshTraktMostAnticipatedShows: Update not needed. Empty: $isEmpty, Force: $forceRefresh.")
            _isLoadingTraktMostAnticipated.value = false
            return
        }

        _isLoadingTraktMostAnticipated.value = true
        Timber.d(
            "refreshTraktMostAnticipatedShows: STARTING. IsEmpty: $isEmpty, NeedsUpdate: ${
                isUpdateNeededByDay(
                    tableName,
                )
            }, Force: $forceRefresh",
        )

        try {
            withContext(Dispatchers.IO) {
                val networkResponseItems: List<NetworkTraktMostAnticipatedResponseItem> =
                    traktService.getMostAnticipatedShowsAsync()
                        .await() // Verify your service method name

                if (networkResponseItems.isEmpty()) {
                    Timber.d("refreshTraktMostAnticipatedShows: No most anticipated shows from Trakt API. Clearing local table.")
                    traktDao.clearMostAnticipatedShows() // Ensure DAO method exists
                    logTableUpdateTimestamp(tableName)
                    _isLoadingTraktMostAnticipated.value = false
                    return@withContext
                }

                val newShowDatabaseModels: List<DatabaseTraktMostAnticipated> =
                    networkResponseItems.map { networkItem: NetworkTraktMostAnticipatedResponseItem ->
                        networkItem.show.asDatabaseModel()
                    }

                if (newShowDatabaseModels.isEmpty()) {
                    Timber.w("refreshTraktMostAnticipatedShows: All network items failed to map to database models (e.g. show was null).")
                    _isLoadingTraktMostAnticipated.value = false
                    return@withContext
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
                    traktDao.insertAllTraktMostAnticipated(*showsToUpsert.toTypedArray()) // Ensure DAO method exists
                    Timber.d("refreshTraktMostAnticipatedShows: Upserted ${showsToUpsert.size} shows.")
                }

                val showsToDelete = localShowsList.filter { it.id !in networkShowIds }
                if (showsToDelete.isNotEmpty()) {
                    traktDao.deleteSpecificMostAnticipatedShows(showsToDelete.map { it.id })
                    Timber.d("refreshTraktMostAnticipatedShows: Deleted ${showsToDelete.size} old shows.")
                }

                // Image fetching part
                if (showsMissingImages.isNotEmpty()) {
                    val showsToUpdateWithFetchedImages =
                        mutableListOf<DatabaseTraktMostAnticipated>()
                    for (showNeedingImage in showsMissingImages) {
                        showNeedingImage.imdbID?.let { imdbId ->
                            Timber.d("Fetching images for most anticipated show: '${showNeedingImage.title}' (IMDb: $imdbId)")
                            val (newTvMazeId, newPoster, newHeroImage) = getImages(imdbId)

                            var imageUpdatedShow = showNeedingImage
                            var changed = false

                            if (newPoster != null && newPoster != imageUpdatedShow.original_image_url) {
                                imageUpdatedShow =
                                    imageUpdatedShow.copy(original_image_url = newPoster)
                                changed = true
                            }
                            if (newHeroImage != null && newHeroImage != imageUpdatedShow.medium_image_url) {
                                imageUpdatedShow =
                                    imageUpdatedShow.copy(medium_image_url = newHeroImage)
                                changed = true
                            }
                            if (newTvMazeId != null && newTvMazeId != imageUpdatedShow.tvMazeID) {
                                imageUpdatedShow =
                                    imageUpdatedShow.copy(tvMazeID = newTvMazeId)
                                changed = true
                            }

                            if (changed) {
                                showsToUpdateWithFetchedImages.add(imageUpdatedShow)
                            }
                        }
                            ?: Timber.w("Most anticipated show '${showNeedingImage.title}' (TraktID: ${showNeedingImage.id}) missing imdbID for image fetch.")
                    }

                    if (showsToUpdateWithFetchedImages.isNotEmpty()) {
                        traktDao.insertAllTraktMostAnticipated(*showsToUpdateWithFetchedImages.toTypedArray())
                        Timber.d(
                            "refreshTraktMostAnticipatedShows: Updated images/tvMazeIDs for ${showsToUpdateWithFetchedImages.size} shows.",
                        )
                    }
                } else {
                    Timber.d("refreshTraktMostAnticipatedShows: No shows required image fetching/updating.")
                }

                logTableUpdateTimestamp(tableName)
                Timber.d("refreshTraktMostAnticipatedShows: COMPLETED SUCCESSFULLY.")
            }
        } catch (e: Exception) {
            Timber.e(e, "refreshTraktMostAnticipatedShows: FAILED.")
            firebaseCrashlytics.recordException(e)
        } finally {
            _isLoadingTraktMostAnticipated.value = false
            Timber.d("refreshTraktMostAnticipatedShows: FINISHED.")
        }
    }

    private suspend fun logTableUpdate(databaseTables: DatabaseTables) {
        Timber.d("Attempting to log table update for: ${databaseTables.tableName}")

        try {
            logTableUpdateTimestamp(databaseTables.tableName)
            Timber.d("logTableUpdateTimestamp finished for: ${databaseTables.tableName} in repositoryScope")
        } catch (e: Exception) {
            Timber.e(
                e,
                "Error in repositoryScope.launch while logging table update for ${databaseTables.tableName}",
            )
        }
    }

    /**
     * Attempts to check into a specific episode of a show on Trakt.
     *
     * This function performs the following steps:
     * 1. Validates the provided [token] and essential details from [showSeasonEpisode]
     *    (IMDB ID, season number, episode number).
     * 2. If an IMDB ID is provided, it performs an ID lookup via the Trakt API to find the
     *    corresponding Trakt Show ID. This is necessary as the check-in endpoint requires
     *    Trakt's internal show ID.
     * 3. Constructs a check-in request payload including the Trakt Show ID, season number,
     *    and episode number. Optional parameters like app version and date can also be included.
     * 4. Makes the check-in API call to Trakt.
     * 5. Emits the result of the check-in attempt through the [traktCheckInEvent] SharedFlow:
     *    - On successful check-in (HTTP 201), a [TraktCheckInStatus] with details of the
     *      check-in (show title, season/episode, watched time) and a success message is emitted.
     *    - If a check-in is already in progress for the same item or another item by the user
     *      (HTTP 409 Conflict), the error response is parsed to extract the `expires_at` time
     *      and other details. A [TraktCheckInStatus] with a relevant conflict message,
     *      season/episode info, and the expiry time is emitted.
     *    - For other HTTP errors (e.g., 401 Unauthorized, 404 Not Found), a [TraktCheckInStatus]
     *      with an appropriate error message is emitted.
     *    - For network errors or other unexpected exceptions, a generic error message is emitted.
     *
     * The loading state is managed via the `_isLoading` StateFlow, which is set to `true`
     * at the beginning of the operation and `false` in a `finally` block to ensure it's
     * always reset.
     *
     * All network operations are performed on the [Dispatchers.IO] context.
     * Exceptions and significant events are logged using [logTraktException].
     *
     * @param showSeasonEpisode The [ShowSeasonEpisode] object containing details of the
     *                          episode to check into. Must include `imdbID` (for Trakt ID lookup),
     *                          `season`, and `number`. The `name` field is used as a fallback
     *                          for show titles in error messages if not available from API responses.
     * @param token The Trakt OAuth access token for the authenticated user. If null or empty,
     *              the check-in attempt will fail immediately.
     * @see TraktCheckInStatus
     * @see traktCheckInEvent
     * @see NetworkTraktCheckInRequest
     * @see TraktConflictErrorResponse
     */
    override suspend fun checkInToShow(
        showSeasonEpisode: ShowSeasonEpisode,
        token: String?,
    ) {
        if (token.isNullOrEmpty()) {
            logTraktException("Cannot check-in to show: token is null or empty.")
            _traktCheckInEvent.tryEmit(TraktCheckInStatus(message = "Authentication token is missing."))
            return
        }

        val imdbId = showSeasonEpisode.imdbID
        val seasonNumber = showSeasonEpisode.season
        val episodeNumber = showSeasonEpisode.number

        if (imdbId.isNullOrEmpty()) {
            logTraktException("Cannot check-in: IMDB ID is missing from ShowSeasonEpisode.")
            _traktCheckInEvent.tryEmit(TraktCheckInStatus(message = "Show information (IMDB ID) is missing."))
            return
        }

        if (seasonNumber == null || episodeNumber == null) {
            val missingInfo = mutableListOf<String>()
            if (seasonNumber == null) missingInfo.add("Season Number")
            if (episodeNumber == null) missingInfo.add("Episode Number")
            val errorMsg =
                "Episode information incomplete: ${missingInfo.joinToString(", ")} missing."
            logTraktException(errorMsg)
            _traktCheckInEvent.tryEmit(TraktCheckInStatus(message = errorMsg))
            return
        }

        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Looking up Trakt ID for IMDB ID: $imdbId")
                val idLookupResponse =
                    traktService.idLookupAsync(idType = "imdb", id = imdbId).await()
                val traktShowId =
                    idLookupResponse.firstNotNullOfOrNull { result ->
                        if (result.type == "show" && result.show?.ids?.trakt != null) {
                            result.show.ids.trakt
                        } else {
                            null
                        }
                    }

                if (traktShowId == null) {
                    logTraktException("Could not find Trakt Show ID for IMDB ID: $imdbId")
                    _traktCheckInEvent.tryEmit(TraktCheckInStatus(message = "Could not find the show on Trakt using IMDB ID: $imdbId."))

                    return@withContext
                }
                Timber.d("Found Trakt Show ID: $traktShowId for IMDB ID: $imdbId")

                val bearerToken = "Bearer $token"
                val request =
                    NetworkTraktCheckInRequest(
                        show =
                            NetworkTraktCheckInRequestShow(
                                ids =
                                    NetworkTraktCheckInRequestShowIds(
                                        trakt = traktShowId,
                                    ),
                                title = null,
                                year = null,
                            ),
                        episode =
                            NetworkTraktCheckInRequestEpisode(
                                season = seasonNumber,
                                number = episodeNumber,
                            ),
                    )

                Timber.d("Attempting Trakt check-in: Show Trakt ID $traktShowId, S${seasonNumber}E$episodeNumber")
                val checkInApiResponse: NetworkTraktCheckInResponse =
                    traktService.checkInAsync(bearerToken, request).await()

                val domainStatus = checkInApiResponse.asDomainModel()
                _traktCheckInEvent.tryEmit(domainStatus)
                Timber.i("Trakt check-in successful: ${domainStatus.message}")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                logTraktException(
                    "Trakt check-in process HTTP error (Code: ${e.code()}): $errorBody",
                    e,
                )

                var errorStatus: TraktCheckInStatus
                if (e.code() == 409 && errorBody != null) {
                    try {
                        val conflictResponse = traktConflictErrorAdapter.fromJson(errorBody)
                        if (conflictResponse?.expiresAt != null) {
                            val showTitleFromConflict =
                                conflictResponse.show?.title
                                    ?: showSeasonEpisode.name // Prioritize conflict response
                            val conflictSeason =
                                conflictResponse.episode?.season ?: seasonNumber
                            val conflictEpisode =
                                conflictResponse.episode?.number ?: episodeNumber
                            errorStatus =
                                TraktCheckInStatus(
                                    season = conflictSeason,
                                    episode = conflictEpisode,
                                    checkInTime =
                                        DateUtils.getDisplayDateFromDateStamp(
                                            conflictResponse.expiresAt,
                                        )
                                            .toString(),
                                    message = "A check-in for ${showTitleFromConflict ?: "this item"} S${conflictSeason}E$conflictEpisode is already in progress. Expires at: ${
                                        DateUtils.getDisplayDateFromDateStamp(
                                            conflictResponse.expiresAt,
                                        )
                                    }.",
                                )
                        } else {
                            // 409 but no expires_at, could be another type of conflict (e.g., watching something else not this item)
                            errorStatus =
                                TraktCheckInStatus(
                                    season = seasonNumber, episode = episodeNumber,
                                    message = "Check-in conflict (Code 409). You might be watching something else, or the item is already watched. Details: $errorBody",
                                )
                        }
                    } catch (parseEx: Exception) {
                        logTraktException(
                            "Failed to parse Trakt 409 error body for check-in.",
                            parseEx,
                        )
                        errorStatus =
                            TraktCheckInStatus(
                                season = seasonNumber, episode = episodeNumber,
                                message = "Check-in conflict (Code 409), unable to parse details. Raw: $errorBody",
                            )
                    }
                } else {
                    val httpErrorMessage =
                        when (e.code()) {
                            400 -> "Bad request (Code 400). Details: $errorBody"
                            401 -> "Unauthorized. Check Trakt token (Code 401). Details: $errorBody"
                            403 -> "Forbidden. You may not have permission for this action or item (Code 403). Details: $errorBody"
                            404 -> "Show or episode not found on Trakt (Code 404). Details: $errorBody"
                            412 -> "Precondition Failed - Typically related to user settings or watched status (Code 412). Details: $errorBody"
                            429 -> "Rate limit exceeded. Please try again later (Code 429). Details: $errorBody"
                            500, 502, 503, 504 -> "Trakt server error (Code: ${e.code()}). Please try again later. Details: $errorBody"
                            else -> "Check-in process failed with HTTP error (Code: ${e.code()}). Details: $errorBody"
                        }
                    errorStatus =
                        TraktCheckInStatus(
                            season = seasonNumber, episode = episodeNumber,
                            message = httpErrorMessage,
                        )
                }
                _traktCheckInEvent.tryEmit(errorStatus)
            } catch (e: Exception) {
                // For non-HTTP exceptions (network issues, other parsing errors, etc.)
                logTraktException("Generic error during Trakt check-in process.", e)
                _traktCheckInEvent.tryEmit(
                    TraktCheckInStatus(
                        season = seasonNumber,
                        episode = episodeNumber,
                        message = "An unexpected error occurred: ${e.localizedMessage}",
                    ),
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        const val FAVORITES_LIST_NAME = "Upnext Favorites"
    }
}
