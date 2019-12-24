package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.theupnextapp.database.*
import com.theupnextapp.domain.NewShows
import com.theupnextapp.domain.RecommendedShows
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.network.NetworkShowInfoCombined
import com.theupnextapp.network.TvMazeNetwork
import com.theupnextapp.network.UpnextNetwork
import com.theupnextapp.network.asDatabaseModel
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

    suspend fun refreshRecommendedShows() {
        withContext(Dispatchers.IO) {
            try {
                val recommendedShowsList =
                    UpnextNetwork.upnextApi.getRecommendedShowsAsync().await()
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
                val newShowsList = UpnextNetwork.upnextApi.getNewShowsAsync().await()
                database.upnextDao.apply {
                    deleteAllNewShows()
                    insertAllNewShows(*newShowsList.asDatabaseModel())
                }

            } catch (e: HttpException) {
                Timber.e(e)
            }
        }
    }

    suspend fun refreshYesterdayShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
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

            } catch (e: HttpException) {
                Timber.e(e)
            }
        }
    }

    suspend fun refreshTodayShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
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

            } catch (e: HttpException) {
                Timber.e(e)
            }
        }
    }

    suspend fun refreshTomorrowShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
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
                val showPreviousEpisode =
                    TvMazeNetwork.tvMazeApi.getPreviousEpisodeAsync(showId.toString()).await()
                val showNextEpisode =
                    TvMazeNetwork.tvMazeApi.getNextEpisodeAsync(showId.toString()).await()
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
                addShowData(showId)
                val showInfo = database.upnextDao.getShowWithId(showId)
                _showInfo.postValue(showInfo)
            } catch (e: HttpException) {
                Timber.e(e)
            }
        }
    }
}