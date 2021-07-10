package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.BuildConfig
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.database.*
import com.theupnextapp.domain.*
import com.theupnextapp.network.TraktNetwork
import com.theupnextapp.network.TvMazeNetwork
import com.theupnextapp.network.models.trakt.*
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.net.ssl.SSLHandshakeException

class TraktRepository constructor(
    private val upnextDao: UpnextDao,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseRepository(upnextDao) {

    fun tableUpdate(tableName: String): LiveData<TableUpdate?> =
        Transformations.map(upnextDao.getTableLastUpdate(tableName)) {
            it?.asDomainModel()
        }

    val traktPopularShows: LiveData<List<TraktPopularShows>> =
        Transformations.map(upnextDao.getTraktPopular()) {
            it.asDomainModel()
        }

    val traktTrendingShows: LiveData<List<TraktTrendingShows>> =
        Transformations.map(upnextDao.getTraktTrending()) {
            it.asDomainModel()
        }

    val traktMostAnticipatedShows: LiveData<List<TraktMostAnticipated>> =
        Transformations.map(upnextDao.getTraktMostAnticipated()) {
            it.asDomainModel()
        }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingTraktTrending = MutableLiveData<Boolean>()
    val isLoadingTraktTrending: LiveData<Boolean> = _isLoadingTraktTrending

    private val _isLoadingTraktPopular = MutableLiveData<Boolean>()
    val isLoadingTraktPopular: LiveData<Boolean> = _isLoadingTraktPopular

    private val _isLoadingTraktMostAnticipated = MutableLiveData<Boolean>()
    val isLoadingTraktMostAnticipated: LiveData<Boolean> = _isLoadingTraktMostAnticipated

    private val _traktShowRating = MutableLiveData<TraktShowRating>()
    val traktShowRating: LiveData<TraktShowRating> = _traktShowRating

    private val _traktShowStats = MutableLiveData<TraktShowStats>()
    val traktShowStats: LiveData<TraktShowStats> = _traktShowStats

    private val _traktAccessToken = MutableLiveData<TraktAccessToken?>()
    val traktAccessToken: LiveData<TraktAccessToken?> = _traktAccessToken

    suspend fun getTraktAccessToken(code: String?) {
        if (code.isNullOrEmpty()) {
            logTraktException("Could not get the access token due to a null code")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
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
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun getTraktShowRating(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            logTraktException("Could not get the show rating due to a null imdb ID")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showRatingResponse = TraktNetwork.traktApi.getShowRatingsAsync(
                    id = imdbID
                ).await()
                _traktShowRating.postValue(showRatingResponse.asDomainModel())
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun getTraktShowStats(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            logTraktException("Could not get the show stats due to a null imdb ID")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val showStatsResponse = TraktNetwork.traktApi.getShowStatsAsync(
                    id = imdbID
                ).await()
                _traktShowStats.postValue(showStatsResponse.asDomainModel())
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoading.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshTraktTrendingShows() {
        withContext(Dispatchers.IO) {
            try {
                if (canProceedWithUpdate(
                        tableName = DatabaseTables.TABLE_TRAKT_TRENDING.tableName,
                        intervalMins = TableUpdateInterval.TRAKT_TRENDING_ITEMS.intervalMins
                    )
                ) {
                    _isLoadingTraktTrending.postValue(true)
                    val shows: MutableList<DatabaseTraktTrendingShows> = mutableListOf()

                    val trendingShowsResponse =
                        TraktNetwork.traktApi.getTrendingShowsAsync().await()

                    if (!trendingShowsResponse.isEmpty()) {
                        for (item in trendingShowsResponse) {
                            val trendingItem: NetworkTraktTrendingShowsResponseItem = item

                            val (id, poster, heroImage) = getImages(trendingItem.show.ids.imdb)

                            trendingItem.show.originalImageUrl = poster
                            trendingItem.show.mediumImageUrl = heroImage
                            trendingItem.show.ids.tvMazeID = id

                            shows.add(trendingItem.asDatabaseModel())
                        }

                        upnextDao.apply {
                            deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_TRENDING.tableName)
                            deleteAllTraktTrending()
                            insertAllTraktTrending(*shows.toTypedArray())
                            insertTableUpdateLog(
                                DatabaseTableUpdate(
                                    table_name = DatabaseTables.TABLE_TRAKT_TRENDING.tableName,
                                    last_updated = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    _isLoadingTraktTrending.postValue(false)
                }
            } catch (e: Exception) {
                _isLoadingTraktTrending.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoadingTraktTrending.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoadingTraktTrending.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoadingTraktTrending.postValue(false)
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
    ): Triple<Int?, String?, String?> {
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
        return Triple(tvMazeSearch?.id, poster ?: fallbackPoster, heroImage ?: fallbackMedium)
    }

    suspend fun refreshTraktPopularShows() {
        withContext(Dispatchers.IO) {
            try {
                if (canProceedWithUpdate(
                        tableName = DatabaseTables.TABLE_TRAKT_POPULAR.tableName,
                        intervalMins = TableUpdateInterval.TRAKT_POPULAR_ITEMS.intervalMins
                    )
                ) {
                    _isLoadingTraktPopular.postValue(true)
                    val shows: MutableList<DatabaseTraktPopularShows> = mutableListOf()

                    val popularShowsResponse = TraktNetwork.traktApi.getPopularShowsAsync().await()

                    if (!popularShowsResponse.isEmpty()) {
                        for (item in popularShowsResponse) {
                            val popularItem: NetworkTraktPopularShowsResponseItem = item

                            val (id, poster, heroImage) = getImages(popularItem.ids.imdb)

                            popularItem.originalImageUrl =
                                poster
                            popularItem.mediumImageUrl =
                                heroImage
                            popularItem.ids.tvMazeID = id

                            shows.add(popularItem.asDatabaseModel())
                        }
                        upnextDao.apply {
                            deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_POPULAR.tableName)
                            deleteAllTraktPopular()
                            insertAllTraktPopular(*shows.toTypedArray())
                            insertTableUpdateLog(
                                DatabaseTableUpdate(
                                    table_name = DatabaseTables.TABLE_TRAKT_POPULAR.tableName,
                                    last_updated = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    _isLoadingTraktPopular.postValue(false)
                }
            } catch (e: Exception) {
                _isLoadingTraktPopular.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoadingTraktPopular.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoadingTraktPopular.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoadingTraktPopular.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    suspend fun refreshTraktMostAnticipatedShows() {
        withContext(Dispatchers.IO) {
            try {
                if (canProceedWithUpdate(
                        tableName = DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName,
                        intervalMins = TableUpdateInterval.TRAKT_MOST_ANTICIPATED_ITEMS.intervalMins
                    )
                ) {
                    _isLoadingTraktMostAnticipated.postValue(true)
                    val shows: MutableList<DatabaseTraktMostAnticipated> = mutableListOf()

                    val mostAnticipatedShowsResponse =
                        TraktNetwork.traktApi.getMostAnticipatedShowsAsync().await()

                    if (!mostAnticipatedShowsResponse.isEmpty()) {
                        for (item in mostAnticipatedShowsResponse) {
                            val mostAnticipatedItem: NetworkTraktMostAnticipatedResponseItem = item

                            val (id, poster, heroImage) = getImages(mostAnticipatedItem.show.ids.imdb)

                            mostAnticipatedItem.show.originalImageUrl =
                                poster
                            mostAnticipatedItem.show.mediumImageUrl =
                                heroImage
                            mostAnticipatedItem.show.ids.tvMazeID = id
                            shows.add(mostAnticipatedItem.asDatabaseModel())
                        }
                        upnextDao.apply {
                            deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName)
                            deleteAllTraktMostAnticipated()
                            insertAllTraktMostAnticipated(*shows.toTypedArray())
                            insertTableUpdateLog(
                                DatabaseTableUpdate(
                                    table_name = DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName,
                                    last_updated = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    _isLoadingTraktMostAnticipated.postValue(false)
                }
            } catch (e: Exception) {
                _isLoadingTraktMostAnticipated.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: HttpException) {
                _isLoadingTraktMostAnticipated.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: SSLHandshakeException) {
                _isLoadingTraktMostAnticipated.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            } catch (e: IOException) {
                _isLoadingTraktMostAnticipated.postValue(false)
                Timber.d(e)
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    private fun logTraktException(message: String) {
        Timber.d(Throwable(message = message))
        firebaseCrashlytics
            .recordException(Throwable(message = "TraktRepository: $message"))
    }
}