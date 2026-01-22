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

package com.theupnextapp.ui.showSeasonEpisodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import com.theupnextapp.work.SyncWatchProgressWorker
import com.theupnextapp.common.utils.TraktAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowSeasonEpisodesViewModel
    @Inject
    constructor(
        private val showDetailRepository: ShowDetailRepository,
        private val watchProgressRepository: WatchProgressRepository,
        private val localWorkManager: WorkManager,
        traktRepository: TraktRepository,
        val traktAuthManager: TraktAuthManager,
    ) : BaseTraktViewModel(
            traktRepository,
            localWorkManager,
            traktAuthManager,
        ) {
        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        private val _episodes = MutableLiveData<List<ShowSeasonEpisode>?>()
        val episodes: LiveData<List<ShowSeasonEpisode>?> = _episodes

        private val _seasonNumber = MutableLiveData<Int?>()
        val seasonNumber: LiveData<Int?> = _seasonNumber

        private var currentShowTraktId: Int? = null
        private var currentShowTvMazeId: Int? = null
        private var currentShowImdbId: String? = null
        private var currentSeasonNumber: Int? = null

        fun selectedSeason(showSeasonEpisodesArg: ShowSeasonEpisodesArg?) {
            showSeasonEpisodesArg?.let { selectedSeason ->
                _seasonNumber.value = selectedSeason.seasonNumber

                currentShowTraktId = selectedSeason.showTraktId
                currentShowTvMazeId = selectedSeason.showId
                currentShowImdbId = selectedSeason.imdbID
                currentSeasonNumber = selectedSeason.seasonNumber

                selectedSeason.showId?.let { showId ->
                    selectedSeason.seasonNumber?.let { seasonNumber ->
                        loadEpisodesWithWatchStatus(showId, seasonNumber)
                    }
                }
            }
        }

        private fun loadEpisodesWithWatchStatus(
            showId: Int,
            seasonNumber: Int,
        ) {
            viewModelScope.launch {
                val episodesFlow =
                    showDetailRepository.getShowSeasonEpisodes(
                        showId = showId,
                        seasonNumber = seasonNumber,
                    )

                val watchedEpisodesFlow =
                    currentShowTraktId?.let {
                        watchProgressRepository.getWatchedEpisodesForShow(it)
                    } ?: flowOf(emptyList())

                combine(episodesFlow, watchedEpisodesFlow) { episodeResult, watchedEpisodes ->
                    when (episodeResult) {
                        is Result.Success -> {
                            val watchedSet =
                                watchedEpisodes
                                    .filter { it.seasonNumber == seasonNumber }
                                    .map { it.episodeNumber }
                                    .toSet()

                            episodeResult.data.map { episode ->
                                episode.copy(isWatched = episode.number in watchedSet)
                            }
                        }

                        is Result.Loading -> {
                            _isLoading.postValue(episodeResult.status)
                            null
                        }

                        else -> null
                    }
                }.collect { episodes ->
                    episodes?.let { _episodes.postValue(it) }
                }
            }
        }

        fun onToggleWatched(episode: ShowSeasonEpisode) {
            if (isAuthorizedOnTrakt.value != true) return
            val showTraktId = currentShowTraktId ?: return
            val season = episode.season ?: return
            val episodeNum = episode.number ?: return

            viewModelScope.launch {
                if (episode.isWatched) {
                    watchProgressRepository.markEpisodeUnwatched(
                        showTraktId = showTraktId,
                        seasonNumber = season,
                        episodeNumber = episodeNum,
                    )
                } else {
                    watchProgressRepository.markEpisodeWatched(
                        showTraktId = showTraktId,
                        showTvMazeId = currentShowTvMazeId,
                        showImdbId = currentShowImdbId,
                        seasonNumber = season,
                        episodeNumber = episodeNum,
                    )
                }

                triggerSyncIfAuthenticated()
            }
        }

        private fun triggerSyncIfAuthenticated() {
            traktAccessToken.value?.access_token?.let { token ->
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
