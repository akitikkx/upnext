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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.BuildConfig
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.database.DatabaseFavoriteNextEpisode
import com.theupnextapp.database.DatabaseFavoriteShows
import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.DatabaseTraktMostAnticipated
import com.theupnextapp.database.DatabaseTraktPopularShows
import com.theupnextapp.database.DatabaseTraktTrendingShows
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.domain.FavoriteNextEpisode
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
import com.theupnextapp.domain.areVariablesEmpty
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
import com.theupnextapp.network.models.trakt.NetworkTraktCreateCustomListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktCreateCustomListResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMostAnticipatedResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktPopularShowsResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequestShow
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequestShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktUserListItemResponse
import com.theupnextapp.network.models.trakt.NetworkTraktUserListItemResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktUserListsResponse
import com.theupnextapp.network.models.trakt.asDatabaseModel
import com.theupnextapp.network.models.trakt.asDomainModel
import com.theupnextapp.network.models.tvmaze.asDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import timber.log.Timber

class TraktRepository(
    private val upnextDao: UpnextDao,
    private val traktDao: TraktDao,
    private val tvMazeService: TvMazeService,
    private val traktService: TraktService,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseRepository(upnextDao = upnextDao, tvMazeService = tvMazeService) {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun tableUpdate(tableName: String): Flow<TableUpdate?> =
        upnextDao.getTableLastUpdate(tableName).map {
            it?.asDomainModel()
        }

    val traktPopularShows: Flow<List<TraktPopularShows>> =
        traktDao.getTraktPopular().map { it.asDomainModel() }

    val traktTrendingShows: Flow<List<TraktTrendingShows>> =
        traktDao.getTraktTrending().map { it.asDomainModel() }

    val traktMostAnticipatedShows: Flow<List<TraktMostAnticipated>> =
        traktDao.getTraktMostAnticipated().map { it.asDomainModel() }

    val traktFavoriteShows: Flow<List<TraktUserListItem>> =
        traktDao.getFavoriteShows().map { it.asDomainModel() }

    val favoriteShowEpisodes: Flow<List<FavoriteNextEpisode>> =
        traktDao.getFavoriteEpisodes().map { it.asDomainModel() }

    val traktAccessToken: StateFlow<TraktAccessToken?> = traktDao.getTraktAccessData()
        .map { it.asDomainModel() }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun getTraktAccessTokenRaw(): DatabaseTraktAccess? = traktDao.getTraktAccessDataRaw()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingTraktTrending = MutableLiveData<Boolean>()
    val isLoadingTraktTrending: LiveData<Boolean> = _isLoadingTraktTrending

    private val _isLoadingTraktPopular = MutableLiveData<Boolean>()
    val isLoadingTraktPopular: LiveData<Boolean> = _isLoadingTraktPopular

    private val _isLoadingTraktMostAnticipated = MutableLiveData<Boolean>()
    val isLoadingTraktMostAnticipated: LiveData<Boolean> = _isLoadingTraktMostAnticipated

    private val _traktShowRating = MutableStateFlow<TraktShowRating?>(null)
    val traktShowRating: StateFlow<TraktShowRating?> = _traktShowRating

    private val _traktShowStats = MutableStateFlow<TraktShowStats?>(null)
    val traktShowStats: StateFlow<TraktShowStats?> = _traktShowStats

    private val _favoriteShow = MutableStateFlow<TraktUserListItem?>(null)
    val favoriteShow: StateFlow<TraktUserListItem?> = _favoriteShow.asStateFlow()

    private val _traktCheckInStatus = MutableSharedFlow<TraktCheckInStatus>()
    val traktCheckInStatus: SharedFlow<TraktCheckInStatus> = _traktCheckInStatus

    private suspend fun <T> makeTraktCall(request: suspend () -> T): T? {
        return try {
            _isLoading.value = true
            val result = request.invoke()
            _isLoading.value = false
            result
        } catch (e: Exception) {
            _isLoading.value = false
            Timber.d(e)
            firebaseCrashlytics.recordException(e)
            null
        }
    }

    suspend fun getTraktAccessToken(code: String?) {
        if (code.isNullOrEmpty()) {
            reportTraktError("Could not get the access token due to a null code")
            return
        }

        makeTraktCall {
            val traktAccessTokenRequest = NetworkTraktAccessTokenRequest(
                code = code,
                client_id = BuildConfig.TRAKT_CLIENT_ID,
                client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                grant_type = "authorization_code"
            )
            val accessTokenResponse =
                traktService.getAccessTokenAsync(traktAccessTokenRequest).await()
            traktDao.deleteTraktAccessData()
            traktDao.insertAllTraktAccessData(accessTokenResponse.asDatabaseModel())
        }
    }

    suspend fun revokeTraktAccessToken(traktAccessToken: TraktAccessToken) {
        if (traktAccessToken.areVariablesEmpty()) return

        if (traktAccessToken.access_token.isNullOrEmpty()) return

        makeTraktCall {
            val revokeRequest = NetworkTraktRevokeAccessTokenRequest(
                    client_id = BuildConfig.TRAKT_CLIENT_ID,
                    client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                    token = traktAccessToken.access_token
                )

            revokeRequest.let { traktService.revokeAccessTokenAsync(it).await() }
            traktDao.deleteTraktAccessData()
        }
    }

    suspend fun getTraktAccessRefreshToken(refreshToken: String?) {
        if (refreshToken.isNullOrEmpty()) {
            reportTraktError("Could not get the access refresh token due to a null refresh token")
            return
        }

        makeTraktCall {
            val traktAccessTokenRequest =
                NetworkTraktAccessRefreshTokenRequest(
                    refresh_token = refreshToken,
                    client_id = BuildConfig.TRAKT_CLIENT_ID,
                    client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                    redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                    grant_type = "refresh_token"
                )

            val accessTokenResponse =
                traktService.getAccessRefreshTokenAsync(traktAccessTokenRequest)
                    .await()
            traktDao.deleteTraktAccessData()
            traktDao.insertAllTraktAccessData(accessTokenResponse.asDatabaseModel())
        }
    }

    suspend fun refreshFavoriteShows(forceRefresh: Boolean = false, token: String?) {
        if (token.isNullOrEmpty()) {
            reportTraktError("Could not get the favorite shows due to a null access token")
            return
        }

        if (forceRefresh || canProceedWithUpdate(
                tableName = DatabaseTables.TABLE_FAVORITE_SHOWS.tableName,
                intervalMinutes = TableUpdateInterval.TRAKT_FAVORITE_SHOWS.intervalMins
            )
        ) {
            makeTraktCall {
                val userSettings =
                    traktService.getUserSettingsAsync(token = "Bearer $token").await()
                val userSlug = userSettings.user?.ids?.slug

                if (!userSlug.isNullOrEmpty()) {
                    val favoritesListId = getOrCreateFavoritesListId(userSlug, token)

                    if (!favoritesListId.isNullOrEmpty()) {
                        val customListItemsResponse = traktService.getCustomListItemsAsync(
                            token = "Bearer $token",
                            userSlug = userSlug,
                            traktId = favoritesListId
                        ).await()
                        handleTraktUserListItemsResponse(customListItemsResponse)
                    }
                }
            }
        }
    }

    private suspend fun getOrCreateFavoritesListId(userSlug: String?, token: String?): String? {
        if (userSlug == null || token == null) return null

        var favoritesListId: String?
        val userCustomLists = getUserSettings(userSlug, token)
        favoritesListId = userCustomLists?.find { it.name == FAVORITES_LIST_NAME }?.ids?.slug

        if (favoritesListId == null) {
            val createCustomListResponse = createCustomList(userSlug, token)
            favoritesListId = createCustomListResponse?.ids?.slug
        }
        return favoritesListId
    }

    private suspend fun handleTraktUserListItemsResponse(customListItemsResponse: NetworkTraktUserListItemResponse?) {
        val shows = mutableListOf<DatabaseFavoriteShows>()
        if (!customListItemsResponse.isNullOrEmpty()) {
            for (item in customListItemsResponse) {
                val favoriteItem: NetworkTraktUserListItemResponseItem = item

                val (id, poster, heroImage) = getImages(favoriteItem.show?.ids?.imdb)

                favoriteItem.show?.originalImageUrl = poster
                favoriteItem.show?.mediumImageUrl = heroImage
                favoriteItem.show?.ids?.tvMazeID = id

                shows.add(item.asDatabaseModel())
            }
        }

        upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
        traktDao.deleteAllFavoriteShows()
        if (shows.isNotEmpty()) {
            traktDao.insertAllFavoriteShows(*shows.toTypedArray())
        }
        upnextDao.insertTableUpdateLog(
            DatabaseTableUpdate(
                table_name = DatabaseTables.TABLE_FAVORITE_SHOWS.tableName,
                last_updated = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearFavorites() {
        withContext(Dispatchers.IO) {
            traktDao.deleteAllFavoriteShows()
            traktDao.deleteAllFavoriteEpisodes()
            upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
            upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_FAVORITE_EPISODES.tableName)
        }
    }

    suspend fun checkIfShowIsFavorite(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) return

        withContext(Dispatchers.IO) {
            val show = traktDao.getFavoriteShow(imdbID)
            _favoriteShow.emit(show?.asDomainModel())
        }
    }

    /**
     * Add the show to a custom list stored on Trakt
     */
    suspend fun addShowToList(imdbID: String?, token: String?) {
        if (imdbID.isNullOrEmpty()) {
            reportTraktError("Could add the show to the favorites due to a null traktID")
            return
        }

        makeTraktCall {
            val userSettings = traktService.getUserSettingsAsync(token = "Bearer $token").await()
            val userSlug = userSettings.user?.ids?.slug

            if (!userSlug.isNullOrEmpty()) {
                val favoritesListId = getOrCreateFavoritesListId(userSlug, token)

                if (!favoritesListId.isNullOrEmpty()) {
                    val showInfoResponse = traktService.getShowInfoAsync(imdbID).await()
                    val traktId = showInfoResponse.ids?.trakt

                    if (traktId != null) {
                        val addShowToListRequest = NetworkTraktAddShowToListRequest(
                            shows = listOf(
                                NetworkTraktAddShowToListRequestShow(
                                    ids = NetworkTraktAddShowToListRequestShowIds(trakt = traktId)
                                )
                            )
                        )

                        val addToListResponse = traktService.addShowToCustomListAsync(
                            userSlug = userSlug,
                            traktId = favoritesListId,
                            token = "Bearer $token",
                            networkTraktAddShowToListRequest = addShowToListRequest
                        ).await()

                        if (addToListResponse.added.shows == 1) {
                            val customListItemsResponse = traktService.getCustomListItemsAsync(
                                token = "Bearer $token",
                                userSlug = userSlug,
                                traktId = favoritesListId
                            ).await()
                            handleTraktUserListItemsResponse(customListItemsResponse)
                            checkIfShowIsFavorite(imdbID)
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove a show from the personal list on Trakt
     */
    suspend fun removeShowFromList(traktId: Int?, imdbID: String?, token: String?) {
        if (traktId == null || imdbID.isNullOrEmpty()) {
            reportTraktError("Could remove the show from the favorites due to either a null traktID or imdbID: $imdbID, traktID: $traktId")
            return
        }
        makeTraktCall {
            var hasFavoritesList = false
            var favoritesListId: String? = null

            val userSettings =
                traktService.getUserSettingsAsync(token = "Bearer $token").await()
            if (!userSettings.user?.ids?.slug.isNullOrEmpty()) {
                val userSlug = userSettings.user?.ids?.slug
                val userCustomLists = getUserSettings(userSlug, token)

                if (!userCustomLists.isNullOrEmpty()) {
                    for (listItem in userCustomLists) {
                        if (listItem.name == FAVORITES_LIST_NAME) {
                            hasFavoritesList = true
                            favoritesListId = listItem.ids?.slug
                        }
                    }
                }
                if (!hasFavoritesList) {
                    val createCustomListResponse = createCustomList(userSlug, token)
                    if (createCustomListResponse?.ids?.trakt != null) {
                        favoritesListId = createCustomListResponse.ids.slug
                    }
                }
                if (!favoritesListId.isNullOrEmpty()) {
                    val removeShowFromListRequest = NetworkTraktRemoveShowFromListRequest(
                        shows = listOf(
                            NetworkTraktRemoveShowFromListRequestShow(
                                ids = NetworkTraktRemoveShowFromListRequestShowIds(trakt = traktId)
                            )
                        )
                    )
                    val removeFromListResponse = userSlug?.let {
                        traktService.removeShowFromCustomListAsync(
                            userSlug = it,
                            traktId = favoritesListId,
                            token = "Bearer $token",
                            networkTraktRemoveShowFromListRequest = removeShowFromListRequest
                        ).await()
                    }

                    if (removeFromListResponse?.deleted?.shows == 1) {
                        traktDao.deleteFavoriteEpisode(imdbID)

                        val customListItemsResponse =
                            traktService.getCustomListItemsAsync(
                                token = "Bearer $token",
                                userSlug = userSlug,
                                traktId = favoritesListId
                            ).await()
                        handleTraktUserListItemsResponse(customListItemsResponse)
                        checkIfShowIsFavorite(imdbID)
                    }
                }
            }

        }
    }

    suspend fun refreshFavoriteNextEpisodes(forceRefresh: Boolean = false) {
        if (forceRefresh || canProceedWithUpdate(
                tableName = DatabaseTables.TABLE_FAVORITE_EPISODES.tableName,
                intervalMinutes = TableUpdateInterval.TRAKT_FAVORITE_EPISODES.intervalMins
            )
        ) {
            makeTraktCall {
                val episodesList = mutableListOf<DatabaseFavoriteNextEpisode>()
                val favoriteShows = traktDao.getFavoriteShowsRaw()
                if (favoriteShows.isNotEmpty()) {
                    for (item in favoriteShows) {
                        val nextEpisode = item.tvMazeID?.let { getNextEpisode(it) }

                        val (id, poster, heroImage) = getImages(item.imdbID)

                        nextEpisode?.originalShowImageUrl = poster
                        nextEpisode?.mediumShowImageUrl = heroImage
                        nextEpisode?.tvMazeID = id

                        nextEpisode?.asDatabaseModel()?.let { episodesList.add(it) }
                    }
                    if (episodesList.isNotEmpty()) {
                        traktDao.deleteAllFavoriteEpisodes()
                        traktDao.insertAllFavoriteNextEpisodes(*episodesList.toTypedArray())

                        for (episode in episodesList) {
                            val showRecord = episode.tvMazeID?.let { tvMazeId ->
                                traktDao.getFavoriteShowRaw(
                                    tvMazeId
                                )
                            }
                            showRecord?.airStamp = episode.airStamp
                            showRecord?.let { traktDao.updateFavoriteEpisode(it) }
                        }

                        upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_TRENDING.tableName)
                        upnextDao.insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_FAVORITE_EPISODES.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }
                }

            }
        }
    }

    private suspend fun getUserSettings(
        userSlug: String?,
        token: String?
    ): NetworkTraktUserListsResponse? {
        return userSlug?.let { slug ->
            traktService.getUserCustomListsAsync(
                token = "Bearer $token",
                userSlug = slug
            ).await()
        }
    }

    private suspend fun createCustomList(
        userSlug: String?,
        token: String?
    ): NetworkTraktCreateCustomListResponse? {
        val createCustomListRequest = NetworkTraktCreateCustomListRequest(
            name = FAVORITES_LIST_NAME
        )
        return userSlug?.let { slug ->
            traktService.createCustomListAsync(
                token = "Bearer $token",
                userSlug = slug,
                createCustomListRequest = createCustomListRequest
            ).await()
        }
    }

    suspend fun getTraktShowRating(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            reportTraktError("Could not get the show rating due to a null imdb ID")
            return
        }

        makeTraktCall {
            val showRatingResponse = traktService.getShowRatingsAsync(id = imdbID).await()
            _traktShowRating.value = showRatingResponse.asDomainModel()
        }
    }

    suspend fun getTraktShowStats(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            reportTraktError("Could not get the show stats due to a null imdb ID")
            return
        }

        makeTraktCall {
            val showStatsResponse = traktService.getShowStatsAsync(
                id = imdbID
            ).await()
            _traktShowStats.emit(showStatsResponse.asDomainModel())
        }
    }

    suspend fun refreshTraktTrendingShows(forceRefresh: Boolean = false) {
        if (forceRefresh || canProceedWithUpdate(
                tableName = DatabaseTables.TABLE_TRAKT_TRENDING.tableName,
                intervalMinutes = TableUpdateInterval.TRAKT_TRENDING_ITEMS.intervalMins
            )
        ) {
            makeTraktCall {
                val shows = mutableListOf<DatabaseTraktTrendingShows>()
                val trendingShowsResponse = traktService.getTrendingShowsAsync().await()

                if (trendingShowsResponse.isNotEmpty()) {
                    for (item in trendingShowsResponse) {
                        val (id, poster, heroImage) = getImages(item.show.ids.imdb)

                        item.show.originalImageUrl = poster
                        item.show.mediumImageUrl = heroImage
                        item.show.ids.tvMazeID = id

                        shows.add(item.asDatabaseModel())
                    }

                    upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_TRENDING.tableName)
                    traktDao.deleteAllTraktTrending()
                    traktDao.insertAllTraktTrending(*shows.toTypedArray())
                    upnextDao.insertTableUpdateLog(
                        DatabaseTableUpdate(
                            table_name = DatabaseTables.TABLE_TRAKT_TRENDING.tableName,
                            last_updated = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    /**
     * Get the images for the given ImdbID
     */
    private suspend fun getImages(imdbID: String?): Triple<Int?, String?, String?> {
        return makeTraktCall {
            val tvMazeSearch = imdbID?.let { tvMazeService.getShowLookupAsync(it).await() }

            val fallbackPoster = tvMazeSearch?.image?.original
            val fallbackMedium = tvMazeSearch?.image?.medium
            var poster: String? = null
            var heroImage: String? = null

            val showImagesResponse = tvMazeSearch?.id?.toString()?.let {
                tvMazeService.getShowImagesAsync(it).await()
            }

            showImagesResponse?.let { response ->
                poster = response.find { it.type == "poster" }?.resolutions?.original?.url
                heroImage = response.find { it.type == "background" }?.resolutions?.original?.url
            }

            Triple(tvMazeSearch?.id, poster ?: fallbackPoster, heroImage ?: fallbackMedium)
        } ?: Triple(null, null, null) // Return Triple with nulls if makeTraktCall returns null
    }

    suspend fun refreshTraktPopularShows() {
        makeTraktCall {
            if (canProceedWithUpdate(
                    tableName = DatabaseTables.TABLE_TRAKT_POPULAR.tableName,
                    intervalMinutes = TableUpdateInterval.TRAKT_POPULAR_ITEMS.intervalMins
                )
            ) {
                val shows: MutableList<DatabaseTraktPopularShows> = mutableListOf()

                val popularShowsResponse = traktService.getPopularShowsAsync().await()

                if (!popularShowsResponse.isEmpty()) {
                    for (item in popularShowsResponse) {
                        val popularItem: NetworkTraktPopularShowsResponseItem = item

                        val (id, poster, heroImage) = getImages(popularItem.ids.imdb)

                        popularItem.originalImageUrl =
                            poster
                        popularItem.mediumImageUrl =
                            heroImage
                        popularItem.ids.tvMazeID = id

                        shows.add(popularItem.asDatabaseModel())
                    }

                    upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_POPULAR.tableName)
                    traktDao.deleteAllTraktPopular()
                    traktDao.insertAllTraktPopular(*shows.toTypedArray())
                    upnextDao.insertTableUpdateLog(
                        DatabaseTableUpdate(
                            table_name = DatabaseTables.TABLE_TRAKT_POPULAR.tableName,
                            last_updated = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    suspend fun refreshTraktMostAnticipatedShows() {
        makeTraktCall {

            if (canProceedWithUpdate(
                    tableName = DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName,
                    intervalMinutes = TableUpdateInterval.TRAKT_MOST_ANTICIPATED_ITEMS.intervalMins
                )
            ) {
                val shows: MutableList<DatabaseTraktMostAnticipated> = mutableListOf()

                val mostAnticipatedShowsResponse =
                    traktService.getMostAnticipatedShowsAsync().await()

                if (!mostAnticipatedShowsResponse.isEmpty()) {
                    for (item in mostAnticipatedShowsResponse) {
                        val mostAnticipatedItem: NetworkTraktMostAnticipatedResponseItem = item

                        val (id, poster, heroImage) = getImages(mostAnticipatedItem.show.ids.imdb)

                        mostAnticipatedItem.show.originalImageUrl =
                            poster
                        mostAnticipatedItem.show.mediumImageUrl =
                            heroImage
                        mostAnticipatedItem.show.ids.tvMazeID = id
                        shows.add(mostAnticipatedItem.asDatabaseModel())
                    }

                    upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName)
                    traktDao.deleteAllTraktMostAnticipated()
                    traktDao.insertAllTraktMostAnticipated(*shows.toTypedArray())
                    upnextDao.insertTableUpdateLog(
                        DatabaseTableUpdate(
                            table_name = DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName,
                            last_updated = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    suspend fun checkInToShow(showSeasonEpisode: ShowSeasonEpisode, token: String?) {
        if (showSeasonEpisode.imdbID.isNullOrEmpty()) {
            reportTraktError("Could not check-in to the episode due to a null imdb ID")
            return
        }

        makeTraktCall {
            val showLookupResponse = showSeasonEpisode.imdbID?.let {
                traktService.idLookupAsync(idType = "imdb", id = it).await()
            }

            if (!showLookupResponse.isNullOrEmpty()) {
                val showLookup = showLookupResponse.firstOrNull()
                if (showLookup != null) {
                    val checkInRequest = NetworkTraktCheckInRequest(
                        show = NetworkTraktCheckInRequestShow(
                            ids = NetworkTraktCheckInRequestShowIds(trakt = showLookup.show?.ids?.trakt),
                            title = showLookup.show?.title,
                            year = showLookup.show?.year
                        ),
                        episode = NetworkTraktCheckInRequestEpisode(
                            season = showSeasonEpisode.season,
                            number = showSeasonEpisode.number
                        )
                    )

                    val checkInResponse = traktService.checkInAsync(
                        token = "Bearer $token",
                        networkTraktCheckInRequest = checkInRequest
                    ).await()

                    _traktCheckInStatus.emit(checkInResponse.asDomainModel())
                }
            }
        }
    }

    private fun reportTraktError(message: String) {
        Timber.d(Throwable(message = message))
        firebaseCrashlytics
            .recordException(Throwable(message = "TraktRepository: $message"))
    }

    sealed class LoadingState {
        data object Idle : LoadingState()
        data object Active : LoadingState()
    }

    companion object {
        const val FAVORITES_LIST_NAME = "Upnext Favorites"
    }
}
