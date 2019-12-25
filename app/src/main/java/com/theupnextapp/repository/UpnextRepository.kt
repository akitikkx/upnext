package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.theupnextapp.database.*
import com.theupnextapp.domain.NewShows
import com.theupnextapp.domain.RecommendedShows
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ShowInfo
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

    val showInfo: LiveData<ShowInfo>
        get() = _showInfo

    private val _isLoading = MutableLiveData<Boolean>()

    val isLoading: LiveData<Boolean>
        get() = _isLoading

    suspend fun refreshRecommendedShows() {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val recommendedShowsList =
                    UpnextNetwork.upnextApi.getRecommendedShowsAsync().await()
                database.upnextDao.apply {
                    deleteAllRecommendedShows()
                    insertAllRecommendedShows(*recommendedShowsList.asDatabaseModel())
                }
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.e(e)
            }
        }
    }

    suspend fun refreshNewShows() {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val newShowsList = UpnextNetwork.upnextApi.getNewShowsAsync().await()
                database.upnextDao.apply {
                    deleteAllNewShows()
                    insertAllNewShows(*newShowsList.asDatabaseModel())
                }
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.e(e)
            }
        }
    }

    suspend fun refreshYesterdayShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
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
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.e(e)
            }
        }
    }

    suspend fun refreshTodayShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
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
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.e(e)
            }
        }
    }

    suspend fun refreshTomorrowShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val shows: MutableList<DatabaseTomorrowSchedule> = arrayListOf()
                val tomorrowShowsList =
                    TvMazeNetwork.tvMazeApi.getTomorrowScheduleAsync(countryCode, date).await()
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
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
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

    suspend fun getShowData(showId: Int) {
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
}