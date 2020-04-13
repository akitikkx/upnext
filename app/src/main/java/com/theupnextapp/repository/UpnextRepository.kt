package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.crashlytics.android.Crashlytics
import com.theupnextapp.BuildConfig
import com.theupnextapp.database.*
import com.theupnextapp.domain.*
import com.theupnextapp.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

class UpnextRepository(private val database: UpnextDatabase) {

    val recommendedShows: LiveData<List<RecommendedShows>> =
        Transformations.map(database.upnextDao.getRecommendedShows()) {
            it.asDomainModel()
        }

    val newShows: LiveData<List<NewShows>> =
        Transformations.map(database.upnextDao.getNewShows()) {
            it.asDomainModel()
        }

    val yesterdayShows: LiveData<List<ScheduleShow>> =
        Transformations.map(database.upnextDao.getYesterdayShows()) {
            it.asDomainModel()
        }

    val todayShows: LiveData<List<ScheduleShow>> =
        Transformations.map(database.upnextDao.getTodayShows()) {
            it.asDomainModel()
        }

    val tomorrowShows: LiveData<List<ScheduleShow>> =
        Transformations.map(database.upnextDao.getTomorrowShows()) {
            it.asDomainModel()
        }

    val traktWatchlist: LiveData<List<TraktWatchlist>> =
        Transformations.map(database.upnextDao.getTraktWatchlist()) {
            it.asDomainModel()
        }

    val traktHistory: LiveData<List<TraktHistory>> =
        Transformations.map(database.upnextDao.getTraktHistory()) {
            it.asDomainModel()
        }

    fun traktWatchlistItem(imdbID: String?): LiveData<TraktHistory> =
        Transformations.map(database.upnextDao.checkifInTraktWatchlist(imdbID)) {
            it.asDomainModel()
        }

    private val _showInfo = MutableLiveData<ShowInfo>()

    private val _showSearch = MutableLiveData<List<ShowSearch>>()

    private val _isLoading = MutableLiveData<Boolean>()

    private val _isLoadingRecommendedShows = MutableLiveData<Boolean>()

    private val _isLoadingNewShows = MutableLiveData<Boolean>()

    private val _isLoadingYesterdayShows = MutableLiveData<Boolean>()

    private val _isLoadingTodayShows = MutableLiveData<Boolean>()

    private val _isLoadingTomorrowShows = MutableLiveData<Boolean>()

    private val _isLoadingTraktWatchlist = MutableLiveData<Boolean>()

    private val _isLoadingTraktHistory = MutableLiveData<Boolean>()

    private val _traktAccessToken = MutableLiveData<TraktAccessToken>()

    private val _traktCollection = MutableLiveData<TraktCollection>()

    private val _traktHistory = MutableLiveData<TraktHistory>()

    val isLoading: LiveData<Boolean>
        get() = _isLoading

    val isLoadingRecommendedShows: LiveData<Boolean>
        get() = _isLoadingRecommendedShows

    val isLoadingNewShows: LiveData<Boolean>
        get() = _isLoadingNewShows

    val isLoadingYesterdayShows: LiveData<Boolean>
        get() = _isLoadingYesterdayShows

    val isLoadingTodayShows: LiveData<Boolean>
        get() = _isLoadingTodayShows

    val isLoadingTomorrowShows: LiveData<Boolean>
        get() = _isLoadingTomorrowShows

    val isLoadingTraktWatchlist: LiveData<Boolean>
        get() = _isLoadingTraktWatchlist

    val isLoadingTraktHistory: LiveData<Boolean>
        get() = _isLoadingTraktHistory

    val showInfo: LiveData<ShowInfo>
        get() = _showInfo

    val showSearch: LiveData<List<ShowSearch>>
        get() = _showSearch

    val traktAccessToken: LiveData<TraktAccessToken>
        get() = _traktAccessToken

    suspend fun getSearchSuggestions(name: String?) {
        if (!name.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val searchList = TvMazeNetwork.tvMazeApi.getSuggestionListAsync(name).await()
                    _showSearch.postValue(searchList.asDomainModel())
                } catch (e: Exception) {
                    Timber.d(e)
                    Crashlytics.logException(e)
                }
            }
        }
    }

    suspend fun getTraktAccessToken(code: String?) {
        if (!code.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
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
                } catch (e: Exception) {
                    Timber.d(e)
                    Crashlytics.logException(e)
                }
            }
        }
    }

    suspend fun refreshTraktWatchlist(accessToken: String?) {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingTraktWatchlist.postValue(true)
                val updatedWatchList: MutableList<DatabaseTraktWatchlist> = arrayListOf()

                val watchlistResponse = TraktNetwork.traktApi.getWatchlistAsync(
                    contentType = "application/json",
                    token = "Bearer $accessToken",
                    version = "2",
                    apiKey = BuildConfig.TRAKT_CLIENT_ID
                ).await()

                // loop through each watchlist item to get the title and IMDB link
                for (item in watchlistResponse) {
                    var watchListItem: NetworkTraktWatchlistResponseItem = item
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
                                watchListItem.show?.originalImageUrl =
                                    showSearchItem.show.image?.medium
                                watchListItem.show?.mediumImageUrl =
                                    showSearchItem.show.image?.original
                                watchListItem.show?.ids?.tvMaze = showSearchItem.show.id
                            }
                        }
                    }
                    updatedWatchList.add(watchListItem.asDatabaseModel())
                }
                saveTraktWatchlist(updatedWatchList)
                _isLoadingTraktWatchlist.postValue(false)
            } catch (e: Exception) {
                Timber.d(e)
                Crashlytics.logException(e)
                _isLoadingTraktWatchlist.postValue(false)
            }
        }
    }

    private suspend fun saveTraktWatchlist(list: List<DatabaseTraktWatchlist>) {
        if (!list.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    database.upnextDao.apply {
                        deleteAllTraktWatchlist()
                        insertAllTraktWatchlist(*list.toTypedArray())
                    }

                } catch (e: Exception) {
                    Timber.d(e)
                    Crashlytics.logException(e)
                }
            }
        }
    }

    suspend fun refreshTraktHistory(accessToken: String?) {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingTraktHistory.postValue(true)
                val updatedHistoryList: MutableList<DatabaseTraktHistory> = arrayListOf()

                val historyResponse = TraktNetwork.traktApi.getHistoryAsync(
                    contentType = "application/json",
                    token = "Bearer $accessToken",
                    version = "2",
                    apiKey = BuildConfig.TRAKT_CLIENT_ID
                ).await()

                // loop through each watchlist item to get the title and IMDB link
                for (item in historyResponse) {
                    var historyItem: NetworkTraktHistoryResponseItem = item
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
                                    historyItem.show?.originalImageUrl =
                                        showSearchItem.show.image?.medium
                                    historyItem.show?.mediumImageUrl =
                                        showSearchItem.show.image?.original
                                    historyItem.show?.ids?.tvMaze = showSearchItem.show.id
                                }
                            }
                        }
                        updatedHistoryList.add(historyItem.asDatabaseModel())
                    }
                    saveTraktHistory(updatedHistoryList)
                }
                _isLoadingTraktHistory.postValue(false)
            } catch (e: Exception) {
                Timber.d(e)
                Crashlytics.logException(e)
                _isLoadingTraktHistory.postValue(false)
            }
        }
    }

    private suspend fun saveTraktHistory(list: List<DatabaseTraktHistory>) {
        if (!list.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    database.upnextDao.apply {
                        deleteAllTraktHistory()
                        insertAllTraktHistory(*list.toTypedArray())
                    }

                } catch (e: Exception) {
                    Timber.d(e)
                    Crashlytics.logException(e)
                }
            }
        }
    }

    suspend fun refreshTraktWatched(accessToken: String?) {
        withContext(Dispatchers.IO) {
            try {
                val watchlistResponse = TraktNetwork.traktApi.getWatchedAsync(
                    contentType = "application/json",
                    token = "Bearer $accessToken",
                    version = "2",
                    apiKey = BuildConfig.TRAKT_CLIENT_ID
                ).await()
                Timber.d(watchlistResponse.toString())
            } catch (e: HttpException) {
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    suspend fun refreshTraktCollection(accessToken: String?) {
        withContext(Dispatchers.IO) {
            try {
                val collectionResponse = TraktNetwork.traktApi.getCollectionAsync(
                    contentType = "application/json",
                    token = "Bearer $accessToken",
                    version = "2",
                    apiKey = BuildConfig.TRAKT_CLIENT_ID
                ).await()
            } catch (e: Exception) {
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    suspend fun refreshRecommendedShows() {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingRecommendedShows.postValue(true)
                val recommendedShowsList =
                    UpnextNetwork.upnextApi.getRecommendedShowsAsync().await()

                saveRecommendedShows(recommendedShowsList)

                _isLoadingRecommendedShows.postValue(false)
            } catch (e: Exception) {
                _isLoadingRecommendedShows.postValue(false)
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    private suspend fun saveRecommendedShows(recommendedShowsList: NetworkRecommendedShowsResponse) {
        withContext(Dispatchers.IO) {
            try {
                database.upnextDao.apply {
                    deleteAllRecommendedShows()
                    insertAllRecommendedShows(*recommendedShowsList.asDatabaseModel())
                }
            } catch (e: Exception) {
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    suspend fun refreshNewShows() {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingNewShows.postValue(true)
                val newShowsList = UpnextNetwork.upnextApi.getNewShowsAsync().await()
                database.upnextDao.apply {
                    deleteAllNewShows()
                    insertAllNewShows(*newShowsList.asDatabaseModel())
                }
                _isLoadingNewShows.postValue(false)
            } catch (e: Exception) {
                _isLoadingNewShows.postValue(false)
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    suspend fun refreshYesterdayShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingYesterdayShows.postValue(true)
                val shows: MutableList<DatabaseYesterdaySchedule> = arrayListOf()
                val yesterdayShowsList =
                    TvMazeNetwork.tvMazeApi.getYesterdayScheduleAsync(countryCode, date).await()
                if (!yesterdayShowsList.isNullOrEmpty()) {
                    database.upnextDao.apply {
                        deleteAllYesterdayShows()
                        yesterdayShowsList.forEach {
                            // only adding shows that have an image
                            if (!it.show.image?.original.isNullOrEmpty()) {
                                shows.add(it.asDatabaseModel())
                                insertAllYesterdayShows(*shows.toTypedArray())
                            }
                        }
                    }
                }
                _isLoadingYesterdayShows.postValue(false)
            } catch (e: Exception) {
                _isLoadingYesterdayShows.postValue(false)
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    suspend fun refreshTodayShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingTodayShows.postValue(true)
                val shows: MutableList<DatabaseTodaySchedule> = arrayListOf()
                val todayShowsList =
                    TvMazeNetwork.tvMazeApi.getTodayScheduleAsync(countryCode, date).await()
                if (!todayShowsList.isNullOrEmpty()) {
                    database.upnextDao.apply {
                        deleteAllTodayShows()
                        todayShowsList.forEach {
                            // only adding shows that have an image
                            if (!it.show.image?.original.isNullOrEmpty()) {
                                shows.add(it.asDatabaseModel())
                                insertAllTodayShows(*shows.toTypedArray())
                            }
                        }
                    }
                }
                _isLoadingTodayShows.postValue(false)
            } catch (e: Exception) {
                _isLoadingTodayShows.postValue(false)
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    suspend fun refreshTomorrowShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingTomorrowShows.postValue(true)
                val tomorrowShowsList =
                    TvMazeNetwork.tvMazeApi.getTomorrowScheduleAsync(countryCode, date).await()

                saveTomorrowShows(tomorrowShowsList)

                _isLoadingTomorrowShows.postValue(false)
            } catch (e: Exception) {
                _isLoadingTomorrowShows.postValue(false)
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    private suspend fun saveTomorrowShows(tomorrowShowsList: List<TomorrowNetworkSchedule>) {
        withContext(Dispatchers.IO) {
            try {
                val shows: MutableList<DatabaseTomorrowSchedule> = arrayListOf()
                if (!tomorrowShowsList.isNullOrEmpty()) {
                    database.upnextDao.apply {
                        deleteAllTomorrowShows()
                        tomorrowShowsList.forEach {
                            // only adding shows that have an image
                            if (!it.show.image?.original.isNullOrEmpty()) {
                                shows.add(it.asDatabaseModel())
                                insertAllTomorrowShows(*shows.toTypedArray())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    private suspend fun addShowData(showId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val showInfo =
                    TvMazeNetwork.tvMazeApi.getShowSummaryAsync(showId.toString()).await()

                val previousEpisodeLink = showInfo._links?.previousepisode?.href?.substring(31)
                val nextEpisodeLink = showInfo._links?.nextepisode?.href?.substring(31)

                var showPreviousEpisode: NetworkShowPreviousEpisode? = null
                var showNextEpisode: NetworkShowNextEpisode? = null
                if (!previousEpisodeLink.isNullOrEmpty()) {
                    showPreviousEpisode =
                        TvMazeNetwork.tvMazeApi.getPreviousEpisodeAsync(
                            showInfo._links.previousepisode.href.substring(
                                31
                            )
                        ).await()
                }
                if (!nextEpisodeLink.isNullOrEmpty()) {
                    showNextEpisode =
                        TvMazeNetwork.tvMazeApi.getNextEpisodeAsync(
                            showInfo._links.nextepisode.href.substring(
                                31
                            )
                        ).await()
                }
                val showInfoCombined = NetworkShowInfoCombined(
                    showInfoResponse = showInfo,
                    previousEpisode = showPreviousEpisode,
                    nextEpisode = showNextEpisode
                )
                database.upnextDao.apply {
                    deleteAllShowInfo(showId)
                    insertAllShowInfo(showInfoCombined.asDatabaseModel())
                }
            } catch (e: Exception) {
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    suspend fun getShowDataWithInsert(showId: Int) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                addShowData(showId)
                val showInfo = database.upnextDao.getShowWithId(showId)
                _showInfo.postValue(showInfo)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }

    suspend fun getShowData(showId: Int) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showInfo =
                    TvMazeNetwork.tvMazeApi.getShowSummaryAsync(showId.toString()).await()

                val previousEpisodeLink = showInfo._links?.previousepisode?.href?.substring(31)
                val nextEpisodeLink = showInfo._links?.nextepisode?.href?.substring(31)

                var showPreviousEpisode: NetworkShowPreviousEpisode? = null
                var showNextEpisode: NetworkShowNextEpisode? = null
                if (!previousEpisodeLink.isNullOrEmpty()) {
                    showPreviousEpisode =
                        TvMazeNetwork.tvMazeApi.getPreviousEpisodeAsync(
                            showInfo._links.previousepisode.href.substring(
                                31
                            )
                        ).await()
                }
                if (!nextEpisodeLink.isNullOrEmpty()) {
                    showNextEpisode =
                        TvMazeNetwork.tvMazeApi.getNextEpisodeAsync(
                            showInfo._links.nextepisode.href.substring(
                                31
                            )
                        ).await()
                }
                val showInfoCombined = NetworkShowInfoCombined(
                    showInfoResponse = showInfo,
                    previousEpisode = showPreviousEpisode,
                    nextEpisode = showNextEpisode
                )
                _showInfo.postValue(showInfoCombined.asDomainModel())
                _isLoading.postValue(true)
            } catch (e: Exception) {
                _isLoading.postValue(true)
                Timber.d(e)
                Crashlytics.logException(e)
            }
        }
    }
}