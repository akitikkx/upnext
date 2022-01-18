package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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
import com.theupnextapp.extensions.Event
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
import com.theupnextapp.network.models.trakt.NetworkTraktTrendingShowsResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktUserListItemResponse
import com.theupnextapp.network.models.trakt.NetworkTraktUserListItemResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktUserListsResponse
import com.theupnextapp.network.models.trakt.asDatabaseModel
import com.theupnextapp.network.models.trakt.asDomainModel
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import com.theupnextapp.network.models.tvmaze.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.net.ssl.SSLHandshakeException

class TraktRepository constructor(
    private val upnextDao: UpnextDao,
    private val traktDao: TraktDao,
    private val tvMazeService: TvMazeService,
    private val traktService: TraktService,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseRepository(upnextDao = upnextDao, tvMazeService = tvMazeService) {

    fun tableUpdate(tableName: String): LiveData<TableUpdate?> =
        Transformations.map(upnextDao.getTableLastUpdate(tableName)) {
            it?.asDomainModel()
        }

    val traktPopularShows: LiveData<List<TraktPopularShows>> =
        Transformations.map(traktDao.getTraktPopular()) {
            it.asDomainModel()
        }

    val traktTrendingShows: LiveData<List<TraktTrendingShows>> =
        Transformations.map(traktDao.getTraktTrending()) {
            it.asDomainModel()
        }

    val traktMostAnticipatedShows: LiveData<List<TraktMostAnticipated>> =
        Transformations.map(traktDao.getTraktMostAnticipated()) {
            it.asDomainModel()
        }

    val traktFavoriteShows: LiveData<List<TraktUserListItem>> =
        Transformations.map(traktDao.getFavoriteShows()) {
            it.asDomainModel()
        }

    val favoriteShowEpisodes: LiveData<List<FavoriteNextEpisode>> =
        Transformations.map(traktDao.getFavoriteEpisodes()) {
            it.asDomainModel()
        }

    val traktAccessToken: LiveData<TraktAccessToken?> =
        Transformations.map(traktDao.getTraktAccessData()) {
            it?.asDomainModel()
        }

    fun getTraktAccessTokenRaw(): DatabaseTraktAccess? = traktDao.getTraktAccessDataRaw()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingTraktTrending = MutableLiveData<Boolean>()
    val isLoadingTraktTrending: LiveData<Boolean> = _isLoadingTraktTrending

    private val _isLoadingTraktPopular = MutableLiveData<Boolean>()
    val isLoadingTraktPopular: LiveData<Boolean> = _isLoadingTraktPopular

    private val _isLoadingTraktMostAnticipated = MutableLiveData<Boolean>()
    val isLoadingTraktMostAnticipated: LiveData<Boolean> = _isLoadingTraktMostAnticipated

    private val _traktShowRating = MutableLiveData<TraktShowRating>()
    val traktShowRating: LiveData<TraktShowRating> = _traktShowRating

    private val _traktShowStats = MutableLiveData<TraktShowStats>()
    val traktShowStats: LiveData<TraktShowStats> = _traktShowStats

    private val _favoriteShow = MutableLiveData<TraktUserListItem>()
    val favoriteShow: LiveData<TraktUserListItem> = _favoriteShow

    private val _traktCheckInStatus = MutableLiveData<Event<TraktCheckInStatus>>()
    val traktCheckInStatus: LiveData<Event<TraktCheckInStatus>> = _traktCheckInStatus

    suspend fun getTraktAccessToken(code: String?) {
        if (code.isNullOrEmpty()) {
            logTraktException("Could not get the access token due to a null code")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val traktAccessTokenRequest =
                    NetworkTraktAccessTokenRequest(
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
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun revokeTraktAccessToken(traktAccessToken: TraktAccessToken) {
        if (traktAccessToken.areVariablesEmpty()) return

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val revokeRequest = traktAccessToken.access_token?.let {
                    NetworkTraktRevokeAccessTokenRequest(
                        client_id = BuildConfig.TRAKT_CLIENT_ID,
                        client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                        token = it
                    )
                }
                revokeRequest?.let { traktService.revokeAccessTokenAsync(it).await() }
                traktDao.deleteTraktAccessData()
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun getTraktAccessRefreshToken(refreshToken: String?) {
        if (refreshToken.isNullOrEmpty()) {
            logTraktException("Could not get the access refresh token due to a null refresh token")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)

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
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshFavoriteShows(forceRefresh: Boolean = false, token: String?) {
        if (token.isNullOrEmpty()) {
            logTraktException("Could not get the favorite shows due to a null access token")
            return
        }

        if (forceRefresh || canProceedWithUpdate(
                tableName = DatabaseTables.TABLE_FAVORITE_SHOWS.tableName,
                intervalMinutes = TableUpdateInterval.TRAKT_FAVORITE_SHOWS.intervalMins
            )
        ) {
            withContext(Dispatchers.IO) {
                try {
                    _isLoading.postValue(true)
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
                            val customListItemsResponse =
                                userSlug?.let { slug ->
                                    traktService.getCustomListItemsAsync(
                                        token = "Bearer $token",
                                        userSlug = slug,
                                        traktId = favoritesListId
                                    ).await()
                                }
                            handleTraktUserListItemsResponse(customListItemsResponse)
                        }
                    }
                    _isLoading.postValue(false)
                } catch (e: Exception) {
                    _isLoading.postValue(false)
                    Timber.d(e)
                    firebaseCrashlytics.recordException(e)
                }
            }
        }
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
            _favoriteShow.postValue(show?.asDomainModel())
        }
    }

    /**
     * Add the show to a custom list stored on Trakt
     */
    suspend fun addShowToList(imdbID: String?, token: String?) {
        if (imdbID.isNullOrEmpty()) {
            logTraktException("Could add the show to the favorites due to a null traktID")
            return
        }
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
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
                        val showInfoResponse =
                            traktService.getShowInfoAsync(imdbID).await()
                        if (showInfoResponse.ids?.trakt != null) {
                            val addShowToListRequest = NetworkTraktAddShowToListRequest(
                                shows = listOf(
                                    NetworkTraktAddShowToListRequestShow(
                                        ids = NetworkTraktAddShowToListRequestShowIds(trakt = showInfoResponse.ids.trakt)
                                    )
                                )
                            )
                            val addToListResponse = userSlug?.let {
                                traktService.addShowToCustomListAsync(
                                    userSlug = it,
                                    traktId = favoritesListId,
                                    token = "Bearer $token",
                                    networkTraktAddShowToListRequest = addShowToListRequest
                                ).await()
                            }

                            if (addToListResponse?.added?.shows == 1) {
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
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    /**
     * Remove a show from the personal list on Trakt
     */
    suspend fun removeShowFromList(traktId: Int?, imdbID: String?, token: String?) {
        if (traktId == null || imdbID.isNullOrEmpty()) {
            logTraktException("Could remove the show from the favorites due to either a null traktID or imdbID: $imdbID, traktID: $traktId")
            return
        }
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
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
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshFavoriteNextEpisodes(forceRefresh: Boolean = false) {
        if (forceRefresh || canProceedWithUpdate(
                tableName = DatabaseTables.TABLE_FAVORITE_EPISODES.tableName,
                intervalMinutes = TableUpdateInterval.TRAKT_FAVORITE_EPISODES.intervalMins
            )
        ) {
            withContext(Dispatchers.IO) {
                try {
                    val episodesList = mutableListOf<DatabaseFavoriteNextEpisode>()
                    val favoriteShows = traktDao.getFavoriteShowsRaw()
                    if (!favoriteShows.isNullOrEmpty()) {
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
                } catch (e: Exception) {
                    _isLoading.postValue(false)
                    Timber.d(e)
                    firebaseCrashlytics.recordException(e)
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
            logTraktException("Could not get the show rating due to a null imdb ID")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showRatingResponse = traktService.getShowRatingsAsync(
                    id = imdbID
                ).await()
                _traktShowRating.postValue(showRatingResponse.asDomainModel())
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun getTraktShowStats(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            logTraktException("Could not get the show stats due to a null imdb ID")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showStatsResponse = traktService.getShowStatsAsync(
                    id = imdbID
                ).await()
                _traktShowStats.postValue(showStatsResponse.asDomainModel())
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshTraktTrendingShows() {
        withContext(Dispatchers.IO) {
            try {
                if (canProceedWithUpdate(
                        tableName = DatabaseTables.TABLE_TRAKT_TRENDING.tableName,
                        intervalMinutes = TableUpdateInterval.TRAKT_TRENDING_ITEMS.intervalMins
                    )
                ) {
                    _isLoadingTraktTrending.postValue(true)
                    val shows: MutableList<DatabaseTraktTrendingShows> = mutableListOf()

                    val trendingShowsResponse =
                        traktService.getTrendingShowsAsync().await()

                    if (!trendingShowsResponse.isEmpty()) {
                        for (item in trendingShowsResponse) {
                            val trendingItem: NetworkTraktTrendingShowsResponseItem = item

                            val (id, poster, heroImage) = getImages(trendingItem.show.ids.imdb)

                            trendingItem.show.originalImageUrl = poster
                            trendingItem.show.mediumImageUrl = heroImage
                            trendingItem.show.ids.tvMazeID = id

                            shows.add(trendingItem.asDatabaseModel())
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
                    _isLoadingTraktTrending.postValue(false)
                }
            } catch (e: Exception) {
                _isLoadingTraktTrending.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoadingTraktTrending.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoadingTraktTrending.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoadingTraktTrending.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    /**
     * Get the images for the given ImdbID
     */
    private suspend fun getImages(
        imdbID: String?
    ): Triple<Int?, String?, String?> {
        var tvMazeSearch: NetworkTvMazeShowLookupResponse? = null

        // perform a TvMaze search for the Trakt item using the Trakt title
        try {
            tvMazeSearch = imdbID?.let {
                tvMazeService.getShowLookupAsync(it).await()
            }
        } catch (e: Exception) {
            Timber.d(e)
            firebaseCrashlytics.recordException(e)
        }

        val fallbackPoster = tvMazeSearch?.image?.original
        val fallbackMedium = tvMazeSearch?.image?.medium
        var poster: String? = ""
        var heroImage: String? = ""

        var showImagesResponse: NetworkTvMazeShowImageResponse? = null
        try {
            showImagesResponse =
                tvMazeService.getShowImagesAsync(tvMazeSearch?.id.toString())
                    .await()
        } catch (e: Exception) {
            Timber.d(e)
            firebaseCrashlytics.recordException(e)
        }

        showImagesResponse.let { response ->
            if (!response.isNullOrEmpty()) {
                for (image in response) {
                    if (image.type == "poster") {
                        poster = image.resolutions.original.url
                    }

                    if (image.type == "background") {
                        heroImage = image.resolutions.original.url
                    }
                }
            }
        }
        return Triple(tvMazeSearch?.id, poster ?: fallbackPoster, heroImage ?: fallbackMedium)
    }

    suspend fun refreshTraktPopularShows() {
        withContext(Dispatchers.IO) {
            try {
                if (canProceedWithUpdate(
                        tableName = DatabaseTables.TABLE_TRAKT_POPULAR.tableName,
                        intervalMinutes = TableUpdateInterval.TRAKT_POPULAR_ITEMS.intervalMins
                    )
                ) {
                    _isLoadingTraktPopular.postValue(true)
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
                    _isLoadingTraktPopular.postValue(false)
                }
            } catch (e: Exception) {
                _isLoadingTraktPopular.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoadingTraktPopular.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoadingTraktPopular.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoadingTraktPopular.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshTraktMostAnticipatedShows() {
        withContext(Dispatchers.IO) {
            try {
                if (canProceedWithUpdate(
                        tableName = DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName,
                        intervalMinutes = TableUpdateInterval.TRAKT_MOST_ANTICIPATED_ITEMS.intervalMins
                    )
                ) {
                    _isLoadingTraktMostAnticipated.postValue(true)
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
                    _isLoadingTraktMostAnticipated.postValue(false)
                }
            } catch (e: Exception) {
                _isLoadingTraktMostAnticipated.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoadingTraktMostAnticipated.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoadingTraktMostAnticipated.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoadingTraktMostAnticipated.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun checkInToShow(showSeasonEpisode: ShowSeasonEpisode, token: String?) {
        if (showSeasonEpisode.imdbID.isNullOrEmpty()) {
            logTraktException("Could not check-in to the episode due to a null imdb ID")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showLookupResponse =
                    showSeasonEpisode.imdbID?.let {
                        traktService.idLookupAsync(idType = "imdb", id = it)
                            .await()
                    }
                if (!showLookupResponse.isNullOrEmpty()) {
                    val showLookup = showLookupResponse.firstOrNull()
                    if (showLookup != null) {
                        val checkInRequest = NetworkTraktCheckInRequest(
                            show = NetworkTraktCheckInRequestShow(
                                ids = NetworkTraktCheckInRequestShowIds(
                                    trakt = showLookup.show?.ids?.trakt
                                ),
                                title = showLookup.show?.title,
                                year = showLookup.show?.year
                            ),
                            episode = NetworkTraktCheckInRequestEpisode(
                                season = showSeasonEpisode.season,
                                number = showSeasonEpisode.number
                            )
                        )
                        val checkInResponse =
                            traktService.checkInAsync(
                                token = "Bearer $token",
                                networkTraktCheckInRequest = checkInRequest
                            ).await()
                        _traktCheckInStatus.postValue(Event(checkInResponse.asDomainModel()))
                    }
                }
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                if (e.code() == HTTP_409) {
                    _traktCheckInStatus.postValue(Event(TraktCheckInStatus(message = "A check-in is already in progress on Trakt. Check-ins for an episode at a time")))
                }
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    private fun logTraktException(message: String) {
        Timber.d(Throwable(message = message))
        firebaseCrashlytics
            .recordException(Throwable(message = "TraktRepository: $message"))
    }

    companion object {
        const val FAVORITES_LIST_NAME = "Upnext Favorites"
        const val HTTP_409 = 409
    }
}