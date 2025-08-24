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
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.RefreshTraktAnticipatedShowsWorker
import com.theupnextapp.work.RefreshTraktPopularShowsWorker
import com.theupnextapp.work.RefreshTraktTrendingShowsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
        traktRepository: TraktRepository,
        private val workManager: WorkManager,
    ) : ViewModel() {
        private val _isPullRefreshing = MutableStateFlow(false)
        val isPullRefreshing: StateFlow<Boolean> = _isPullRefreshing.asStateFlow()

        val trendingShows: StateFlow<List<TraktTrendingShows>> =
            traktRepository.traktTrendingShows
                .stateIn(
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

        internal val isLoadingTraktTrending: StateFlow<Boolean> = traktRepository.isLoadingTraktTrending
        internal val isLoadingTraktPopular: StateFlow<Boolean> = traktRepository.isLoadingTraktPopular
        internal val isLoadingTraktMostAnticipated: StateFlow<Boolean> =
            traktRepository.isLoadingTraktMostAnticipated

        val isLoading: StateFlow<Boolean> =
            combine(
                isLoadingTraktTrending,
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

            val constraints =
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            // For Popular Shows
            // We enqueue the worker. The worker calls repository.refreshPopularShows(forceRefresh=true).
            // The repository then internally uses isUpdateNeededByDay OR forceRefresh to decide.
            // We only need to check the loading state here to avoid enqueueing if already loading,
            // and the '''forceRefresh''' or '''isEmpty''' to decide if we should even consider enqueueing.
            viewModelScope.launch {
                if (forceRefresh || popularShowsEmpty.value) {
                    if (!isLoadingTraktPopular.value) {
                        Timber.i("Enqueueing RefreshTraktPopularShowsWorker due to forceRefresh ($forceRefresh) or empty data.")
                        val popularWorkRequest =
                            OneTimeWorkRequestBuilder<RefreshTraktPopularShowsWorker>()
                                .setConstraints(constraints)
                                .build()
                        workManager.enqueueUniqueWork(
                            RefreshTraktPopularShowsWorker.WORK_NAME,
                            ExistingWorkPolicy.KEEP,
                            popularWorkRequest,
                        )
                    } else {
                        Timber.d("RefreshTraktPopularShowsWorker NOT enqueued, already loading.")
                    }
                } else {
                    Timber.d(
                        "RefreshTraktPopularShowsWorker NOT enqueued, not forced and data not empty. Daily check handled by worker's internal logic on next run if needed.",
                    )
                }
            }

            // For Trending Shows
            viewModelScope.launch {
                if (forceRefresh || trendingShowsEmpty.value) {
                    if (!isLoadingTraktTrending.value) {
                        Timber.i("Enqueueing RefreshTraktTrendingShowsWorker due to forceRefresh ($forceRefresh) or empty data.")
                        val trendingWorkRequest =
                            OneTimeWorkRequestBuilder<RefreshTraktTrendingShowsWorker>()
                                .setConstraints(constraints)
                                .build()
                        workManager.enqueueUniqueWork(
                            RefreshTraktTrendingShowsWorker.WORK_NAME,
                            ExistingWorkPolicy.KEEP,
                            trendingWorkRequest,
                        )
                    } else {
                        Timber.d("RefreshTraktTrendingShowsWorker NOT enqueued, already loading.")
                    }
                } else {
                    Timber.d("RefreshTraktTrendingShowsWorker NOT enqueued, not forced and data not empty.")
                }
            }

            // For Most Anticipated Shows
            viewModelScope.launch {
                if (forceRefresh || mostAnticipatedShowsEmpty.value) {
                    if (!isLoadingTraktMostAnticipated.value) {
                        Timber.i("Enqueueing RefreshTraktAnticipatedShowsWorker due to forceRefresh ($forceRefresh) or empty data.")
                        val anticipatedWorkRequest =
                            OneTimeWorkRequestBuilder<RefreshTraktAnticipatedShowsWorker>()
                                .setConstraints(constraints)
                                .build()
                        workManager.enqueueUniqueWork(
                            RefreshTraktAnticipatedShowsWorker.WORK_NAME,
                            ExistingWorkPolicy.KEEP,
                            anticipatedWorkRequest,
                        )
                    } else {
                        Timber.d("RefreshTraktAnticipatedShowsWorker NOT enqueued, already loading.")
                    }
                } else {
                    Timber.d("RefreshTraktAnticipatedShowsWorker NOT enqueued, not forced and data not empty.")
                }
            }
        }
    }
