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
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import com.theupnextapp.work.SyncWatchProgressWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShowSeasonEpisodesViewModel
    @Inject
    constructor(
        private val showDetailRepository: ShowDetailRepository,
        private val watchProgressRepository: WatchProgressRepository,
        private val simklRepository: com.theupnextapp.repository.SimklRepository,
        private val providerManager: com.theupnextapp.repository.ProviderManager,
        private val simklAuthManager: com.theupnextapp.repository.SimklAuthManager,
        private val localWorkManager: WorkManager,
        private val traktRepository: TraktRepository,
        val traktAuthManager: TraktAuthManager,
    ) : BaseTraktViewModel(
            traktRepository,
            localWorkManager,
            traktAuthManager,
        ) {
        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        val activeProvider: StateFlow<String> = providerManager.activeProvider
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT
            )

        val isAuthorizedOnProvider: StateFlow<Boolean> = combine(
            activeProvider,
            traktAuthManager.traktAuthState,
            simklAuthManager.simklAccessToken
        ) { provider, traktState, simklToken ->
            if (provider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                simklToken != null
            } else {
                traktState == com.theupnextapp.domain.TraktAuthState.LoggedIn
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            false
        )

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
                val activeProviderFlow = providerManager.activeProvider

                val episodesFlow =
                    showDetailRepository.getShowSeasonEpisodes(
                        showId = showId,
                        seasonNumber = seasonNumber,
                    )

                val watchedEpisodesFlow =
                    currentShowTraktId?.let {
                        watchProgressRepository.getWatchedEpisodesForShow(it)
                    } ?: flowOf(emptyList())

                val simklWatchedEpisodesFlow =
                    currentShowImdbId?.let {
                        simklRepository.getWatchedEpisodesForShowByImdbId(it)
                    } ?: flowOf(emptyList())

                combine(episodesFlow, watchedEpisodesFlow, simklWatchedEpisodesFlow, activeProviderFlow) { episodeResult, watchedEpisodes, simklWatchedEpisodes, activeProvider ->
                    val provider = activeProvider

                    if (provider == com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT) {
                        // Pull latest watched state from Trakt before reading local DB
                        currentShowTraktId?.let { traktId ->
                            traktRepository.traktAccessToken.value?.access_token?.let { token ->
                                try {
                                    watchProgressRepository.refreshWatchedFromTrakt(
                                        token = token,
                                        showTraktId = traktId,
                                    )
                                } catch (e: Exception) {
                                    Timber.w(e, "Failed to refresh watched state from Trakt, using local cache")
                                }
                            }
                        }
                    }

                    when (episodeResult) {
                        is Result.Success -> {
                            val isSimkl = provider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL
                            val watchedSet = if (isSimkl) {
                                simklWatchedEpisodes
                                    .filter { it.seasonNumber == seasonNumber }
                                    .map { it.episodeNumber }
                                    .toSet()
                            } else {
                                watchedEpisodes
                                    .filter { it.seasonNumber == seasonNumber }
                                    .map { it.episodeNumber }
                                    .toSet()
                            }

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
            val showTraktId = currentShowTraktId ?: return
            val season = episode.season ?: return
            val episodeNum = episode.number ?: return

            viewModelScope.launch {
                val activeProvider = providerManager.activeProvider.firstOrNull() ?: com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT
                if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                    val currentSimklToken = simklAuthManager.simklAccessToken.firstOrNull()?.accessToken
                    if (currentSimklToken != null) {
                        if (episode.isWatched) {
                            simklRepository.markEpisodeUnwatched(
                                simklId = showTraktId, // map appropriately later if needed
                                imdbID = currentShowImdbId,
                                seasonNumber = season,
                                episodeNumber = episodeNum,
                                token = currentSimklToken
                            )
                        } else {
                            simklRepository.markEpisodeWatched(
                                simklId = showTraktId, // map appropriately later if needed
                                imdbID = currentShowImdbId,
                                seasonNumber = season,
                                episodeNumber = episodeNum,
                                token = currentSimklToken
                            )
                        }
                        triggerSimklSync(currentSimklToken)
                    }
                } else {
                    if (isAuthorizedOnTrakt.value != true) return@launch
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
        }

        fun markSeasonAsWatched() {
            val showTraktId = currentShowTraktId ?: return
            val season = currentSeasonNumber ?: return
            val episodesList = _episodes.value ?: return

            viewModelScope.launch {
                val activeProvider = providerManager.activeProvider.firstOrNull() ?: com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT
                if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                    val currentSimklToken = simklAuthManager.simklAccessToken.firstOrNull()?.accessToken
                    if (currentSimklToken != null) {
                        simklRepository.markSeasonWatched(
                            simklId = showTraktId, // map appropriately later if needed
                            imdbID = currentShowImdbId,
                            seasonNumber = season,
                            token = currentSimklToken
                        )
                        triggerSimklSync(currentSimklToken)
                    }
                } else {
                    if (isAuthorizedOnTrakt.value != true) return@launch
                    watchProgressRepository.markSeasonWatched(
                        showTraktId = showTraktId,
                        showTvMazeId = currentShowTvMazeId,
                        showImdbId = currentShowImdbId,
                        seasonNumber = season,
                        episodes = episodesList,
                    )
                    triggerSyncIfAuthenticated()
                }
            }
        }

        fun markSeasonAsUnwatched() {
            val showTraktId = currentShowTraktId ?: return
            val season = currentSeasonNumber ?: return

            viewModelScope.launch {
                val activeProvider = providerManager.activeProvider.firstOrNull() ?: com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT
                if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                    val currentSimklToken = simklAuthManager.simklAccessToken.firstOrNull()?.accessToken
                    if (currentSimklToken != null) {
                        simklRepository.markSeasonUnwatched(
                            simklId = showTraktId, // map appropriately later if needed
                            imdbID = currentShowImdbId,
                            seasonNumber = season,
                            token = currentSimklToken
                        )
                        triggerSimklSync(currentSimklToken)
                    }
                } else {
                    if (isAuthorizedOnTrakt.value != true) return@launch
                    watchProgressRepository.markSeasonUnwatched(
                        showTraktId = showTraktId,
                        seasonNumber = season,
                    )
                    triggerSyncIfAuthenticated()
                }
            }
        }

        private fun triggerSimklSync(token: String) {
            val syncWork =
                OneTimeWorkRequestBuilder<com.theupnextapp.work.SimklSyncWorker>()
                    .setInputData(
                        Data
                            .Builder()
                            .putString(com.theupnextapp.work.SimklSyncWorker.ARG_TOKEN, token)
                            .build(),
                    ).build()
            localWorkManager.enqueue(syncWork)
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
    }
