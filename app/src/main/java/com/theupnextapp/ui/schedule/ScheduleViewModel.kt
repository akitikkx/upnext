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

package com.theupnextapp.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel
    @Inject
    constructor(
        dashboardRepository: DashboardRepository,
        private val workManager: WorkManager,
        private val firebaseAnalytics: FirebaseAnalytics,
    ) : ViewModel() {

        val isLoadingYesterdayShows = dashboardRepository.isLoadingYesterdayShows
        val isLoadingTodayShows = dashboardRepository.isLoadingTodayShows
        val isLoadingTomorrowShows = dashboardRepository.isLoadingTomorrowShows

        val yesterdayShowsList: StateFlow<List<ScheduleShow>> = dashboardRepository.yesterdayShows
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        val todayShowsList: StateFlow<List<ScheduleShow>> = dashboardRepository.todayShows
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        val tomorrowShowsList: StateFlow<List<ScheduleShow>> = dashboardRepository.tomorrowShows
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        val isLoading: StateFlow<Boolean> =
            combine(
                isLoadingYesterdayShows,
                isLoadingTodayShows,
                isLoadingTomorrowShows
            ) { yesterday, today, tomorrow ->
                yesterday || today || tomorrow
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

        init {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "Schedule")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "ScheduleScreen")
            }

            viewModelScope.launch {
                var trace: com.google.firebase.perf.metrics.Trace? = null
                isLoading.collect { loading ->
                    if (loading) {
                        if (trace == null) {
                            try {
                                trace = com.google.firebase.perf.FirebasePerformance.getInstance().newTrace("schedule_data_load")
                                trace?.start()
                            } catch (e: Exception) {
                                // Ignored in unit tests
                            }
                        }
                    } else {
                        try {
                            trace?.stop()
                        } catch (e: Exception) {
                            // Ignored
                        }
                        trace = null
                    }
                }
            }
        }
    }
