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

import com.theupnextapp.core.data.BuildConfig
import com.theupnextapp.domain.TrackingProvider
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TrendingShow
import com.theupnextapp.network.SimklService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

import com.theupnextapp.database.DatabaseWatchlistShows
import com.theupnextapp.database.TraktDao

class SimklRepository @Inject constructor(
    private val simklService: SimklService,
    private val traktDao: TraktDao
) : TrackingProvider {

    override val providerId: String = "simkl"

    private val _isAuthorized = MutableStateFlow(false)
    override val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

    suspend fun saveSyncShows(shows: List<com.theupnextapp.network.models.simkl.NetworkSimklShowItem>) {
        val databaseShows = shows.map { networkShow ->
            DatabaseWatchlistShows(
                id = networkShow.ids?.simklId ?: networkShow.ids?.tmdbId?.toIntOrNull(),
                title = networkShow.title,
                year = networkShow.year?.toString(),
                mediumImageUrl = null, // Can be populated later by TvMaze fallback
                originalImageUrl = null,
                imdbID = networkShow.ids?.imdbId,
                slug = null,
                tmdbID = networkShow.ids?.tmdbId?.toIntOrNull(),
                traktID = null,
                tvdbID = networkShow.ids?.tvdbId?.toIntOrNull(),
                tvMazeID = null,
                network = null,
                status = null,
                rating = null
            )
        }
        traktDao.insertAllWatchlistShows(*databaseShows.toTypedArray())
    }

    suspend fun clearSyncShows() {
        traktDao.deleteAllWatchlistShows()
    }

    private val _trendingShows = MutableStateFlow<List<TrendingShow>>(emptyList())
    override val trendingShows: Flow<List<TrendingShow>> = _trendingShows.asStateFlow()

    private val _isLoadingTrending = MutableStateFlow(false)
    override val isLoadingTrending: StateFlow<Boolean> = _isLoadingTrending.asStateFlow()

    private val _popularShows = MutableStateFlow<List<TraktPopularShows>>(emptyList())
    override val popularShows: Flow<List<TraktPopularShows>> = _popularShows.asStateFlow()

    private val _mostAnticipatedShows = MutableStateFlow<List<TraktMostAnticipated>>(emptyList())
    override val mostAnticipatedShows: Flow<List<TraktMostAnticipated>> = _mostAnticipatedShows.asStateFlow()

    override suspend fun refreshTrendingShows() {
        if (_isLoadingTrending.value) return
        _isLoadingTrending.value = true
        try {
            val response = simklService.getTrendingShows(
                token = null // TODO: Pass user auth token when OAuth is implemented
            )

            if (response.isSuccessful && response.body() != null) {
                val mappedShows = response.body()!!.map { networkShow ->
                    TrendingShow(
                        id = networkShow.ids?.simklId,
                        title = networkShow.title,
                        year = networkShow.year?.toString(),
                        mediumImageUrl = networkShow.poster,
                        originalImageUrl = networkShow.poster,
                        imdbID = networkShow.ids?.imdbId,
                        tmdbID = networkShow.ids?.tmdbId?.toIntOrNull(),
                        tvMazeID = null, // SIMKL may not return TVMaze
                        providerId = providerId
                    )
                }
                _trendingShows.value = mappedShows
            }
        } catch (e: Exception) {
            // Handle error, optionally update an error state flow
        } finally {
            _isLoadingTrending.value = false
        }
    }

    override suspend fun refreshPopularShows() {
        // TODO: Implement SIMKL Popular endpoints later
    }

    override suspend fun refreshMostAnticipatedShows() {
        // TODO: Implement SIMKL Anticipated endpoints later
    }

}
