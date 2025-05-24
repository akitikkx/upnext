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
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.database.DatabaseTodaySchedule
import com.theupnextapp.database.DatabaseTomorrowSchedule
import com.theupnextapp.database.DatabaseYesterdaySchedule
import com.theupnextapp.database.TvMazeDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.asDatabaseModel
import com.theupnextapp.network.models.tvmaze.NetworkTodayScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTomorrowScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponse
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class DashboardRepository(
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    private val tvMazeDao: TvMazeDao,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseRepository(upnextDao = upnextDao, tvMazeService = tvMazeService) {

    private val _isLoadingYesterdayShows = MutableLiveData<Boolean>()
    val isLoadingYesterdayShows: LiveData<Boolean> = _isLoadingYesterdayShows

    private val _isLoadingTodayShows = MutableLiveData<Boolean>()
    val isLoadingTodayShows: LiveData<Boolean> = _isLoadingTodayShows

    private val _isLoadingTomorrowShows = MutableLiveData<Boolean>()
    val isLoadingTomorrowShows: LiveData<Boolean> = _isLoadingTomorrowShows

    val yesterdayShows: Flow<List<ScheduleShow>>
        get() = tvMazeDao.getYesterdayShows().map {
            it.asDomainModel()
        }

    val todayShows: Flow<List<ScheduleShow>>
        get() = tvMazeDao.getTodayShows().map {
            it.asDomainModel()
        }

    val tomorrowShows: Flow<List<ScheduleShow>>
        get() = tvMazeDao.getTomorrowShows().map {
            it.asDomainModel()
        }

    fun tableUpdate(tableName: String): Flow<TableUpdate?> =
        upnextDao.getTableLastUpdate(tableName).map {
            it?.asDomainModel()
        }

    suspend fun refreshYesterdayShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
                if (canProceedWithUpdate(
                        tableName = DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName,
                        intervalMinutes = TableUpdateInterval.DASHBOARD_ITEMS.intervalMins
                    )
                ) {
                    _isLoadingYesterdayShows.postValue(true)
                    val shows: MutableList<DatabaseYesterdaySchedule> = arrayListOf()
                    val yesterdayShowsList =
                        tvMazeService.getYesterdayScheduleAsync(
                            countryCode,
                            date
                        )
                            .await()
                    if (!yesterdayShowsList.isNullOrEmpty()) {
                        yesterdayShowsList.forEach {
                            val yesterdayShow: NetworkYesterdayScheduleResponse = it

                            // only adding shows that have an image
                            if (!yesterdayShow.show.image?.original.isNullOrEmpty() && !yesterdayShow.show.externals?.imdb.isNullOrEmpty()) {

                                val (poster, heroImage) = getImages(
                                    id = yesterdayShow.show.id.toString(),
                                    fallbackPoster = yesterdayShow.show.image?.original,
                                    fallbackMedium = yesterdayShow.show.image?.medium
                                )

                                yesterdayShow.show.image?.original = poster
                                yesterdayShow.show.image?.medium = heroImage

                                shows.add(yesterdayShow.asDatabaseModel())
                            }
                        }

                        tvMazeDao.deleteAllYesterdayShows()
                        tvMazeDao.insertAllYesterdayShows(*shows.toTypedArray())
                        upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName)
                        upnextDao.insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }
                    _isLoadingYesterdayShows.postValue(false)
                }
            } catch (e: Exception) {
                _isLoadingYesterdayShows.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshTodayShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
                if (canProceedWithUpdate(
                        tableName = DatabaseTables.TABLE_TODAY_SHOWS.tableName,
                        intervalMinutes = TableUpdateInterval.DASHBOARD_ITEMS.intervalMins
                    )
                ) {
                    _isLoadingTodayShows.postValue(true)
                    val shows: MutableList<DatabaseTodaySchedule> = arrayListOf()
                    val todayShowsList =
                        tvMazeService.getTodayScheduleAsync(countryCode, date).await()
                    if (!todayShowsList.isNullOrEmpty()) {
                        todayShowsList.forEach {
                            val todayShow: NetworkTodayScheduleResponse = it

                            // only adding shows that have an image
                            if (!it.show.image?.original.isNullOrEmpty() && !it.show.externals?.imdb.isNullOrEmpty()) {

                                val (poster, heroImage) = getImages(
                                    id = todayShow.show.id.toString(),
                                    fallbackPoster = todayShow.show.image?.original,
                                    fallbackMedium = todayShow.show.image?.medium
                                )

                                todayShow.show.image?.original = poster
                                todayShow.show.image?.medium = heroImage

                                shows.add(todayShow.asDatabaseModel())
                            }
                        }

                        tvMazeDao.deleteAllTodayShows()
                        tvMazeDao.insertAllTodayShows(*shows.toTypedArray())
                        upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TODAY_SHOWS.tableName)
                        upnextDao.insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_TODAY_SHOWS.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }
                    _isLoadingTodayShows.postValue(false)
                }
            } catch (e: Exception) {
                _isLoadingTodayShows.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshTomorrowShows(countryCode: String, date: String?) {
        withContext(Dispatchers.IO) {
            try {
                if (canProceedWithUpdate(
                        tableName = DatabaseTables.TABLE_TOMORROW_SHOWS.tableName,
                        intervalMinutes = TableUpdateInterval.DASHBOARD_ITEMS.intervalMins
                    )
                ) {
                    _isLoadingTomorrowShows.postValue(true)
                    val tomorrowShowsList =
                        tvMazeService.getTomorrowScheduleAsync(countryCode, date).await()

                    val shows: MutableList<DatabaseTomorrowSchedule> = arrayListOf()
                    if (!tomorrowShowsList.isNullOrEmpty()) {
                        tomorrowShowsList.forEach {
                            val tomorrowShow: NetworkTomorrowScheduleResponse = it

                            // only adding shows that have an image
                            if (!tomorrowShow.show.image?.original.isNullOrEmpty() && !tomorrowShow.show.externals?.imdb.isNullOrEmpty()) {

                                val (poster, heroImage) = getImages(
                                    id = tomorrowShow.show.id.toString(),
                                    fallbackPoster = tomorrowShow.show.image?.original,
                                    fallbackMedium = tomorrowShow.show.image?.medium
                                )

                                tomorrowShow.show.image?.original = poster
                                tomorrowShow.show.image?.medium = heroImage

                                shows.add(tomorrowShow.asDatabaseModel())
                            }
                        }

                        tvMazeDao.deleteAllTomorrowShows()
                        tvMazeDao.insertAllTomorrowShows(*shows.toTypedArray())
                        upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TOMORROW_SHOWS.tableName)
                        upnextDao.insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_TOMORROW_SHOWS.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }
                    _isLoadingTomorrowShows.postValue(false)
                }
            } catch (e: Exception) {
                _isLoadingTomorrowShows.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    /**
     * Get the images for the given ImdbID
     */
    private suspend fun getImages(
        id: String,
        fallbackPoster: String?,
        fallbackMedium: String?
    ): Pair<String?, String?> {
        var poster: String? = ""
        var heroImage: String? = ""

        var showImagesResponse: NetworkTvMazeShowImageResponse? = null
        try {
            showImagesResponse =
                tvMazeService.getShowImagesAsync(id)
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
        return Pair(poster ?: fallbackPoster, heroImage ?: fallbackMedium)
    }
}
