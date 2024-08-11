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

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ShowDetailRepository(
    upnextDao: UpnextDao,
    private val tvMazeService: TvMazeService, private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseRepository(upnextDao = upnextDao, tvMazeService = tvMazeService) {

    private val _isLoading = MutableStateFlow(false) // Add isLoading back
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private suspend fun <T> makeNetworkCall(block: suspend () -> Result<T>): Flow<Result<T>> =
        flow {
            _isLoading.value = true
            emit(block()) // Emit the Result from the block directly
            _isLoading.value = false
        }.catch {
            firebaseCrashlytics.recordException(it)
            _isLoading.value = false
            emit(Result.UnknownError(it))
        }.flowOn(Dispatchers.IO)

    private fun extractEpisodeId(episodeRef: String?): String? = episodeRef?.substringAfterLast("/")

    suspend fun getShowSummary(showId: Int): Flow<Result<ShowDetailSummary>> = makeNetworkCall {
        Result.Success(tvMazeService.getShowSummaryAsync(showId.toString()).await().asDomainModel())
    }

    suspend fun getPreviousEpisode(episodeRef: String?): Flow<Result<ShowPreviousEpisode>> =
        makeNetworkCall {
            val episodeId = extractEpisodeId(episodeRef)
            if (episodeId != null) {
                Result.Success(
                    tvMazeService.getPreviousEpisodeAsync(episodeId).await().asDomainModel()
                )
            } else {
                Result.UnknownError(Exception("Invalid episode reference"))
            }
        }

    suspend fun getNextEpisode(episodeRef: String?): Flow<Result<ShowNextEpisode>> =
        makeNetworkCall {
            val episodeId = extractEpisodeId(episodeRef)
            if (episodeId != null) {
                Result.Success(tvMazeService.getNextEpisodeAsync(episodeId).await().asDomainModel())
            } else {
                Result.UnknownError(Exception("Invalid episode reference"))
            }
        }

    suspend fun getShowCast(showId: Int): Flow<Result<List<ShowCast>>> = makeNetworkCall {
        Result.Success(tvMazeService.getShowCastAsync(showId.toString()).await().asDomainModel())
    }

    suspend fun getShowSeasons(showId: Int): Flow<Result<List<ShowSeason>>> = makeNetworkCall {
        Result.Success(tvMazeService.getShowSeasonsAsync(showId.toString()).await().asDomainModel())
    }

    suspend fun getShowSeasonEpisodes(
        showId: Int,
        seasonNumber: Int
    ): Flow<Result<List<ShowSeasonEpisode>>> = makeNetworkCall {
        Result.Success(tvMazeService.getSeasonEpisodesAsync(showId.toString()).await()
            .asDomainModel()
            .filter { it.season == seasonNumber })
    }
}
