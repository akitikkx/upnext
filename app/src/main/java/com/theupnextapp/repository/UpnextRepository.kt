package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.database.*
import com.theupnextapp.domain.*
import com.theupnextapp.network.*
import com.theupnextapp.network.models.tvmaze.*
import com.theupnextapp.network.models.upnext.NetworkShowInfoCombined
import com.theupnextapp.network.models.upnext.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

class UpnextRepository(private val database: UpnextDatabase) {

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

    fun tableUpdate(tableName: String): LiveData<TableUpdate?> =
        Transformations.map(database.upnextDao.getTableLastUpdate(tableName)) {
            it?.asDomainModel()
        }

    private val _showInfo = MutableLiveData<ShowInfo>()

    private val _showSearch = MutableLiveData<List<ShowSearch>>()

    private val _isLoading = MutableLiveData<Boolean>()

    private val _isLoadingNewShows = MutableLiveData<Boolean>()

    private val _isLoadingYesterdayShows = MutableLiveData<Boolean>()

    private val _isLoadingTodayShows = MutableLiveData<Boolean>()

    private val _isLoadingTomorrowShows = MutableLiveData<Boolean>()

    private val _showCast = MutableLiveData<List<ShowCast>>()

    private val _showSeasons = MutableLiveData<List<ShowSeason>>()

    val isLoading: LiveData<Boolean> = _isLoading

    val isLoadingNewShows: LiveData<Boolean> = _isLoadingNewShows

    val isLoadingYesterdayShows: LiveData<Boolean> = _isLoadingYesterdayShows

    val isLoadingTodayShows: LiveData<Boolean> = _isLoadingTodayShows

    val isLoadingTomorrowShows: LiveData<Boolean> = _isLoadingTomorrowShows

    val showInfo: LiveData<ShowInfo> = _showInfo

    val showSearch: LiveData<List<ShowSearch>> = _showSearch

    val showCast: LiveData<List<ShowCast>> = _showCast

    val showSeasons: LiveData<List<ShowSeason>> = _showSeasons

    suspend fun getSearchSuggestions(name: String?) {
        if (!name.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val searchList = TvMazeNetwork.tvMazeApi.getSuggestionListAsync(name).await()
                    val rebuiltList: MutableList<NetworkShowSearchResponse> = arrayListOf()

                    // we need to ensure that the items returned have an IMDB link
                    if (!searchList.isNullOrEmpty()) {
                        searchList.forEach {
                            val item: NetworkShowSearchResponse = it
                            if (!item.show.externals.imdb.isNullOrEmpty()) {
                                rebuiltList.add(item)
                            }
                        }
                    }

                    _showSearch.postValue(rebuiltList.asDomainModel())
                } catch (e: Exception) {
                    Timber.d(e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
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
                FirebaseCrashlytics.getInstance().recordException(e)
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
                    database.upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName)
                    database.upnextDao.insertTableUpdateLog(
                        DatabaseTableUpdate(
                            table_name = DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName,
                            last_updated = System.currentTimeMillis()
                        )
                    )

                    yesterdayShowsList.forEach {
                        // only adding shows that have an image
                        if (!it.show.image?.original.isNullOrEmpty() && !it.show.externals?.imdb.isNullOrEmpty()) {
                            shows.add(it.asDatabaseModel())
                        }
                    }
                    database.upnextDao.apply {
                        deleteAllYesterdayShows()
                        insertAllYesterdayShows(*shows.toTypedArray())
                    }
                }
                _isLoadingYesterdayShows.postValue(false)
            } catch (e: Exception) {
                _isLoadingYesterdayShows.postValue(false)
                Timber.d(e)
                FirebaseCrashlytics.getInstance().recordException(e)
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
                    database.upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TODAY_SHOWS.tableName)
                    database.upnextDao.insertTableUpdateLog(
                        DatabaseTableUpdate(
                            table_name = DatabaseTables.TABLE_TODAY_SHOWS.tableName,
                            last_updated = System.currentTimeMillis()
                        )
                    )

                    todayShowsList.forEach {
                        // only adding shows that have an image
                        if (!it.show.image?.original.isNullOrEmpty() && !it.show.externals?.imdb.isNullOrEmpty()) {
                            shows.add(it.asDatabaseModel())
                        }
                    }
                    database.upnextDao.apply {
                        deleteAllTodayShows()
                        insertAllTodayShows(*shows.toTypedArray())
                    }
                }
                _isLoadingTodayShows.postValue(false)
            } catch (e: Exception) {
                _isLoadingTodayShows.postValue(false)
                Timber.d(e)
                FirebaseCrashlytics.getInstance().recordException(e)
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
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    private suspend fun saveTomorrowShows(tomorrowShowsList: List<NetworkTomorrowScheduleResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val shows: MutableList<DatabaseTomorrowSchedule> = arrayListOf()
                if (!tomorrowShowsList.isNullOrEmpty()) {
                    tomorrowShowsList.forEach {
                        database.upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TOMORROW_SHOWS.tableName)
                        database.upnextDao.insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_TOMORROW_SHOWS.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )

                        // only adding shows that have an image
                        if (!it.show.image?.original.isNullOrEmpty() && !it.show.externals?.imdb.isNullOrEmpty()) {
                            shows.add(it.asDatabaseModel())
                        }
                    }
                    database.upnextDao.apply {
                        deleteAllTomorrowShows()
                        insertAllTomorrowShows(*shows.toTypedArray())
                    }
                }
            } catch (e: Exception) {
                Timber.d(e)
                FirebaseCrashlytics.getInstance().recordException(e)
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

                var showPreviousEpisode: NetworkShowPreviousEpisodeResponse? = null
                var showNextEpisode: NetworkShowNextEpisodeResponse? = null
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
                val showInfoCombined =
                    NetworkShowInfoCombined(
                        showInfoResponse = showInfo,
                        previousEpisode = showPreviousEpisode,
                        nextEpisode = showNextEpisode
                    )
                _showInfo.postValue(showInfoCombined.asDomainModel())
                _isLoading.postValue(true)
            } catch (e: Exception) {
                _isLoading.postValue(true)
                Timber.d(e)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    suspend fun getShowCast(showId: Int) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showCast = TvMazeNetwork.tvMazeApi.getShowCastAsync(showId.toString()).await()
                _showCast.postValue(showCast.asDomainModel())
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.d(e)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    suspend fun getShowSeasons(showId: Int) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showSeasons =
                    TvMazeNetwork.tvMazeApi.getShowSeasonsAsync(showId.toString()).await()
                _showSeasons.postValue(showSeasons.asDomainModel())
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.d(e)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }
}