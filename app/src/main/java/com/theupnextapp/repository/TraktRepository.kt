package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.BuildConfig
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.database.*
import com.theupnextapp.domain.*
import com.theupnextapp.network.TraktNetwork
import com.theupnextapp.network.TvMazeNetwork
import com.theupnextapp.network.models.trakt.*
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
    private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseRepository(upnextDao) {

    fun tableUpdate(tableName: String): LiveData<TableUpdate?> =
        Transformations.map(upnextDao.getTableLastUpdate(tableName)) {
            it?.asDomainModel()
        }

    val traktPopularShows: LiveData<List<TraktPopularShows>> =
        Transformations.map(upnextDao.getTraktPopular()) {
            it.asDomainModel()
        }

    val traktTrendingShows: LiveData<List<TraktTrendingShows>> =
        Transformations.map(upnextDao.getTraktTrending()) {
            it.asDomainModel()
        }

    val traktMostAnticipatedShows: LiveData<List<TraktMostAnticipated>> =
        Transformations.map(upnextDao.getTraktMostAnticipated()) {
            it.asDomainModel()
        }

    val traktFavoriteShows: LiveData<List<TraktUserListItem>> =
        Transformations.map(upnextDao.getFavoriteShows()) {
            it.asDomainModel()
        }

    val favoriteShowEpisodes: LiveData<List<FavoriteNextEpisode>> =
        Transformations.map(upnextDao.getFavoriteEpisodes()) {
            it.asDomainModel()
        }

    val traktAccessToken: LiveData<TraktAccessToken?> =
        Transformations.map(upnextDao.getTraktAccessData()) {
            it?.asDomainModel()
        }

    fun getTraktAccessTokenRaw(): DatabaseTraktAccess? = upnextDao.getTraktAccessDataRaw()

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
                    TraktNetwork.traktApi.getAccessTokenAsync(traktAccessTokenRequest).await()
                upnextDao.deleteTraktAccessData()
                upnextDao.insertAllTraktAccessData(accessTokenResponse.asDatabaseModel())
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
                revokeRequest?.let { TraktNetwork.traktApi.revokeAccessTokenAsync(it).await() }
                upnextDao.deleteTraktAccessData()
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
                    TraktNetwork.traktApi.getAccessRefreshTokenAsync(traktAccessTokenRequest)
                        .await()
                upnextDao.deleteTraktAccessData()
                upnextDao.insertAllTraktAccessData(accessTokenResponse.asDatabaseModel())
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
                        TraktNetwork.traktApi.getUserSettingsAsync(token = "Bearer $token").await()
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
                                    TraktNetwork.traktApi.getCustomListItemsAsync(
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
        upnextDao.deleteAllFavoriteShows()
        if (shows.isNotEmpty()) {
            upnextDao.insertAllFavoriteShows(*shows.toTypedArray())
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
            upnextDao.deleteAllFavoriteShows()
            upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_FAVORITE_SHOWS.tableName)
        }
    }

    suspend fun checkIfShowIsFavorite(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) return

        withContext(Dispatchers.IO) {
            val show = upnextDao.getFavoriteShow(imdbID)
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
                    TraktNetwork.traktApi.getUserSettingsAsync(token = "Bearer $token").await()
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
                            TraktNetwork.traktApi.getShowInfoAsync(imdbID).await()
                        if (showInfoResponse.ids?.trakt != null) {
                            val addShowToListRequest = NetworkTraktAddShowToListRequest(
                                shows = listOf(
                                    NetworkTraktAddShowToListRequestShow(
                                        ids = NetworkTraktAddShowToListRequestShowIds(trakt = showInfoResponse.ids.trakt)
                                    )
                                )
                            )
                            val addToListResponse = userSlug?.let {
                                TraktNetwork.traktApi.addShowToCustomListAsync(
                                    userSlug = it,
                                    traktId = favoritesListId,
                                    token = "Bearer $token",
                                    networkTraktAddShowToListRequest = addShowToListRequest
                                ).await()
                            }

                            if (addToListResponse?.added?.shows == 1) {
                                val customListItemsResponse =
                                    TraktNetwork.traktApi.getCustomListItemsAsync(
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
                    TraktNetwork.traktApi.getUserSettingsAsync(token = "Bearer $token").await()
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
                            TraktNetwork.traktApi.removeShowFromCustomListAsync(
                                userSlug = it,
                                traktId = favoritesListId,
                                token = "Bearer $token",
                                networkTraktRemoveShowFromListRequest = removeShowFromListRequest
                            ).await()
                        }

                        if (removeFromListResponse?.deleted?.shows == 1) {
                            traktUserListItem.imdbID?.let { upnextDao.deleteFavoriteEpisode(it) }

                            val customListItemsResponse =
                                TraktNetwork.traktApi.getCustomListItemsAsync(
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
                intervalMins = TableUpdateInterval.TRAKT_FAVORITE_EPISODES.intervalMins
            )
        ) {
            withContext(Dispatchers.IO) {
                try {
                    val episodesList = mutableListOf<DatabaseFavoriteNextEpisode>()
                    val favoriteShows = upnextDao.getFavoriteShowsRaw()
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
                            upnextDao.deleteAllFavoriteEpisodes()
                            upnextDao.insertAllFavoriteNextEpisodes(*episodesList.toTypedArray())
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
            TraktNetwork.traktApi.getUserCustomListsAsync(
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
            TraktNetwork.traktApi.createCustomListAsync(
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
                val showRatingResponse = TraktNetwork.traktApi.getShowRatingsAsync(
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
                val showStatsResponse = TraktNetwork.traktApi.getShowStatsAsync(
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
                        TraktNetwork.traktApi.getTrendingShowsAsync().await()

                    if (!trendingShowsResponse.isEmpty()) {
                        for (item in trendingShowsResponse) {
                            val trendingItem: NetworkTraktTrendingShowsResponseItem = item

                            val (id, poster, heroImage) = getImages(trendingItem.show.ids.imdb)

                            trendingItem.show.originalImageUrl = poster
                            trendingItem.show.mediumImageUrl = heroImage
                            trendingItem.show.ids.tvMazeID = id

                            shows.add(trendingItem.asDatabaseModel())
                        }

                        upnextDao.apply {
                            deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_TRENDING.tableName)
                            deleteAllTraktTrending()
                            insertAllTraktTrending(*shows.toTypedArray())
                            insertTableUpdateLog(
                                DatabaseTableUpdate(
                                    table_name = DatabaseTables.TABLE_TRAKT_TRENDING.tableName,
                                    last_updated = System.currentTimeMillis()
                                )
                            )
                        }
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
                TvMazeNetwork.tvMazeApi.getShowLookupAsync(it).await()
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
                TvMazeNetwork.tvMazeApi.getShowImagesAsync(tvMazeSearch?.id.toString())
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

                    val popularShowsResponse = TraktNetwork.traktApi.getPopularShowsAsync().await()

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
                        upnextDao.apply {
                            deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_POPULAR.tableName)
                            deleteAllTraktPopular()
                            insertAllTraktPopular(*shows.toTypedArray())
                            insertTableUpdateLog(
                                DatabaseTableUpdate(
                                    table_name = DatabaseTables.TABLE_TRAKT_POPULAR.tableName,
                                    last_updated = System.currentTimeMillis()
                                )
                            )
                        }
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
                        TraktNetwork.traktApi.getMostAnticipatedShowsAsync().await()

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
                        upnextDao.apply {
                            deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName)
                            deleteAllTraktMostAnticipated()
                            insertAllTraktMostAnticipated(*shows.toTypedArray())
                            insertTableUpdateLog(
                                DatabaseTableUpdate(
                                    table_name = DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName,
                                    last_updated = System.currentTimeMillis()
                                )
                            )
                        }
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

    private fun logTraktException(message: String) {
        Timber.d(Throwable(message = message))
        firebaseCrashlytics
            .recordException(Throwable(message = "TraktRepository: $message"))
    }

    companion object {
        const val FAVORITES_LIST_NAME = "Upnext Favorites"
    }
}