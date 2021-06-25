package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.common.utils.models.DatabaseTables
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
    private val firebaseCrashlytics: FirebaseCrashlytics
) {

    val yesterdayShows: LiveData<List<ScheduleShow>> =
        Transformations.map(upnextDao.getYesterdayShows()) {
            it.asDomainModel()
        }

    val todayShows: LiveData<List<ScheduleShow>> =
        Transformations.map(upnextDao.getTodayShows()) {
            it.asDomainModel()
        }

    val tomorrowShows: LiveData<List<ScheduleShow>> =
        Transformations.map(upnextDao.getTomorrowShows()) {
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
                _isLoadingYesterdayShows.postValue(true)
                val shows: MutableList<DatabaseYesterdaySchedule> = arrayListOf()
                val yesterdayShowsList =
                    TvMazeNetwork.tvMazeApi.getYesterdayScheduleAsync(countryCode, date).await()
                if (!yesterdayShowsList.isNullOrEmpty()) {
                    upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName)

                    yesterdayShowsList.forEach {
                        val yesterdayShow: NetworkYesterdayScheduleResponse = it

                        // only adding shows that have an image
                        if (!yesterdayShow.show.image?.original.isNullOrEmpty() && !yesterdayShow.show.externals?.imdb.isNullOrEmpty()) {

                            val (poster, heroImage) = getImages(yesterdayShow.show.externals?.imdb)

                            yesterdayShow.show.image?.original =
                                poster ?: yesterdayShow.show.image?.original
                            yesterdayShow.show.image?.medium =
                                heroImage ?: yesterdayShow.show.image?.medium

                            shows.add(yesterdayShow.asDatabaseModel())
                        }
                    }
                    upnextDao.apply {
                        deleteAllYesterdayShows()
                        insertAllYesterdayShows(*shows.toTypedArray())
                        insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }
                }
                _isLoadingYesterdayShows.postValue(false)
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
                _isLoadingTodayShows.postValue(true)
                val shows: MutableList<DatabaseTodaySchedule> = arrayListOf()
                val todayShowsList =
                    TvMazeNetwork.tvMazeApi.getTodayScheduleAsync(countryCode, date).await()
                if (!todayShowsList.isNullOrEmpty()) {
                    upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TODAY_SHOWS.tableName)

                    todayShowsList.forEach {
                        val todayShow: NetworkTodayScheduleResponse = it

                        // only adding shows that have an image
                        if (!it.show.image?.original.isNullOrEmpty() && !it.show.externals?.imdb.isNullOrEmpty()) {

                            val (poster, heroImage) = getImages(todayShow.show.externals?.imdb)

                            todayShow.show.image?.original =
                                poster ?: todayShow.show.image?.original
                            todayShow.show.image?.medium = heroImage ?: todayShow.show.image?.medium

                            shows.add(todayShow.asDatabaseModel())
                        }
                    }
                    upnextDao.apply {
                        deleteAllTodayShows()
                        insertAllTodayShows(*shows.toTypedArray())
                        insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_TODAY_SHOWS.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }
                }
                _isLoadingTodayShows.postValue(false)
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
                _isLoadingTomorrowShows.postValue(true)
                val tomorrowShowsList =
                    TvMazeNetwork.tvMazeApi.getTomorrowScheduleAsync(countryCode, date).await()

                saveTomorrowShows(tomorrowShowsList)

                _isLoadingTomorrowShows.postValue(false)
            } catch (e: Exception) {
                _isLoadingTomorrowShows.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    private suspend fun saveTomorrowShows(tomorrowShowsList: List<NetworkTomorrowScheduleResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val shows: MutableList<DatabaseTomorrowSchedule> = arrayListOf()
                if (!tomorrowShowsList.isNullOrEmpty()) {
                    upnextDao.deleteRecentTableUpdate(DatabaseTables.TABLE_TOMORROW_SHOWS.tableName)

                    tomorrowShowsList.forEach {
                        val tomorrowShow: NetworkTomorrowScheduleResponse = it

                        // only adding shows that have an image
                        if (!tomorrowShow.show.image?.original.isNullOrEmpty() && !tomorrowShow.show.externals?.imdb.isNullOrEmpty()) {

                            val (poster, heroImage) = getImages(tomorrowShow.show.externals?.imdb)

                            tomorrowShow.show.image?.original =
                                poster ?: tomorrowShow.show.image?.original
                            tomorrowShow.show.image?.medium =
                                heroImage ?: tomorrowShow.show.image?.medium

                            shows.add(tomorrowShow.asDatabaseModel())
                        }
                    }
                    upnextDao.apply {
                        deleteAllTomorrowShows()
                        insertAllTomorrowShows(*shows.toTypedArray())
                        insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_TOMORROW_SHOWS.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    /**
     * Get the images for the given ImdbID
     */
    private suspend fun getImages(
        imdbID: String?
    ): Pair<String?, String?> {
        var tvMazeSearch: NetworkTvMazeShowLookupResponse? = null

        // perform a TvMaze search for the Trakt item using the Trakt title
        try {
            tvMazeSearch = imdbID?.let {
                TvMazeNetwork.tvMazeApi.getShowLookupAsync(it).await()
            }
        } catch (e: Exception) {
            Timber.d(e)
            firebaseCrashlytics.recordException(e)
        }

        val fallbackPoster = tvMazeSearch?.image?.original
        val fallbackMedium = tvMazeSearch?.image?.medium
        var poster: String? = ""
        var heroImage: String? = ""

        var showImagesResponse: NetworkTvMazeShowImageResponse? = null
        try {
            showImagesResponse =
                TvMazeNetwork.tvMazeApi.getShowImagesAsync(tvMazeSearch?.id.toString())
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

                val previousEpisodeLink = showInfo._links?.previousepisode?.href?.substring(
                    showInfo._links.previousepisode.href.lastIndexOf("/") + 1,
                    showInfo._links.previousepisode.href.length
                )?.replace("/", "")

                val nextEpisodeLink = showInfo._links?.nextepisode?.href?.substring(
                    showInfo._links.nextepisode.href.lastIndexOf("/") + 1,
                    showInfo._links.nextepisode.href.length
                )?.replace("/", "")

                var showPreviousEpisode: NetworkShowPreviousEpisodeResponse? = null
                var showNextEpisode: NetworkShowNextEpisodeResponse? = null
                if (!previousEpisodeLink.isNullOrEmpty()) {
                    showPreviousEpisode =
                        TvMazeNetwork.tvMazeApi.getPreviousEpisodeAsync(
                            previousEpisodeLink.replace("/", "")
                        ).await()
                }
                if (!nextEpisodeLink.isNullOrEmpty()) {
                    showNextEpisode =
                        TvMazeNetwork.tvMazeApi.getNextEpisodeAsync(
                            nextEpisodeLink.replace("/", "")
                        ).await()
                }
                val showInfoCombined =
                    NetworkShowInfoCombined(
                        showInfoResponse = showInfo,
                        previousEpisode = showPreviousEpisode,
                        nextEpisode = showNextEpisode
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
                val showCast = TvMazeNetwork.tvMazeApi.getShowCastAsync(showId.toString()).await()
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
}