package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.theupnextapp.database.UpnextDatabase
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.domain.NewShows
import com.theupnextapp.domain.RecommendedShows
import com.theupnextapp.network.Network
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

    suspend fun refreshRecommendedShows() {
        withContext(Dispatchers.IO) {
            try {
                val recommendedShowsList = Network.upnextApi.getRecommendedShowsAsync().await()
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
                val newShowsList = Network.upnextApi.getNewShowsAsync().await()
                database.upnextDao.apply {
                    deleteAllNewShows()
                    insertAllNewShows(*newShowsList.asDatabaseModel())
                }

            } catch (e: HttpException) {
                Timber.e(e)
            }
        }
    }
}