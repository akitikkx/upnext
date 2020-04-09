package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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

    private val _showInfo = MutableLiveData<ShowInfo>()

    private val _showSearch = MutableLiveData<List<ShowSearch>>()

    private val _isLoading = MutableLiveData<Boolean>()

    private val _isLoadingRecommendedShows = MutableLiveData<Boolean>()

    private val _isLoadingNewShows = MutableLiveData<Boolean>()

    private val _isLoadingYesterdayShows = MutableLiveData<Boolean>()

    private val _isLoadingTodayShows = MutableLiveData<Boolean>()

    private val _isLoadingTomorrowShows = MutableLiveData<Boolean>()

    private val _traktAccessToken = MutableLiveData<TraktAccessToken>()

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
                } catch (e: HttpException) {
                    Timber.e(e)
                }
            }
        }
    }

    suspend fun getTraktAccessToken(code: String?) {
        if (!code.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val traktAccessTokenRequest =
                        TraktAccessTokenRequest(
                            code = code,
                            client_id = BuildConfig.TRAKT_CLIENT_ID,
                            client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                            redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                            grant_type = "authorization_code"
                        )

                    val accessTokenResponse = TraktNetwork.traktApi.getAccessTokenAsync(traktAccessTokenRequest).await()
                    _traktAccessToken.postValue(accessTokenResponse.asDomainModel())
                } catch (e: HttpException) {
                    Timber.d(e)
                }
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
            } catch (e: HttpException) {
                _isLoadingRecommendedShows.postValue(false)
                Timber.e(e)
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
            } catch (e: HttpException) {
                Timber.e(e)
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
            } catch (e: HttpException) {
                _isLoadingNewShows.postValue(false)
                Timber.e(e)
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
            } catch (e: HttpException) {
                _isLoadingYesterdayShows.postValue(false)
                Timber.e(e)
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
            } catch (e: HttpException) {
                _isLoadingTodayShows.postValue(false)
                Timber.e(e)
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
            } catch (e: HttpException) {
                _isLoadingTomorrowShows.postValue(false)
                Timber.e(e)
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
            } catch (e: HttpException) {
                Timber.e(e)
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
            } catch (e: HttpException) {
                Timber.e(e)
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
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.e(e)
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
            } catch (e: HttpException) {
                _isLoading.postValue(true)
                Timber.e(e)
            }
        }
    }
}