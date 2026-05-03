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

package com.theupnextapp.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.domain.TrendingShow
import com.theupnextapp.repository.ProviderManager
import com.theupnextapp.repository.SimklRepository
import com.theupnextapp.repository.TraktRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ExploreViewModel
    @Inject
    constructor(
        private val traktRepository: TraktRepository,
        private val simklRepository: SimklRepository,
        private val providerManager: ProviderManager,
        private val firebaseAnalytics: FirebaseAnalytics,
    ) : ViewModel() {
        private val _isPullRefreshing = MutableStateFlow(false)
        val isPullRefreshing: StateFlow<Boolean> = _isPullRefreshing.asStateFlow()

        val trendingShows: StateFlow<List<TrendingShow>> =
            providerManager.activeProvider.flatMapLatest { provider ->
                if (provider == ProviderManager.PROVIDER_SIMKL) {
                    simklRepository.trendingShows
                } else {
                    traktRepository.trendingShows
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        val popularShows: StateFlow<List<TraktPopularShows>> =
            traktRepository.traktPopularShows
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        val mostAnticipatedShows: StateFlow<List<TraktMostAnticipated>> =
            traktRepository.traktMostAnticipatedShows
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        internal val isLoadingTrending: StateFlow<Boolean> =
            providerManager.activeProvider.flatMapLatest { provider ->
                if (provider == ProviderManager.PROVIDER_SIMKL) {
                    simklRepository.isLoadingTrending
                } else {
                    traktRepository.isLoadingTrending
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

        internal val isLoadingTraktPopular: StateFlow<Boolean> = traktRepository.isLoadingTraktPopular
        internal val isLoadingTraktMostAnticipated: StateFlow<Boolean> =
            traktRepository.isLoadingTraktMostAnticipated

        val isLoading: StateFlow<Boolean> =
            combine(
                isLoadingTrending,
                isLoadingTraktPopular,
                isLoadingTraktMostAnticipated,
            ) { trending, popular, anticipated ->
                val currentlyLoading = trending || popular || anticipated
                if (!currentlyLoading && _isPullRefreshing.value) {
                    _isPullRefreshing.value = false
                }
                currentlyLoading
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

        val trendingShowsEmpty: StateFlow<Boolean> =
            trendingShows
                .map { it.isEmpty() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

        val popularShowsEmpty: StateFlow<Boolean> =
            popularShows
                .map { it.isEmpty() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

        val mostAnticipatedShowsEmpty: StateFlow<Boolean> =
            mostAnticipatedShows
                .map { it.isEmpty() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

        init {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "Explore")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "ExploreScreen")
            }
            Timber.d("ExploreViewModel initialized. Triggering initial data check.")
            checkAndRefreshAllExploreData(forceRefresh = false)
        }

        /**
         * Call this method when the UI comes to the foreground or on a manual refresh trigger.
         * @param forceRefresh True if the user explicitly requested a refresh (e.g., swipe to refresh)
         *                     or if you want to bypass the daily check for a specific reason.
         */
        fun checkAndRefreshAllExploreData(forceRefresh: Boolean) {
            Timber.d("checkAndRefreshAllExploreData called with forceRefresh: $forceRefresh")

            if (forceRefresh) {
                // Only set to true if not already pull refreshing to avoid redundant state updates
                // and to correctly capture the start of a user-initiated refresh.
                if (!_isPullRefreshing.value) {
                    _isPullRefreshing.value = true
                }
            }

            // For Popular Shows
            viewModelScope.launch {
                if (forceRefresh || popularShowsEmpty.value) {
                    if (!isLoadingTraktPopular.value) {
                        Timber.i("Refreshing Trakt popular shows due to forceRefresh ($forceRefresh) or empty data.")
                        traktRepository.refreshTraktPopularShows(forceRefresh = forceRefresh)
                    } else {
                        Timber.d("RefreshTraktPopularShows NOT executed, already loading.")
                    }
                } else {
                    Timber.d("RefreshTraktPopularShows NOT executed, not forced and data not empty.")
                }
            }

            // For Trending Shows
            viewModelScope.launch {
                val activeProvider = providerManager.activeProvider.first()
                val isSimkl = activeProvider == ProviderManager.PROVIDER_SIMKL
                val isTrendingLoading = if (isSimkl) simklRepository.isLoadingTrending else traktRepository.isLoadingTraktTrending

                if (forceRefresh || trendingShowsEmpty.value) {
                    if (!isTrendingLoading.value) {
                        Timber.i("Refreshing trending shows due to forceRefresh ($forceRefresh) or empty data.")
                        if (isSimkl) {
                            simklRepository.refreshTrendingShows()
                        } else {
                            traktRepository.refreshTraktTrendingShows(forceRefresh = forceRefresh)
                        }
                    } else {
                        Timber.d("RefreshTrendingShows NOT executed, already loading.")
                    }
                } else {
                    Timber.d("RefreshTrendingShows NOT executed, not forced and data not empty.")
                }
            }

            // For Most Anticipated Shows
            viewModelScope.launch {
                if (forceRefresh || mostAnticipatedShowsEmpty.value) {
                    if (!isLoadingTraktMostAnticipated.value) {
                        Timber.i("Refreshing Trakt anticipated shows due to forceRefresh ($forceRefresh) or empty data.")
                        traktRepository.refreshTraktMostAnticipatedShows(forceRefresh = forceRefresh)
                    } else {
                        Timber.d("RefreshTraktAnticipatedShows NOT executed, already loading.")
                    }
                } else {
                    Timber.d("RefreshTraktAnticipatedShows NOT executed, not forced and data not empty.")
                }
            }
        }
    }
