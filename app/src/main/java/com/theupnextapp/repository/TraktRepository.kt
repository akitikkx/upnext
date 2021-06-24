package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.database.*
import com.theupnextapp.domain.*
import com.theupnextapp.network.TraktNetwork
import com.theupnextapp.network.TvMazeNetwork
import com.theupnextapp.network.models.trakt.*
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.net.ssl.SSLHandshakeException

class TraktRepository constructor(
    private val upnextDao: UpnextDao,
    private val firebaseCrashlytics: FirebaseCrashlytics
) {

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
                _isLoadingTraktTrending.postValue(true)
                val shows: MutableList<DatabaseTraktTrendingShows> = mutableListOf()

                val trendingShowsResponse =
                    TraktNetwork.traktApi.getTrendingShowsAsync().await()

                if (!trendingShowsResponse.isEmpty()) {
                    upnextDao.apply {
                        deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_TRENDING.tableName)
                        insertTableUpdateLog(
                            DatabaseTableUpdate(
                                table_name = DatabaseTables.TABLE_TRAKT_TRENDING.tableName,
                                last_updated = System.currentTimeMillis()
                            )
                        )
                    }

                    for (item in trendingShowsResponse) {
                        val trendingItem: NetworkTraktTrendingShowsResponseItem = item

                        val imdbID = trendingItem.show.ids.imdb
                        val traktTitle = trendingItem.show.title

                        // perform a TvMaze search for the Trakt item using the Trakt title
                        val tvMazeSearch =
                            traktTitle?.let {
                                TvMazeNetwork.tvMazeApi.getSuggestionListAsync(it).await()
                            }

                        // loop through the search results from TvMaze and find a match for the IMDb ID
                        // and update the watchlist item by adding the TvMaze ID
                        if (!tvMazeSearch.isNullOrEmpty()) {
                            for (searchItem in tvMazeSearch) {
                                val showSearchItem: NetworkShowSearchResponse = searchItem
                                if (showSearchItem.show.externals.imdb == imdbID) {
                                    trendingItem.show.originalImageUrl =
                                        showSearchItem.show.image?.medium
                                    trendingItem.show.mediumImageUrl =
                                        showSearchItem.show.image?.original
                                    trendingItem.show.ids.tvMazeID = showSearchItem.show.id
                                }
                            }
                        }
                        shows.add(trendingItem.asDatabaseModel())
                    }

                    upnextDao.apply {
                        deleteAllTraktTrending()
                        insertAllTraktTrending(*shows.toTypedArray())
                    }
                }
                _isLoadingTraktTrending.postValue(false)
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

    suspend fun refreshTraktPopularShows() {
        withContext(Dispatchers.IO) {
            try {
                _isLoadingTraktPopular.postValue(true)
                val shows: MutableList<DatabaseTraktPopularShows> = mutableListOf()

                val popularShowsResponse = TraktNetwork.traktApi.getPopularShowsAsync().await()

                if (!popularShowsResponse.isEmpty()) {
                    upnextDao.apply {
                        deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_POPULAR.tableName)
                        deleteAllTraktPopular()
                    }

                    for (item in popularShowsResponse) {
                        val popularItem: NetworkTraktPopularShowsResponseItem = item

                        val imdbID = popularItem.ids.imdb
                        val traktTitle = popularItem.title

                        // perform a TvMaze search for the Trakt item using the Trakt title
                        val tvMazeSearch =
                            traktTitle?.let {
                                TvMazeNetwork.tvMazeApi.getSuggestionListAsync(it).await()
                            }

                        // loop through the search results from TvMaze and find a match for the IMDb ID
                        // and update the watchlist item by adding the TvMaze ID
                        if (!tvMazeSearch.isNullOrEmpty()) {
                            for (searchItem in tvMazeSearch) {
                                val showSearchItem: NetworkShowSearchResponse = searchItem
                                if (showSearchItem.show.externals.imdb == imdbID) {
                                    popularItem.originalImageUrl =
                                        showSearchItem.show.image?.medium
                                    popularItem.mediumImageUrl =
                                        showSearchItem.show.image?.original
                                    popularItem.ids.tvMazeID = showSearchItem.show.id
                                }
                            }
                        }
                        shows.add(popularItem.asDatabaseModel())
                    }
                    upnextDao.apply {
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
                _isLoadingTraktMostAnticipated.postValue(true)
                val shows: MutableList<DatabaseTraktMostAnticipated> = mutableListOf()

                val mostAnticipatedShowsResponse =
                    TraktNetwork.traktApi.getMostAnticipatedShowsAsync().await()

                if (!mostAnticipatedShowsResponse.isEmpty()) {
                    upnextDao.apply {
                        deleteRecentTableUpdate(DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName)
                        deleteAllTraktMostAnticipated()
                    }

                    for (item in mostAnticipatedShowsResponse) {
                        val mostAnticipatedItem: NetworkTraktMostAnticipatedResponseItem = item

                        val imdbID = mostAnticipatedItem.show.ids.imdb
                        val traktTitle = mostAnticipatedItem.show.title

                        // perform a TvMaze search for the Trakt item using the Trakt title
                        val tvMazeSearch =
                            traktTitle?.let {
                                TvMazeNetwork.tvMazeApi.getSuggestionListAsync(it).await()
                            }

                        // loop through the search results from TvMaze and find a match for the IMDb ID
                        // and update the watchlist item by adding the TvMaze ID
                        if (!tvMazeSearch.isNullOrEmpty()) {
                            for (searchItem in tvMazeSearch) {
                                val showSearchItem: NetworkShowSearchResponse = searchItem
                                if (showSearchItem.show.externals.imdb == imdbID) {
                                    mostAnticipatedItem.show.originalImageUrl =
                                        showSearchItem.show.image?.medium
                                    mostAnticipatedItem.show.mediumImageUrl =
                                        showSearchItem.show.image?.original
                                    mostAnticipatedItem.show.ids.tvMazeID = showSearchItem.show.id
                                }
                            }
                        }
                        shows.add(mostAnticipatedItem.asDatabaseModel())
                    }
                    upnextDao.apply {
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