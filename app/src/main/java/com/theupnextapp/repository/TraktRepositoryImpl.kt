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
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.theupnextapp.BuildConfig
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
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
import com.theupnextapp.network.models.trakt.asDatabaseModel
import com.theupnextapp.network.models.trakt.asDomainModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

class TraktRepositoryImpl(
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    private val traktDao: TraktDao,
    private val traktService: TraktService,
    private val firebaseCrashlytics: FirebaseCrashlytics
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

    override val traktAccessToken: Flow<TraktAccessToken?> =
        traktDao.getTraktAccessData().map { it?.asDomainModel() }.flowOn(Dispatchers.IO)

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

    private val _traktShowRating = MutableStateFlow<TraktShowRating?>(null)
    override val traktShowRating: StateFlow<TraktShowRating?> = _traktShowRating.asStateFlow()

    private val _traktShowStats = MutableStateFlow<TraktShowStats?>(null)
    override val traktShowStats: StateFlow<TraktShowStats?> = _traktShowStats.asStateFlow()

    private val _favoriteShow = MutableStateFlow<TraktUserListItem?>(null)
    override val favoriteShow: StateFlow<TraktUserListItem?> = _favoriteShow.asStateFlow()

    private val _traktCheckInEvent = MutableSharedFlow<TraktCheckInStatus>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val traktConflictErrorAdapter = moshi.adapter(TraktConflictErrorResponse::class.java)

    override val traktCheckInEvent: SharedFlow<TraktCheckInStatus> =
        _traktCheckInEvent.asSharedFlow()

    override fun getTraktAccessTokenRaw(): DatabaseTraktAccess? = traktDao.getTraktAccessDataRaw()

    /**
     * Placeholder for your actual logging function.
     * This function should ideally log to Timber and/or a remote crash reporting service.
     */
    private fun logTraktException(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.e(throwable, "Trakt Error: $message")
            firebaseCrashlytics.recordException(throwable) // Example
        } else {
            Timber.e("Trakt Error: $message")
        }
    }

    private fun handleGenericException(e: Exception, messagePrefix: String = "An error occurred") {
        Timber.e(e, "$messagePrefix: ${e.message}")
        firebaseCrashlytics.recordException(e)
    }

    private suspend fun <T> safeApiCall(
        loadingStateFlow: MutableStateFlow<Boolean> = _isLoading,
        apiCall: suspend () -> T
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            loadingStateFlow.value = true
            try {
                Result.success(apiCall())
            } catch (e: Exception) {
                handleGenericException(e)
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
        safeApiCall {
            traktService.getShowStatsAsync(imdbID).await()
        }.onSuccess { stats ->
            _traktShowStats.value = stats.asDomainModel()
        }.onFailure {
            _traktShowStats.value = null // Clear on failure
            logTraktException("Failed to get Trakt show stats for $imdbID.", it)
        }
    }

    override suspend fun getTraktAccessToken(code: String?) {
        if (code.isNullOrEmpty()) {
            logTraktException("Attempted to get access token with null or empty code.")
            return
        }

        safeApiCall {
            val request = NetworkTraktAccessTokenRequest(
                code = code,
                client_id = BuildConfig.TRAKT_CLIENT_ID,
                client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                grant_type = "authorization_code"
            )
            val response = traktService.getAccessTokenAsync(request).await()
            // Clear old data before inserting new, ensuring only one active token record
            traktDao.deleteTraktAccessData()
            traktDao.insertAllTraktAccessData(response.asDatabaseModel())
        }.onFailure { error ->
            logTraktException("Failed to get Trakt access token.", error)
        }
    }

    override suspend fun revokeTraktAccessToken(traktAccessToken: TraktAccessToken) {
        val tokenToRevoke = traktAccessToken.access_token
        if (tokenToRevoke.isNullOrEmpty()) {
            logTraktException("Attempted to revoke access token with null or empty token string.")
            return
        }

        safeApiCall {
            val request = NetworkTraktRevokeAccessTokenRequest(
                client_id = BuildConfig.TRAKT_CLIENT_ID,
                client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                token = tokenToRevoke
            )
            traktService.revokeAccessTokenAsync(request).await()
            traktDao.deleteTraktAccessData() // Remove token data after successful revocation
        }.onFailure { error ->
            logTraktException("Failed to revoke Trakt access token.", error)
        }
    }

    override suspend fun getTraktAccessRefreshToken(refreshToken: String?) {
        if (refreshToken.isNullOrEmpty()) {
            logTraktException("Attempted to refresh access token with null or empty refresh token.")
            return
        }

        safeApiCall {
            val request = NetworkTraktAccessRefreshTokenRequest(
                refresh_token = refreshToken,
                client_id = BuildConfig.TRAKT_CLIENT_ID,
                client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                grant_type = "refresh_token"
            )
            val response = traktService.getAccessRefreshTokenAsync(request).await()
            traktDao.deleteTraktAccessData()
            traktDao.insertAllTraktAccessData(response.asDatabaseModel())
        }.onFailure { error ->
            logTraktException("Failed to refresh Trakt access token.", error)
        }
    }

    private suspend fun getOrCreateFavoritesListId(
        token: String,
        userSlug: String
    ): Result<String> {
        val bearerToken = "Bearer $token"
        return try {
            val userCustomLists =
                traktService.getUserCustomListsAsync(bearerToken, userSlug).await()
            val favoritesList = userCustomLists.find { it.name == FAVORITES_LIST_NAME }
            val slug = favoritesList?.ids?.slug ?: run {
                val createRequest = NetworkTraktCreateCustomListRequest(name = FAVORITES_LIST_NAME)
                val createdList =
                    traktService.createCustomListAsync(
                        token = bearerToken,
                        userSlug = userSlug,
                        createCustomListRequest = createRequest
                    ).await()
                createdList.ids?.slug
            }
            slug?.let { Result.success(it) }
                ?: Result.failure(Exception("Failed to get or create favorites list ID. Slug was null."))
        } catch (e: Exception) {
            handleGenericException(e)
            Result.failure(e)
        }
    }

    private suspend fun fetchAndStoreFavoriteShows(
        token: String,
        userSlug: String,
        favoritesListId: String
    ): Result<Unit> {
        val bearerToken = "Bearer $token"
        // This operation is critical for updating the local DB state.
        // It's wrapped in a Result to clearly signal success/failure to the caller.
        return try {
            val customListItemsResponse =
                traktService.getCustomListItemsAsync(
                    token = bearerToken,
                    userSlug = userSlug,
                    traktId = favoritesListId
                ).await()
            handleTraktUserListItemsResponse(customListItemsResponse)
            Result.success(Unit)
        } catch (e: Exception) {
            handleGenericException(e)
            Result.failure(e)
        }
    }

    override suspend fun refreshFavoriteShows(forceRefresh: Boolean, token: String?) {
        if (token.isNullOrEmpty()) {
            logTraktException("Cannot refresh favorite shows: token is null or empty.")
            return
        }

        if (!forceRefresh && !shouldUpdate(
                DatabaseTables.TABLE_FAVORITE_SHOWS,
                TableUpdateInterval.TRAKT_FAVORITE_SHOWS
            )
        ) {
            Timber.d("Skipping favorite shows refresh, not due for update.")
            return
        }

        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                // Fetch user settings first to get the user slug
                val userSettingsResult = safeApiCall(_isLoading) {
                    traktService.getUserSettingsAsync(bearerToken).await()
                }

                userSettingsResult.onSuccess { userSettings ->
                    userSettings.user?.ids?.slug?.let { userSlug ->
                        // Get or create the custom list ID for favorites
                        getOrCreateFavoritesListId(token, userSlug).onSuccess { listId ->
                            // Fetch items from the list and store them
                            fetchAndStoreFavoriteShows(token, userSlug, listId).onSuccess {
                                // After favorite shows are successfully refreshed from Trakt and stored,
                                // refresh their image information.
                                refreshImagesForFavoriteShowsInternal()
                                logTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS) // Log main favorites list update
                            }.onFailure { error ->
                                logTraktException(
                                    "Failed to fetch and store favorite shows after getting list ID.",
                                    error
                                )
                            }
                        }.onFailure { error ->
                            logTraktException("Failed to get or create favorites list ID.", error)
                        }
                    }
                        ?: logTraktException("User slug not found in Trakt settings. Cannot refresh favorites.")
                }.onFailure { error ->
                    logTraktException(
                        "Failed to get Trakt user settings. Cannot refresh favorites.",
                        error
                    )
                }
            } catch (e: Exception) {
                handleGenericException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun handleTraktUserListItemsResponse(customListItemsResponse: NetworkTraktUserListItemResponse?) {
        val shows = customListItemsResponse?.mapNotNull { item ->
            item.show?.ids?.imdb?.let { imdbId ->
                val (tvMazeId, poster, heroImage) = getImages(imdbId)
                item.show.originalImageUrl = poster
                item.show.mediumImageUrl = heroImage
                item.show.ids.tvMazeID = tvMazeId
                item.asDatabaseModel()
            }
        } ?: emptyList()

        // Replace all existing favorite shows with the new list from Trakt
        traktDao.deleteAllFavoriteShows()
        if (shows.isNotEmpty()) {
            traktDao.insertAllFavoriteShows(*shows.toTypedArray())
        }
    }

    override suspend fun clearFavorites() {
        withContext(Dispatchers.IO) {
            traktDao.deleteAllFavoriteShows()
            upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
            // Also clear the update log for favorite shows' image info if it exists
            upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
        }
    }

    override suspend fun checkIfShowIsFavorite(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            _favoriteShow.value = null // Emit null if no ID provided
            return
        }
        withContext(Dispatchers.IO) {
            val show = traktDao.getFavoriteShow(imdbID)
            _favoriteShow.value = show?.asDomainModel()
        }
    }

    override suspend fun addShowToList(imdbID: String?, token: String?) {
        if (imdbID.isNullOrEmpty() || token.isNullOrEmpty()) {
            logTraktException("Cannot add show to list: imdbID or token is null or empty.")
            return
        }
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val userSettingsResult =
                    safeApiCall { traktService.getUserSettingsAsync(bearerToken).await() }

                userSettingsResult.onSuccess { userSettings ->
                    val userSlug = userSettings.user?.ids?.slug
                    if (userSlug == null) {
                        logTraktException("User slug not found, cannot add show to list.")
                        _isLoading.value = false; return@withContext
                    }

                    getOrCreateFavoritesListId(token, userSlug).onSuccess { listId ->
                        val showInfoResponseResult =
                            safeApiCall { traktService.getShowInfoAsync(imdbID).await() }

                        showInfoResponseResult.onSuccess { showInfoResponse ->
                            showInfoResponse.ids?.trakt?.let { traktShowId ->
                                val request = NetworkTraktAddShowToListRequest(
                                    shows = listOf(
                                        NetworkTraktAddShowToListRequestShow(
                                            ids = NetworkTraktAddShowToListRequestShowIds(
                                                trakt = traktShowId
                                            )
                                        )
                                    )
                                )
                                val addResponseResult = safeApiCall {
                                    traktService.addShowToCustomListAsync(
                                        bearerToken,
                                        userSlug,
                                        listId,
                                        request
                                    ).await()
                                }

                                addResponseResult.onSuccess { addResponse ->
                                    if (addResponse.added.shows == 1) {
                                        // Refresh the local list and associated info after
                                        // successful addition
                                        fetchAndStoreFavoriteShows(
                                            token,
                                            userSlug,
                                            listId
                                        ).onSuccess {
                                            refreshImagesForFavoriteShowsInternal()
                                            checkIfShowIsFavorite(imdbID)
                                            logTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS)
                                        }
                                    } else {
                                        logTraktException("Show not added to list as per Trakt response: ${addResponse.not_found.shows} not found, ${addResponse.existing.shows} existing.")
                                    }
                                }.onFailure {
                                    logTraktException(
                                        "Failed to add show to Trakt custom list.",
                                        it
                                    )
                                }
                            } ?: logTraktException("Trakt show ID not found for IMDB ID $imdbID.")
                        }.onFailure {
                            logTraktException(
                                "Failed to get show info from Trakt for IMDB ID $imdbID.",
                                it
                            )
                        }
                    }.onFailure { logTraktException("Failed to get/create favorites list ID.", it) }
                }.onFailure { logTraktException("Failed to get user settings from Trakt.", it) }
            } catch (e: Exception) {
                handleGenericException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    override suspend fun removeShowFromList(traktId: Int?, imdbID: String?, token: String?) {
        if (traktId == null || imdbID.isNullOrEmpty() || token.isNullOrEmpty()) {
            logTraktException("Cannot remove show: traktId, imdbID, or token is invalid.")
            return
        }
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val userSettingsResult =
                    safeApiCall { traktService.getUserSettingsAsync(bearerToken).await() }

                userSettingsResult.onSuccess { userSettings ->
                    val userSlug = userSettings.user?.ids?.slug
                    if (userSlug == null) {
                        logTraktException("User slug not found, cannot remove show from list.")
                        _isLoading.value = false; return@withContext
                    }

                    getOrCreateFavoritesListId(token, userSlug).onSuccess { listId ->
                        val request = NetworkTraktRemoveShowFromListRequest(
                            shows = listOf(
                                NetworkTraktRemoveShowFromListRequestShow(
                                    ids = NetworkTraktRemoveShowFromListRequestShowIds(
                                        trakt = traktId
                                    )
                                )
                            )
                        )
                        val removeResponseResult = safeApiCall {
                            traktService.removeShowFromCustomListAsync(
                                bearerToken,
                                userSlug,
                                listId,
                                request
                            ).await()
                        }

                        removeResponseResult.onSuccess { removeResponse ->
                            if (removeResponse.deleted.shows == 1) {
                                // Refresh the local list after successful removal
                                fetchAndStoreFavoriteShows(token, userSlug, listId).onSuccess {
                                    checkIfShowIsFavorite(imdbID) // Update specific favorite status (will be null)
                                    logTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS)
                                }
                            } else {
                                logTraktException("Show not removed from list as per Trakt response: ${removeResponse.not_found.shows} not found.")
                            }
                        }.onFailure {
                            logTraktException(
                                "Failed to remove show from Trakt custom list.",
                                it
                            )
                        }
                    }.onFailure { logTraktException("Failed to get/create favorites list ID.", it) }
                }.onFailure { logTraktException("Failed to get user settings from Trakt.", it) }
            } catch (e: Exception) {
                handleGenericException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    override suspend fun refreshImagesForFavoriteShows(forceRefresh: Boolean) {
        if (!forceRefresh && !shouldUpdate(
                DatabaseTables.TABLE_FAVORITE_SHOWS,
                TableUpdateInterval.TRAKT_FAVORITE_SHOWS
            )
        ) {
            Timber.d("Skipping favorite shows image refresh, not due for update.")
            return
        }

        refreshImagesForFavoriteShowsInternal()
        _isLoading.value = false
    }

    private suspend fun refreshImagesForFavoriteShowsInternal() {
        withContext(Dispatchers.IO) {
            try {
                val favoriteShowsFromDb = traktDao.getFavoriteShowsRaw()
                if (favoriteShowsFromDb.isEmpty()) return@withContext

                val showsToUpdate = mutableListOf<DatabaseFavoriteShows>()

                for (showRecord in favoriteShowsFromDb) {
                    // Ensure IMDb ID is present for fetching images via BaseRepository.getImages
                    showRecord.imdbID?.let { imdbId ->
                        val (newTvMazeId, newPoster, newHeroImage) = getImages(imdbId)

                        var updated = false
                        var updatedShowRecord = showRecord.copy()

                        if (newPoster != null && newPoster != updatedShowRecord.originalImageUrl) {
                            updatedShowRecord = updatedShowRecord.copy(originalImageUrl = newPoster)
                            updated = true
                        }
                        if (newHeroImage != null && newHeroImage != updatedShowRecord.mediumImageUrl) {
                            updatedShowRecord =
                                updatedShowRecord.copy(mediumImageUrl = newHeroImage)
                            updated = true
                        }
                        if (newTvMazeId != null && newTvMazeId != updatedShowRecord.tvMazeID) {
                            updatedShowRecord = updatedShowRecord.copy(tvMazeID = newTvMazeId)
                            updated = true
                        }

                        if (updated) {
                            showsToUpdate.add(updatedShowRecord)
                        }
                    }
                        ?: Timber.w("Favorite show '${showRecord.title}' (TraktID: ${showRecord.traktID}) missing imdbID, cannot refresh images.")
                }

                if (showsToUpdate.isNotEmpty()) {
                    traktDao.insertAllFavoriteShows(*showsToUpdate.toTypedArray()) // Uses OnConflictStrategy.REPLACE
                    Timber.d("Updated image information for ${showsToUpdate.size} favorite shows.")
                    logTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS)
                }
            } catch (e: Exception) {
                handleGenericException(e)
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
            traktDao.checkIfTrendingShowsIsEmpty() // Example: Assuming this DAO method exists
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
                    tableName
                )
            }, Force: $forceRefresh"
        )

        try {
            withContext(Dispatchers.IO) {
                val networkTrendingShowsResponse = traktService.getTrendingShowsAsync().await()
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
                    _isLoadingTraktTrending.value = false // Ensure loading is false
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
                        val updatedShow = existingLocalShow.copy(
                            title = networkShowModel.title,
                            year = networkShowModel.year,
                            slug = networkShowModel.slug,
                            tmdbID = networkShowModel.tmdbID,
                            traktID = networkShowModel.traktID,
                            tvdbID = networkShowModel.tvdbID
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
                    // Ensure deleteSpecificTrendingShows takes a List<Int> (or whatever type your ID is)
                    traktDao.deleteSpecificTrendingShows(showsToDelete.map { it.id })
                    Timber.d("refreshTraktTrendingShows: Deleted ${showsToDelete.size} old shows.")
                }

                // Image fetching part
                if (showsMissingImages.isNotEmpty()) {
                    val showsToUpdateWithFetchedImages = mutableListOf<DatabaseTraktTrendingShows>()
                    for (showNeedingImage in showsMissingImages) { // showNeedingImage is DatabaseTraktTrendingShows
                        // Ensure DatabaseTraktTrendingShows has imdbID
                        showNeedingImage.imdbID?.let { imdbId ->
                            Timber.d("Fetching images for trending show: '${showNeedingImage.title}' (IMDb: $imdbId)")
                            val (newTvMazeId, newPoster, newHeroImage) = getImages(imdbId)

                            var imageUpdatedShow = showNeedingImage
                            var changed = false

                            // Ensure DatabaseTraktTrendingShows has these image URL and tvMazeID fields
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
                                imageUpdatedShow = imageUpdatedShow.copy(tvMazeID = newTvMazeId)
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
                        "and not a force refresh."
            )
            return
        }

        val tableName = DatabaseTables.TABLE_TRAKT_POPULAR.tableName
        val isEmpty = traktDao.checkIfPopularShowsIsEmpty()
        val needsUpdate = isEmpty || isUpdateNeededByDay(tableName) || forceRefresh

        if (!needsUpdate) {
            Timber.d(
                "refreshTraktPopularShows: Update not needed. Empty: $isEmpty, " +
                        "Force: $forceRefresh. Last update was today."
            )
            _isLoadingTraktPopular.value = false
            return
        }

        _isLoadingTraktPopular.value = true
        Timber.d(
            "refreshTraktPopularShows: STARTING. IsEmpty: $isEmpty, NeedsUpdateByDay: ${
                isUpdateNeededByDay(
                    tableName
                )
            }, ForceRefresh: $forceRefresh"
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
                val localShowsList = traktDao.getTraktPopularRaw() // Fetches all current shows
                val localShowsMap = localShowsList.associateBy { it.id }

                val showsToUpsert = mutableListOf<DatabaseTraktPopularShows>()
                val showsMissingImages = mutableListOf<DatabaseTraktPopularShows>()

                for (networkShowModel in newShowDatabaseModels) {
                    val existingLocalShow = localShowsMap[networkShowModel.id]

                    if (existingLocalShow != null) {
                        // Show exists locally. Update its Trakt data but preserve existing
                        // images for now. Only copy Trakt-specific fields. Images
                        // will be handled later.
                        val updatedShow = existingLocalShow.copy(
                            title = networkShowModel.title,
                            year = networkShowModel.year,
                            slug = networkShowModel.slug,
                            tmdbID = networkShowModel.tmdbID,
                            traktID = networkShowModel.traktID,
                            tvdbID = networkShowModel.tvdbID
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
                    val showsToUpdateWithFetchedImages = mutableListOf<DatabaseTraktPopularShows>()
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
                                imageUpdatedShow = imageUpdatedShow.copy(tvMazeID = newTvMazeId)
                                changed = true
                            }

                            if (changed) {
                                showsToUpdateWithFetchedImages.add(imageUpdatedShow)
                            }
                        }
                            ?: Timber.w("Popular show '${showNeedingImage.title}' (TraktID: ${showNeedingImage.traktID}) missing imdbID for image fetch.")
                    }

                    if (showsToUpdateWithFetchedImages.isNotEmpty()) {
                        traktDao.insertAllTraktPopular(*showsToUpdateWithFetchedImages.toTypedArray()) // This will update existing rows by PK
                        Timber.d("refreshTraktPopularShows: Updated image information for ${showsToUpdateWithFetchedImages.size} popular shows.")
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

        val tableName = DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName // Ensure this enum value exists
        val isEmpty = traktDao.checkIfMostAnticipatedShowsIsEmpty() // Ensure DAO method exists
        val needsUpdate = isEmpty || isUpdateNeededByDay(tableName) || forceRefresh

        if (!needsUpdate) {
            Timber.d("refreshTraktMostAnticipatedShows: Update not needed. Empty: $isEmpty, Force: $forceRefresh.")
            _isLoadingTraktMostAnticipated.value = false
            return
        }

        _isLoadingTraktMostAnticipated.value = true
        Timber.d("refreshTraktMostAnticipatedShows: STARTING. IsEmpty: $isEmpty, NeedsUpdate: ${isUpdateNeededByDay(tableName)}, Force: $forceRefresh")

        try {
            withContext(Dispatchers.IO) {
                val networkResponseItems: List<NetworkTraktMostAnticipatedResponseItem> =
                    traktService.getMostAnticipatedShowsAsync().await() // Verify your service method name

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

                if (newShowDatabaseModels.isEmpty()){
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
                        val updatedShow = existingLocalShow.copy(
                            title = networkShowModel.title,
                            year = networkShowModel.year,
                            slug = networkShowModel.slug,
                            tmdbID = networkShowModel.tmdbID,
                            traktID = networkShowModel.traktID,
                            tvdbID = networkShowModel.tvdbID,
                            imdbID = networkShowModel.imdbID
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
                    val showsToUpdateWithFetchedImages = mutableListOf<DatabaseTraktMostAnticipated>()
                    for (showNeedingImage in showsMissingImages) {
                        showNeedingImage.imdbID?.let { imdbId ->
                            Timber.d("Fetching images for most anticipated show: '${showNeedingImage.title}' (IMDb: $imdbId)")
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
                        } ?: Timber.w("Most anticipated show '${showNeedingImage.title}' (TraktID: ${showNeedingImage.id}) missing imdbID for image fetch.")
                    }

                    if (showsToUpdateWithFetchedImages.isNotEmpty()) {
                        traktDao.insertAllTraktMostAnticipated(*showsToUpdateWithFetchedImages.toTypedArray())
                        Timber.d("refreshTraktMostAnticipatedShows: Updated images/tvMazeIDs for ${showsToUpdateWithFetchedImages.size} shows.")
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

    private suspend fun shouldUpdate(
        databaseTables: DatabaseTables,
        tableUpdateInterval: TableUpdateInterval
    ): Boolean {
        // Assuming canProceedWithUpdate is protected or public in BaseRepository
        return canProceedWithUpdate(
            databaseTables.tableName,
            tableUpdateInterval.intervalMins.toLong()
        )
    }

    private suspend fun logTableUpdate(databaseTables: DatabaseTables) {
        Timber.d("Attempting to log table update for: ${databaseTables.tableName}")

        try {
            logTableUpdateTimestamp(databaseTables.tableName)
            Timber.d("logTableUpdateTimestamp finished for: ${databaseTables.tableName} in repositoryScope")
        } catch (e: Exception) {
            Timber.e(
                e,
                "Error in repositoryScope.launch while logging table update for ${databaseTables.tableName}"
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
    override suspend fun checkInToShow(showSeasonEpisode: ShowSeasonEpisode, token: String?) {
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
                val traktShowId = idLookupResponse.firstNotNullOfOrNull { result ->
                    if (result.type == "show" && result.show?.ids?.trakt != null) {
                        result.show.ids.trakt
                    } else null
                }

                if (traktShowId == null) {
                    logTraktException("Could not find Trakt Show ID for IMDB ID: $imdbId")
                    _traktCheckInEvent.tryEmit(TraktCheckInStatus(message = "Could not find the show on Trakt using IMDB ID: $imdbId."))

                    return@withContext
                }
                Timber.d("Found Trakt Show ID: $traktShowId for IMDB ID: $imdbId")

                val bearerToken = "Bearer $token"
                val request = NetworkTraktCheckInRequest(
                    show = NetworkTraktCheckInRequestShow(
                        ids = NetworkTraktCheckInRequestShowIds(
                            trakt = traktShowId,
                        ),
                        title = null,
                        year = null
                    ),
                    episode = NetworkTraktCheckInRequestEpisode(
                        season = seasonNumber,
                        number = episodeNumber
                    )
                )

                Timber.d("Attempting Trakt check-in: Show Trakt ID $traktShowId, S${seasonNumber}E${episodeNumber}")
                val checkInApiResponse: NetworkTraktCheckInResponse =
                    traktService.checkInAsync(bearerToken, request).await()

                val domainStatus = checkInApiResponse.asDomainModel()
                _traktCheckInEvent.tryEmit(domainStatus)
                Timber.i("Trakt check-in successful: ${domainStatus.message}")

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                logTraktException(
                    "Trakt check-in process HTTP error (Code: ${e.code()}): $errorBody",
                    e
                )

                var errorStatus: TraktCheckInStatus
                if (e.code() == 409 && errorBody != null) {
                    try {
                        val conflictResponse = traktConflictErrorAdapter.fromJson(errorBody)
                        if (conflictResponse?.expiresAt != null) {
                            val showTitleFromConflict = conflictResponse.show?.title
                                ?: showSeasonEpisode.name // Prioritize conflict response
                            val conflictSeason = conflictResponse.episode?.season ?: seasonNumber
                            val conflictEpisode = conflictResponse.episode?.number ?: episodeNumber
                            errorStatus = TraktCheckInStatus(
                                season = conflictSeason,
                                episode = conflictEpisode,
                                checkInTime = DateUtils.getDisplayDateFromDateStamp(conflictResponse.expiresAt)
                                    .toString(),
                                message = "A check-in for ${showTitleFromConflict ?: "this item"} S${conflictSeason}E${conflictEpisode} is already in progress. Expires at: ${
                                    DateUtils.getDisplayDateFromDateStamp(
                                        conflictResponse.expiresAt
                                    )
                                }."
                            )
                        } else {
                            // 409 but no expires_at, could be another type of conflict (e.g., watching something else not this item)
                            errorStatus = TraktCheckInStatus(
                                season = seasonNumber, episode = episodeNumber,
                                message = "Check-in conflict (Code 409). You might be watching something else, or the item is already watched. Details: $errorBody"
                            )
                        }
                    } catch (parseEx: Exception) {
                        logTraktException(
                            "Failed to parse Trakt 409 error body for check-in.",
                            parseEx
                        )
                        errorStatus = TraktCheckInStatus(
                            season = seasonNumber, episode = episodeNumber,
                            message = "Check-in conflict (Code 409), unable to parse details. Raw: $errorBody"
                        )
                    }
                } else {
                    val httpErrorMessage = when (e.code()) {
                        400 -> "Bad request (Code 400). Details: $errorBody"
                        401 -> "Unauthorized. Check Trakt token (Code 401). Details: $errorBody"
                        403 -> "Forbidden. You may not have permission for this action or item (Code 403). Details: $errorBody"
                        404 -> "Show or episode not found on Trakt (Code 404). Details: $errorBody"
                        412 -> "Precondition Failed - Typically related to user settings or watched status (Code 412). Details: $errorBody"
                        429 -> "Rate limit exceeded. Please try again later (Code 429). Details: $errorBody"
                        500, 502, 503, 504 -> "Trakt server error (Code: ${e.code()}). Please try again later. Details: $errorBody"
                        else -> "Check-in process failed with HTTP error (Code: ${e.code()}). Details: $errorBody"
                    }
                    errorStatus = TraktCheckInStatus(
                        season = seasonNumber, episode = episodeNumber,
                        message = httpErrorMessage
                    )
                }
                _traktCheckInEvent.tryEmit(errorStatus)
            } catch (e: Exception) { // For non-HTTP exceptions (network issues, other parsing errors, etc.)
                logTraktException("Generic error during Trakt check-in process.", e)
                _traktCheckInEvent.tryEmit(
                    TraktCheckInStatus(
                        season = seasonNumber, episode = episodeNumber,
                        message = "An unexpected error occurred: ${e.localizedMessage}"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        const val FAVORITES_LIST_NAME = "UpnextApp Favorites"
    }
}
