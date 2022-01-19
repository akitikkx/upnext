package com.theupnextapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.safeApiCall
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ShowDetailRepository(
    upnextDao: UpnextDao,
    private val tvMazeService: TvMazeService,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseRepository(upnextDao = upnextDao, tvMazeService = tvMazeService) {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _showCast = MutableLiveData<List<ShowCast>>()
    val showCast: LiveData<List<ShowCast>> = _showCast

    private val _showSeasons = MutableLiveData<List<ShowSeason>>()
    val showSeasons: LiveData<List<ShowSeason>> = _showSeasons

    private val _episodes = MutableLiveData<List<ShowSeasonEpisode>>()
    val episodes: LiveData<List<ShowSeasonEpisode>> = _episodes

    suspend fun getShowSummary(showId: Int): Flow<Result<ShowDetailSummary>> {
        return flow {
            emit(Result.Loading(true))
            val response =
                safeApiCall(Dispatchers.IO) {
                    tvMazeService.getShowSummaryAsync(showId.toString()).await().asDomainModel()
                }
            emit(Result.Loading(false))
            emit(response)
        }
            .catch {
                firebaseCrashlytics.recordException(it)
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getPreviousEpisode(episodeRef: String?): Flow<Result<ShowPreviousEpisode>> {
        return flow {
            emit(Result.Loading(true))
            val previousEpisodeLink = episodeRef?.substring(
                episodeRef.lastIndexOf("/") + 1,
                episodeRef.length
            )?.replace("/", "")

            if (!previousEpisodeLink.isNullOrEmpty()) {
                val response = safeApiCall(Dispatchers.IO) {
                    tvMazeService.getPreviousEpisodeAsync(
                        previousEpisodeLink.replace("/", "")
                    ).await().asDomainModel()
                }
                emit(Result.Loading(false))
                emit(response)
            }
        }
            .catch {
                firebaseCrashlytics.recordException(it)
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getNextEpisode(episodeRef: String?): Flow<Result<ShowNextEpisode>> {
        return flow {
            emit(Result.Loading(true))
            val nextEpisodeLink = episodeRef?.substring(
                episodeRef.lastIndexOf("/") + 1,
                episodeRef.length
            )?.replace("/", "")

            if (!nextEpisodeLink.isNullOrEmpty()) {
                val response = safeApiCall(Dispatchers.IO) {
                    tvMazeService.getNextEpisodeAsync(
                        nextEpisodeLink.replace("/", "")
                    ).await().asDomainModel()
                }
                emit(Result.Loading(false))
                emit(response)
            }
        }
            .catch {
                firebaseCrashlytics.recordException(it)
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getShowCast(showId: Int): Flow<Result<List<ShowCast>>> {
        return flow {
            emit(Result.Loading(true))
            val response = safeApiCall(Dispatchers.IO) {
                tvMazeService.getShowCastAsync(showId.toString()).await().asDomainModel()
            }
            emit(Result.Loading(false))
            emit(response)
        }
            .catch {
                firebaseCrashlytics.recordException(it)
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getShowSeasons(showId: Int): Flow<Result<List<ShowSeason>>> {
        return flow {
            emit(Result.Loading(true))
            val response = safeApiCall(Dispatchers.IO) {
                tvMazeService.getShowSeasonsAsync(showId.toString()).await().asDomainModel()
            }
            emit(Result.Loading(false))
            emit(response)
        }
            .catch {
                firebaseCrashlytics.recordException(it)
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getShowSeasonEpisodes(
        showId: Int,
        seasonNumber: Int
    ): Flow<Result<List<ShowSeasonEpisode>>> {
        return flow {
            emit(Result.Loading(true))
            val response = safeApiCall(Dispatchers.IO) {
                tvMazeService.getSeasonEpisodesAsync(showId.toString()).await().asDomainModel()
                    .filter { it.season == seasonNumber }
            }
            emit(Result.Loading(false))
            emit(response)
        }
            .catch {
                firebaseCrashlytics.recordException(it)
            }
            .flowOn(Dispatchers.IO)
    }
}