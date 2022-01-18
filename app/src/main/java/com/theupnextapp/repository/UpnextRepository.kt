package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.database.*
import com.theupnextapp.domain.*
import com.theupnextapp.network.TvMazeNetwork
import com.theupnextapp.network.asDatabaseModel
import com.theupnextapp.network.models.tvmaze.*
import com.theupnextapp.network.models.upnext.NetworkShowInfoCombined
import com.theupnextapp.network.models.upnext.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException

class UpnextRepository constructor(
    private val upnextDao: UpnextDao,
    private val tvMazeDao: TvMazeDao,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseRepository(upnextDao) {

    val yesterdayShows: LiveData<List<ScheduleShow>> =
        Transformations.map(tvMazeDao.getYesterdayShows()) {
            it.asDomainModel()
        }

    val todayShows: LiveData<List<ScheduleShow>> =
        Transformations.map(tvMazeDao.getTodayShows()) {
            it.asDomainModel()
        }

    val tomorrowShows: LiveData<List<ScheduleShow>> =
        Transformations.map(tvMazeDao.getTomorrowShows()) {
            it.asDomainModel()
        }

    fun tableUpdate(tableName: String): LiveData<TableUpdate?> =
        Transformations.map(upnextDao.getTableLastUpdate(tableName)) {
            it?.asDomainModel()
        }

    private val _showInfo = MutableLiveData<ShowInfo>()
    val showInfo: LiveData<ShowInfo> = _showInfo

    private val _showSearch = MutableLiveData<List<ShowSearch>>()
    val showSearch: LiveData<List<ShowSearch>> = _showSearch

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingYesterdayShows = MutableLiveData<Boolean>()
    val isLoadingYesterdayShows: LiveData<Boolean> = _isLoadingYesterdayShows

    private val _isLoadingTodayShows = MutableLiveData<Boolean>()
    val isLoadingTodayShows: LiveData<Boolean> = _isLoadingTodayShows

    private val _isLoadingTomorrowShows = MutableLiveData<Boolean>()
    val isLoadingTomorrowShows: LiveData<Boolean> = _isLoadingTomorrowShows

    private val _showCast = MutableLiveData<List<ShowCast>>()
    val showCast: LiveData<List<ShowCast>> = _showCast

    private val _showSeasons = MutableLiveData<List<ShowSeason>>()
    val showSeasons: LiveData<List<ShowSeason>> = _showSeasons

    private val _episodes = MutableLiveData<List<ShowSeasonEpisode>>()
    val episodes: LiveData<List<ShowSeasonEpisode>> = _episodes

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
                    firebaseCrashlytics.recordException(e)
                }
            }
        }
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
                        TvMazeNetwork.tvMazeApi.getYesterdayScheduleAsync(
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
                        TvMazeNetwork.tvMazeApi.getTodayScheduleAsync(countryCode, date).await()
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
                        TvMazeNetwork.tvMazeApi.getTomorrowScheduleAsync(countryCode, date).await()

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
                TvMazeNetwork.tvMazeApi.getShowImagesAsync(id)
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

    suspend fun getShowData(showId: Int) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showInfo =
                    TvMazeNetwork.tvMazeApi.getShowSummaryAsync(showId.toString()).await()

                val (nextEpisode, previousEpisode) = getPreviousAndNextEpisodes(showId)

                val showInfoCombined =
                    NetworkShowInfoCombined(
                        showInfoResponse = showInfo,
                        previousEpisode = previousEpisode,
                        nextEpisode = nextEpisode
                    )
                _showInfo.postValue(showInfoCombined.asDomainModel())
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: UnknownHostException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun getShowCast(showId: Int) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showCast =
                    TvMazeNetwork.tvMazeApi.getShowCastAsync(showId.toString()).await()
                _showCast.postValue(showCast.asDomainModel())
                _isLoading.postValue(false)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
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
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun getShowSeasonEpisodes(showId: Int, seasonNumber: Int) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val seasonEpisodes =
                    TvMazeNetwork.tvMazeApi.getSeasonEpisodesAsync(showId.toString()).await()
                _episodes.postValue(seasonEpisodes.filter { it.season == seasonNumber }
                    .asDomainModel())
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }
}