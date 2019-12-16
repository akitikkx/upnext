package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.theupnextapp.database.DatabaseYesterdaySchedule
import com.theupnextapp.database.UpnextDatabase
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.domain.NewShows
import com.theupnextapp.domain.RecommendedShows
import com.theupnextapp.domain.ScheduleShow
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
}