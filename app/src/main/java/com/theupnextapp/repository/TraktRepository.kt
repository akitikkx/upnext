package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.BuildConfig
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.database.*
import com.theupnextapp.domain.*
import com.theupnextapp.network.TraktNetwork
import com.theupnextapp.network.TvMazeNetwork
import com.theupnextapp.network.UpnextKtorNetwork
import com.theupnextapp.network.models.trakt.*
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchResponse
import com.theupnextapp.network.models.upnextktor.NetworkUpnextKtorShowTrendingResponseItem
import com.theupnextapp.network.models.upnextktor.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.net.ssl.SSLHandshakeException

class TraktRepository constructor(
    private val upnextDao: UpnextDao,
    private val firebaseCrashlytics: FirebaseCrashlytics
) {

    val traktWatchlist: LiveData<List<TraktWatchlist>> =
        Transformations.map(upnextDao.getTraktWatchlist()) {
            it.asDomainModel()
        }

    val traktHistory: LiveData<List<TraktHistory>> =
        Transformations.map(upnextDao.getTraktHistory()) {
            it.asDomainModel()
        }

    fun traktWatchlistItem(imdbID: String): LiveData<TraktHistory?> =
        Transformations.map(upnextDao.checkIfInTraktWatchlist(imdbID)) { result ->
            result?.asDomainModel()
        }

    fun tableUpdate(tableName: String): LiveData<TableUpdate?> =
        Transformations.map(upnextDao.getTableLastUpdate(tableName)) {
            it?.asDomainModel()
        }

    val traktRecommendations: LiveData<List<TraktRecommendations>> =
        Transformations.map(upnextDao.getTraktRecommendations()) {
            it.asDomainModel()
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _traktAccessToken = MutableLiveData<TraktAccessToken?>()
    val traktAccessToken: LiveData<TraktAccessToken?> = _traktAccessToken

    private val _isLoadingTraktWatchlist = MutableLiveData<Boolean>()
    val isLoadingTraktWatchlist: LiveData<Boolean> = _isLoadingTraktWatchlist

    private val _isLoadingTraktHistory = MutableLiveData<Boolean>()
    val isLoadingTraktHistory: LiveData<Boolean> = _isLoadingTraktHistory

    private val _isLoadingTraktRecommendations = MutableLiveData<Boolean>()
    val isLoadingTraktRecommendations: LiveData<Boolean> = _isLoadingTraktRecommendations

    private val _isLoadingTraktTrending = MutableLiveData<Boolean>()
    val isLoadingTraktTrending: LiveData<Boolean> = _isLoadingTraktTrending

    private val _isLoadingTraktPopular = MutableLiveData<Boolean>()
    val isLoadingTraktPopular: LiveData<Boolean> = _isLoadingTraktPopular

    private val _isLoadingTraktMostAnticipated = MutableLiveData<Boolean>()
    val isLoadingTraktMostAnticipated: LiveData<Boolean> = _isLoadingTraktMostAnticipated

    private val _isRemovingTraktWatchlist = MutableLiveData<Boolean>()
    val isRemovingTraktWatchlist: LiveData<Boolean> = _isRemovingTraktWatchlist

    private val _isRemovingTraktHistory = MutableLiveData<Boolean>()
    val isRemovingTraktHistory: LiveData<Boolean> = _isRemovingTraktHistory

    private val _isRemovingTraktRecommendations = MutableLiveData<Boolean>()
    val isRemovingTraktRecommendations: LiveData<Boolean> = _isRemovingTraktRecommendations

    private val _addToWatchlistResponse = MutableLiveData<TraktAddToWatchlist>()
    val addToWatchlistResponse: LiveData<TraktAddToWatchlist> = _addToWatchlistResponse

    private val _removeFromWatchlistResponse = MutableLiveData<TraktRemoveFromWatchlist>()
    val removeFromWatchlistResponse: LiveData<TraktRemoveFromWatchlist> =
        _removeFromWatchlistResponse

    private val _addToHistoryResponse = MutableLiveData<TraktAddToHistory>()
    val addToHistoryResponse: LiveData<TraktAddToHistory> = _addToHistoryResponse

    private val _removeFromHistoryResponse = MutableLiveData<TraktRemoveFromHistory>()
    val removeFromHistoryResponse: LiveData<TraktRemoveFromHistory> = _removeFromHistoryResponse

    private val _traktShowRating = MutableLiveData<TraktShowRating>()
    val traktShowRating: LiveData<TraktShowRating> = _traktShowRating

    private val _traktShowStats = MutableLiveData<TraktShowStats>()
    val traktShowStats: LiveData<TraktShowStats> = _traktShowStats

    private val _traktWatchedProgress = MutableLiveData<TraktShowWatchedProgress>()
    val traktWatchedProgress: LiveData<TraktShowWatchedProgress> = _traktWatchedProgress

    private val _invalidToken = MutableLiveData<Boolean>()
    val invalidToken: LiveData<Boolean> = _invalidToken

    private val _invalidGrant = MutableLiveData<Boolean>()
    val invalidGrant: LiveData<Boolean> = _invalidGrant

    suspend fun clearAllTraktData() {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                removeAllWatchlistData()
                removeAllHistoryData()
                removeAllRecommendationsData()
                _isLoading.postValue(false)
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

    private suspend fun removeAllWatchlistData() {
        withContext(Dispatchers.IO) {
            _isRemovingTraktWatchlist.postValue(true)
            upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_WATCHLIST.tableName)
            upnextDao.deleteAllTraktWatchlist()
            _isRemovingTraktWatchlist.postValue(false)
        }
    }

    private suspend fun removeAllHistoryData() {
        withContext(Dispatchers.IO) {
            _isRemovingTraktHistory.postValue(true)
            upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_HISTORY.tableName)
            upnextDao.deleteAllTraktHistory()
            _isRemovingTraktHistory.postValue(false)
        }
    }

    private suspend fun removeAllRecommendationsData() {
        withContext(Dispatchers.IO) {
            _isRemovingTraktRecommendations.postValue(true)
            upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_RECOMMENDATIONS.tableName)
            upnextDao.deleteAllTraktRecommendations()
            _isRemovingTraktRecommendations.postValue(false)
        }
    }

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
                _traktAccessToken.postValue(accessTokenResponse.asDomainModel())
                _isLoading.postValue(false)
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
                _traktAccessToken.postValue(accessTokenResponse.asDomainModel())
                _isLoading.postValue(false)
                _traktAccessToken.postValue(null)
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

    suspend fun refreshTraktWatchlist(accessToken: String?) {
        if (accessToken.isNullOrEmpty()) {
            logTraktException("Could not get the watchlist due to a null access token")
            return
        }
        val updatedWatchList: MutableList<DatabaseTraktWatchlist> = arrayListOf()

        withContext(Dispatchers.IO) {
            try {
                _isLoadingTraktWatchlist.postValue(true)

                val watchlistResponse = TraktNetwork.traktApi.getWatchlistAsync(
                    token = "Bearer $accessToken"
                ).await()

                // loop through each watchlist item to get the title and IMDB link
                if (!watchlistResponse.isNullOrEmpty()) {
                    upnextDao.deleteAllTraktWatchlist()
                    upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_WATCHLIST.tableName)

                    for (item in watchlistResponse) {
                        val watchListItem: NetworkTraktWatchlistResponseItem = item
                        val imdbID = watchListItem.show?.ids?.imdb
                        val traktTitle = watchListItem.show?.title

                        // perform a TvMaze search for the Trakt item using the Trakt title
                        val tvMazeSearch =
                            traktTitle?.let {
                                TvMazeNetwork.tvMazeApi.getSuggestionListAsync(it).await()
                            }

                        // loop through the search results from TvMaze and find a match for the IMDb ID
                        // and update the watchlist item by adding the TvMaze ID
                        if (!tvMazeSearch.isNullOrEmpty()) {
                            for (searchItem in tvMazeSearch) {
                                val showSearchItem: NetworkShowSearchResponse = searchItem
                                if (showSearchItem.show.externals.imdb == imdbID) {
                                    watchListItem.show.originalImageUrl =
                                        showSearchItem.show.image?.medium
                                    watchListItem.show.mediumImageUrl =
                                        showSearchItem.show.image?.original
                                    watchListItem.show.ids?.tvMaze = showSearchItem.show.id
                                }
                            }
                        }
                        updatedWatchList.add(watchListItem.asDatabaseModel())
                    }
                    upnextDao.insertAllTraktWatchlist(*updatedWatchList.toTypedArray())
                    upnextDao.insertTableUpdateLog(
                        DatabaseTableUpdate(
                            table_name = DatabaseTables.TABLE_TRAKT_WATCHLIST.tableName,
                            last_updated = System.currentTimeMillis()
                        )
                    )
                }
                _isLoadingTraktWatchlist.postValue(false)
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

    suspend fun removeFromCachedWatchlist(imdbID: String) {
        withContext(Dispatchers.IO) {
            try {
                upnextDao.deleteWatchlistItem(imdbID)
            } catch (e: Exception) {
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshTraktHistory(accessToken: String?) {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingTraktHistory.postValue(true)
                val updatedHistoryList: MutableList<DatabaseTraktHistory> = arrayListOf()

                val historyResponse = TraktNetwork.traktApi.getHistoryAsync(
                    token = "Bearer $accessToken"
                ).await()

                // loop through each watchlist item to get the title and IMDB link
                if (!historyResponse.isNullOrEmpty()) {
                    for (item in historyResponse) {
                        val historyItem: NetworkTraktHistoryResponseItem = item
                        if (item.type == "episode") {
                            val imdbID = historyItem.show?.ids?.imdb
                            val traktTitle = historyItem.show?.title

                            // perform a TvMaze search for the Trakt item using the Trakt title
                            val tvMazeSearch =
                                traktTitle?.let {
                                    TvMazeNetwork.tvMazeApi.getSuggestionListAsync(it).await()
                                }

                            // loop through the search results from TvMaze and find a match for the IMDb ID
                            // and update the watchlist item by adding the TvMaze ID
                            if (!tvMazeSearch.isNullOrEmpty()) {
                                for (searchItem in tvMazeSearch) {
                                    val showSearchItem: NetworkShowSearchResponse = searchItem
                                    if (showSearchItem.show.externals.imdb == imdbID) {
                                        historyItem.show.originalImageUrl =
                                            showSearchItem.show.image?.medium
                                        historyItem.show.mediumImageUrl =
                                            showSearchItem.show.image?.original
                                        historyItem.show.ids?.tvMaze = showSearchItem.show.id
                                    }
                                }
                            }
                            updatedHistoryList.add(historyItem.asDatabaseModel())
                        }
                    }
                    saveTraktHistory(updatedHistoryList)
                }
                _isLoadingTraktHistory.postValue(false)
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

    private suspend fun saveTraktHistory(list: List<DatabaseTraktHistory>) {
        if (!list.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    upnextDao.apply {
                        deleteAllTraktHistory()
                        insertAllTraktHistory(*list.toTypedArray())
                        deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_HISTORY.tableName)
                        insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_TRAKT_HISTORY.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }

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
    }

    suspend fun refreshTraktWatched(accessToken: String?) {
        withContext(Dispatchers.IO) {
            try {
                val watchlistResponse = TraktNetwork.traktApi.getWatchedAsync(
                    token = "Bearer $accessToken"
                ).await()
                Timber.d(watchlistResponse.toString())
            } catch (e: Exception) {
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun traktAddToWatchlist(accessToken: String?, imdbID: String) {
        if (accessToken.isNullOrEmpty()) {
            logTraktException("Could not get the watched progress due to a null access token")
        }
        traktPerformWatchlistAction(accessToken, imdbID, WATCHLIST_ACTION_ADD)
    }

    suspend fun traktRemoveFromWatchlist(accessToken: String?, imdbID: String) {
        traktPerformWatchlistAction(accessToken, imdbID, WATCHLIST_ACTION_REMOVE)
    }

    private suspend fun traktPerformWatchlistAction(
        accessToken: String?,
        imdbID: String,
        action: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val addToWatchlistShowsList: MutableList<NetworkTraktAddToWatchlistRequestShow> =
                    arrayListOf()
                val removeFromWatchlistShowsList: MutableList<NetworkTraktRemoveFromWatchlistRequestShow> =
                    arrayListOf()

                val searchList = TraktNetwork.traktApi.getIDLookupAsync(
                    token = "Bearer $accessToken",
                    id = imdbID,
                    type = "show"
                ).await()

                if (!searchList.isNullOrEmpty()) {
                    when (action) {
                        WATCHLIST_ACTION_ADD -> {
                            performAddToWatchlistRequestAction(
                                accessToken = accessToken,
                                response = searchList,
                                addToWatchlistShowsList = addToWatchlistShowsList
                            )
                        }
                        WATCHLIST_ACTION_REMOVE -> {
                            performRemoveFromWatchlistRequestAction(
                                accessToken = accessToken,
                                response = searchList,
                                removeFromWatchlistShowsList = removeFromWatchlistShowsList
                            )
                        }
                    }
                }
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

    private suspend fun performAddToWatchlistRequestAction(
        accessToken: String?,
        response: NetworkTraktIDLookupResponse,
        addToWatchlistShowsList: MutableList<NetworkTraktAddToWatchlistRequestShow>
    ) {
        for (item in response) {
            val watchlistItem: NetworkTraktIDLookupResponseItem = item
            val addToWatchlistItem =
                NetworkTraktAddToWatchlistRequestShow(
                    ids = watchlistItem.show.ids,
                    title = watchlistItem.show.title,
                    year = watchlistItem.show.year
                )
            addToWatchlistShowsList.add(addToWatchlistItem)
            val addToWatchlist = TraktNetwork.traktApi.addToWatchlistAsync(
                token = "Bearer $accessToken",
                request = NetworkTraktAddToWatchlistRequest(
                    shows = addToWatchlistShowsList,
                    movies = emptyList(),
                    episodes = emptyList()
                )
            ).await()
            _addToWatchlistResponse.postValue(addToWatchlist.asDomainModel())
        }
    }

    private suspend fun performRemoveFromWatchlistRequestAction(
        accessToken: String?,
        response: NetworkTraktIDLookupResponse,
        removeFromWatchlistShowsList: MutableList<NetworkTraktRemoveFromWatchlistRequestShow>
    ) {
        for (item in response) {
            val watchlistItem: NetworkTraktIDLookupResponseItem = item
            val removeFromWatchlistItem =
                NetworkTraktRemoveFromWatchlistRequestShow(
                    ids = watchlistItem.show.ids,
                    title = watchlistItem.show.title,
                    year = watchlistItem.show.year
                )
            removeFromWatchlistShowsList.add(removeFromWatchlistItem)
            val removeFromWatchlist =
                TraktNetwork.traktApi.removeFromWatchlistAsync(
                    token = "Bearer $accessToken",
                    request = NetworkTraktRemoveFromWatchlistRequest(
                        shows = removeFromWatchlistShowsList,
                        movies = emptyList()
                    )
                ).await()
            _removeFromWatchlistResponse.postValue(removeFromWatchlist.asDomainModel())
        }
    }

    suspend fun traktAddSeasonToHistory(
        accessToken: String?,
        imdbID: String,
        showSeason: ShowSeason
    ) {
        val season = NetworkTraktAddToHistoryRequestSeasonX(
            number = showSeason.seasonNumber,
            watched_at = null
        )

        traktPerformHistorySeasonAction(
            accessToken,
            imdbID,
            HISTORY_ACTION_ADD,
            seasonToAdd = season
        )
    }

    suspend fun traktRemoveSeasonFromHistory(
        accessToken: String?,
        imdbID: String,
        showSeason: ShowSeason
    ) {
        val season = NetworkTraktRemoveSeasonFromHistoryRequestSeasonX(
            number = showSeason.seasonNumber
        )

        traktPerformHistorySeasonAction(
            accessToken = accessToken,
            imdbID = imdbID,
            action = HISTORY_ACTION_REMOVE,
            seasonToRemove = season
        )
    }

    private suspend fun traktPerformHistorySeasonAction(
        accessToken: String?,
        imdbID: String,
        action: String,
        seasonToAdd: NetworkTraktAddToHistoryRequestSeasonX? = null,
        seasonToRemove: NetworkTraktRemoveSeasonFromHistoryRequestSeasonX? = null
    ) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)

                val searchList = TraktNetwork.traktApi.getIDLookupAsync(
                    token = "Bearer $accessToken",
                    id = imdbID,
                    type = "show"
                ).await()

                if (!searchList.isNullOrEmpty()) {
                    when (action) {
                        HISTORY_ACTION_ADD -> {
                            performAddToHistorySeasonRequestAction(
                                accessToken = accessToken,
                                response = searchList,
                                seasonToAdd = seasonToAdd
                            )
                        }
                        HISTORY_ACTION_REMOVE -> {
                            performRemoveFromHistorySeasonRequestAction(
                                accessToken = accessToken,
                                response = searchList,
                                seasonToRemove = seasonToRemove
                            )
                        }
                    }
                }
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

    private suspend fun performAddToHistorySeasonRequestAction(
        accessToken: String?,
        response: NetworkTraktIDLookupResponse,
        seasonToAdd: NetworkTraktAddToHistoryRequestSeasonX?
    ) {
        val addToHistoryShowsList: MutableList<NetworkTraktAddToHistoryRequestShow> =
            arrayListOf()

        for (item in response) {
            val historyItem: NetworkTraktIDLookupResponseItem = item
            val addToHistoryListItem =
                NetworkTraktAddToHistoryRequestShow(
                    ids = historyItem.show.ids,
                    title = historyItem.show.title,
                    year = historyItem.show.year,
                    seasons = listOf(seasonToAdd)
                )
            addToHistoryShowsList.add(addToHistoryListItem)
            val addToHistoryList = TraktNetwork.traktApi.addToHistoryAsync(
                token = "Bearer $accessToken",
                request = NetworkTraktAddToHistoryRequest(
                    shows = addToHistoryShowsList,
                    movies = emptyList(),
                    seasons = emptyList()
                )
            ).await()
            _addToHistoryResponse.postValue(addToHistoryList.asDomainModel())
        }
    }

    private suspend fun performRemoveFromHistorySeasonRequestAction(
        accessToken: String?,
        response: NetworkTraktIDLookupResponse,
        seasonToRemove: NetworkTraktRemoveSeasonFromHistoryRequestSeasonX?
    ) {
        val removeFromHistoryShowsList: MutableList<NetworkTraktRemoveSeasonFromHistoryRequestShow> =
            arrayListOf()

        for (item in response) {
            val historyItem: NetworkTraktIDLookupResponseItem = item
            val removeFromHistoryListItem =
                NetworkTraktRemoveSeasonFromHistoryRequestShow(
                    ids = historyItem.show.ids,
                    title = historyItem.show.title,
                    year = historyItem.show.year,
                    seasons = listOf(seasonToRemove)
                )
            removeFromHistoryShowsList.add(removeFromHistoryListItem)
            val removeFromHistoryList = TraktNetwork.traktApi.removeFromHistoryAsync(
                token = "Bearer $accessToken",
                request = NetworkTraktRemoveSeasonFromHistoryRequest(
                    shows = removeFromHistoryShowsList,
                    movies = emptyList(),
                    seasons = emptyList(),
                    ids = emptyList()
                )
            ).await()
            _removeFromHistoryResponse.postValue(removeFromHistoryList.asDomainModel())
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
                _isLoadingTraktTrending.postValue(true)
                val shows: MutableList<DatabaseTraktTrendingShows> = mutableListOf()

                val trendingShowsResponse =
                    UpnextKtorNetwork.upnextKtorApi.getTrendingShowsAsync().await()

                if (!trendingShowsResponse.isEmpty()) {
                    upnextDao.apply {
                        deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_TRENDING.tableName)
                        insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_TRAKT_TRENDING.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }

                    for (item in trendingShowsResponse) {
                        val trendingItem: NetworkUpnextKtorShowTrendingResponseItem = item
                        shows.add(trendingItem.asDatabaseModel())
                    }
                    upnextDao.apply {
                        deleteAllTraktTrending()
                        insertAllTraktTrending(*shows.toTypedArray())
                    }
                }
                _isLoadingTraktTrending.postValue(false)
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

    suspend fun getTraktWatchedProgress(
        accessToken: String?,
        imdbID: String?
    ) {
        if (imdbID.isNullOrEmpty()) {
            logTraktException("Could not get the watched progress due to a null imdb ID")
            return
        }

        if (accessToken.isNullOrEmpty()) {
            logTraktException("Could not get the watched progress due to a null access token")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showWatchedProgress = TraktNetwork.traktApi.getShowWatchedProgressAsync(
                    token = "Bearer $accessToken",
                    id = imdbID
                ).await()
                _traktWatchedProgress.postValue(showWatchedProgress.asDomainModel())
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                handleTraktError(e)
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

    suspend fun refreshTraktRecommendations(accessToken: String?) {
        if (accessToken.isNullOrEmpty()) {
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoadingTraktRecommendations.postValue(true)
                val shows: MutableList<DatabaseTraktRecommendations> = mutableListOf()
                val recommendationsResponse =
                    TraktNetwork.traktApi.getRecommendationsAsync(token = "Bearer $accessToken")
                        .await()
                if (!recommendationsResponse.isNullOrEmpty()) {
                    upnextDao.apply {
                        deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_RECOMMENDATIONS.tableName)
                        deleteAllTraktRecommendations()
                    }

                    for (item in recommendationsResponse) {
                        val recommendationItem: NetworkTraktRecommendationsItem = item

                        if (item.ids?.trakt != null) {
                            val imdbID = recommendationItem.ids?.imdb
                            val traktTitle = recommendationItem.title

                            // perform a TvMaze search for the Trakt item using the Trakt title
                            val tvMazeSearch =
                                traktTitle?.let { title ->
                                    TvMazeNetwork.tvMazeApi.getSuggestionListAsync(title).await()
                                }

                            // loop through the search results from TvMaze and find a match for the IMDb ID
                            // and update the watchlist item by adding the TvMaze ID
                            if (!tvMazeSearch.isNullOrEmpty()) {
                                for (searchItem in tvMazeSearch) {
                                    val showSearchItem: NetworkShowSearchResponse = searchItem
                                    if (showSearchItem.show.externals.imdb == imdbID) {
                                        recommendationItem.originalImageUrl =
                                            showSearchItem.show.image?.medium
                                        recommendationItem.mediumImageUrl =
                                            showSearchItem.show.image?.original
                                        recommendationItem.ids?.tvMazeID = showSearchItem.show.id
                                    }
                                }
                            }
                            shows.add(recommendationItem.asDatabaseModel())
                        }
                    }
                    upnextDao.apply {
                        insertAllTraktRecommendations(*shows.toTypedArray())
                        insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_TRAKT_RECOMMENDATIONS.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }
                }
                _isLoadingTraktRecommendations.postValue(false)
            } catch (e: Exception) {
                _isLoadingTraktRecommendations.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoadingTraktRecommendations.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoadingTraktRecommendations.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshTraktPopularShows() {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingTraktPopular.postValue(true)
                val shows: MutableList<DatabaseTraktPopularShows> = mutableListOf()

                val popularShowsResponse = TraktNetwork.traktApi.getPopularShowsAsync().await()

                if (!popularShowsResponse.isEmpty()) {
                    upnextDao.apply {
                        deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_POPULAR.tableName)
                        deleteAllTraktPopular()
                    }

                    for (item in popularShowsResponse) {
                        val popularItem: NetworkTraktPopularShowsResponseItem = item

                        val imdbID = popularItem.ids.imdb
                        val traktTitle = popularItem.title

                        // perform a TvMaze search for the Trakt item using the Trakt title
                        val tvMazeSearch =
                            traktTitle?.let {
                                TvMazeNetwork.tvMazeApi.getSuggestionListAsync(it).await()
                            }

                        // loop through the search results from TvMaze and find a match for the IMDb ID
                        // and update the watchlist item by adding the TvMaze ID
                        if (!tvMazeSearch.isNullOrEmpty()) {
                            for (searchItem in tvMazeSearch) {
                                val showSearchItem: NetworkShowSearchResponse = searchItem
                                if (showSearchItem.show.externals.imdb == imdbID) {
                                    popularItem.originalImageUrl =
                                        showSearchItem.show.image?.medium
                                    popularItem.mediumImageUrl =
                                        showSearchItem.show.image?.original
                                    popularItem.ids.tvMazeID = showSearchItem.show.id
                                }
                            }
                        }
                        shows.add(popularItem.asDatabaseModel())
                    }
                    upnextDao.apply {
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
                _isLoadingTraktMostAnticipated.postValue(true)
                val shows: MutableList<DatabaseTraktMostAnticipated> = mutableListOf()

                val mostAnticipatedShowsResponse =
                    TraktNetwork.traktApi.getMostAnticipatedShowsAsync().await()

                if (!mostAnticipatedShowsResponse.isEmpty()) {
                    upnextDao.apply {
                        deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName)
                        deleteAllTraktMostAnticipated()
                    }

                    for (item in mostAnticipatedShowsResponse) {
                        val mostAnticipatedItem: NetworkTraktMostAnticipatedResponseItem = item

                        val imdbID = mostAnticipatedItem.show.ids.imdb
                        val traktTitle = mostAnticipatedItem.show.title

                        // perform a TvMaze search for the Trakt item using the Trakt title
                        val tvMazeSearch =
                            traktTitle?.let {
                                TvMazeNetwork.tvMazeApi.getSuggestionListAsync(it).await()
                            }

                        // loop through the search results from TvMaze and find a match for the IMDb ID
                        // and update the watchlist item by adding the TvMaze ID
                        if (!tvMazeSearch.isNullOrEmpty()) {
                            for (searchItem in tvMazeSearch) {
                                val showSearchItem: NetworkShowSearchResponse = searchItem
                                if (showSearchItem.show.externals.imdb == imdbID) {
                                    mostAnticipatedItem.show.originalImageUrl =
                                        showSearchItem.show.image?.medium
                                    mostAnticipatedItem.show.mediumImageUrl =
                                        showSearchItem.show.image?.original
                                    mostAnticipatedItem.show.ids.tvMazeID = showSearchItem.show.id
                                }
                            }
                        }
                        shows.add(mostAnticipatedItem.asDatabaseModel())
                    }
                    upnextDao.apply {
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

    private fun handleTraktError(exception: HttpException) {
        when (exception.code()) {
            401 -> {
                when (exception.message) {
                    TRAKT_ERROR_INVALID_TOKEN -> {
                        _invalidToken.postValue(true)
                    }
                    TRAKT_ERROR_INVALID_GRANT -> {
                        _invalidGrant.postValue(true)
                    }
                    else -> {
                        _invalidToken.postValue(true)
                    }
                }
            }
        }
    }

    private fun logTraktException(message: String) {
        Timber.d(Throwable(message = message))
        firebaseCrashlytics
            .recordException(Throwable(message = "TraktRepository: $message"))
    }

    companion object {
        const val WATCHLIST_ACTION_ADD = "add_to_watchlist"
        const val WATCHLIST_ACTION_REMOVE = "remove_from_watchlist"
        const val HISTORY_ACTION_ADD = "add_to_history"
        const val HISTORY_ACTION_REMOVE = "remove_from_history"

        const val TRAKT_ERROR_INVALID_TOKEN = "invalid_token"
        const val TRAKT_ERROR_INVALID_GRANT = "invalid_grant"
    }
}