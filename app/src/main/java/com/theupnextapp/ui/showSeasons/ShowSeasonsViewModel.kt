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

package com.theupnextapp.ui.showSeasons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import com.theupnextapp.work.SyncWatchProgressWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowSeasonsViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val showDetailRepository: ShowDetailRepository,
        private val watchProgressRepository: WatchProgressRepository,
        private val localWorkManager: WorkManager,
        private val traktRepository: TraktRepository,
        val traktAuthManager: TraktAuthManager,
    ) : BaseTraktViewModel(
            traktRepository,
            localWorkManager,
            traktAuthManager,
        ) {
        private val _isLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
        val isLoading: kotlinx.coroutines.flow.StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _showSeasons = kotlinx.coroutines.flow.MutableStateFlow<List<ShowSeason>?>(null)
        val showSeasons: kotlinx.coroutines.flow.StateFlow<List<ShowSeason>?> = _showSeasons.asStateFlow()

        private var currentShowTvMazeId: Int? = null
        private var currentShowTraktId: Int? = null
        private var currentShowImdbId: String? = null

        fun setSelectedShow(showDetailArg: ShowDetailArg?) {
            showDetailArg?.let { selectedShow ->
                savedStateHandle.set(SHOW_ID, selectedShow.showId)

                currentShowTvMazeId = selectedShow.showId?.toIntOrNull()
                currentShowTraktId = selectedShow.showTraktId
                currentShowImdbId = selectedShow.imdbID

                viewModelScope.launch {
                    val tvMazeId = savedStateHandle.get<String>(SHOW_ID)?.toIntOrNull() ?: return@launch

                    val seasonsFlow = showDetailRepository.getShowSeasons(tvMazeId)
                    val watchedEpisodesFlow =
                        currentShowTraktId?.let { traktId ->
                            watchProgressRepository.getWatchedEpisodesForShow(traktId)
                        } ?: flowOf(emptyList())

                    combine(seasonsFlow, watchedEpisodesFlow) { seasonsResult, watchedEpisodes ->
                        when (seasonsResult) {
                            is Result.Success -> {
                                val episodesPerSeasonMap = watchedEpisodes.groupBy { it.seasonNumber }

                                seasonsResult.data.map { season ->
                                    val watchedInSeason = episodesPerSeasonMap[season.seasonNumber]?.size ?: 0
                                    val count = season.episodeCount
                                    val isFullyWatched =
                                        count != null &&
                                            count > 0 &&
                                            watchedInSeason >= count
                                    season.copy(isWatched = isFullyWatched)
                                }
                            }
                            is Result.Loading -> {
                                _isLoading.value = seasonsResult.status
                                null
                            }
                            else -> null
                        }
                    }.collect { processedSeasons ->
                        processedSeasons?.let { _showSeasons.value = it }
                    }
                }
            }
        }

        fun onToggleSeasonWatched(season: ShowSeason) {
            val showTraktId = currentShowTraktId ?: return
            val seasonNum = season.seasonNumber ?: return

            viewModelScope.launch {
                val isCurrentlyWatched = season.isWatched == true

                if (isCurrentlyWatched) {
                    watchProgressRepository.markSeasonUnwatched(
                        showTraktId = showTraktId,
                        seasonNumber = seasonNum,
                    )
                } else {
                    val episodesResult =
                        showDetailRepository.getShowSeasonEpisodes(
                            showId = currentShowTvMazeId ?: return@launch,
                            seasonNumber = seasonNum,
                        )

                    val result = episodesResult.firstOrNull { it !is Result.Loading }
                    if (result is Result.Success) {
                        watchProgressRepository.markSeasonWatched(
                            showTraktId = showTraktId,
                            showTvMazeId = currentShowTvMazeId,
                            showImdbId = currentShowImdbId,
                            seasonNumber = seasonNum,
                            episodes = result.data,
                        )
                    }
                }
                triggerSyncIfAuthenticated()
            }
        }

        private fun triggerSyncIfAuthenticated() {
            viewModelScope.launch {
                traktRepository.traktAccessToken.firstOrNull()?.access_token?.let { token ->
                    val syncWork =
                        OneTimeWorkRequestBuilder<SyncWatchProgressWorker>()
                            .setInputData(
                                Data
                                    .Builder()
                                    .putString(SyncWatchProgressWorker.ARG_TOKEN, token)
                                    .build(),
                            ).build()
                    localWorkManager.enqueue(syncWork)
                }
            }
        }

        companion object {
            const val SHOW_ID = "showId"
        }
    }
