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

package com.theupnextapp.ui.dashboard

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.work.WorkManager
import com.theupnextapp.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        dashboardRepository: DashboardRepository,
        private val workManager: WorkManager,
    ) : ViewModel() {
        val isLoadingYesterdayShows = dashboardRepository.isLoadingYesterdayShows
        val isLoadingTodayShows = dashboardRepository.isLoadingTodayShows
        val isLoadingTomorrowShows = dashboardRepository.isLoadingTomorrowShows

        val yesterdayShowsList = dashboardRepository.yesterdayShows.asLiveData()

        val todayShowsList = dashboardRepository.todayShows.asLiveData()

        val tomorrowShowsList = dashboardRepository.tomorrowShows.asLiveData()

    private val yesterdayShowsEmpty =
            MediatorLiveData<Boolean>().apply {
                addSource(yesterdayShowsList) {
                    value = it.isNullOrEmpty() == true
                }
            }

        private val todayShowsEmpty =
            MediatorLiveData<Boolean>().apply {
                addSource(todayShowsList) {
                    value = it.isNullOrEmpty() == true
                }
            }

        private val tomorrowShowsEmpty =
            MediatorLiveData<Boolean>().apply {
                addSource(tomorrowShowsList) {
                    value = it.isNullOrEmpty() == true
                }
            }

        val isLoading =
            MediatorLiveData<Boolean>().apply {
                val updateLoadingState = {
                    // Value is true if any of the individual loading states are true
                    // Ensure you handle nulls from the LiveData sources if they haven't emitted yet.
                    // isLoadingYesterdayShows.value could be null initially.
                    value = (isLoadingYesterdayShows.value == true) ||
                        (isLoadingTodayShows.value == true) ||
                        (isLoadingTomorrowShows.value == true)
                }

                addSource(isLoadingYesterdayShows) {
                    updateLoadingState()
                }
                addSource(isLoadingTodayShows) {
                    updateLoadingState()
                }
                addSource(isLoadingTomorrowShows) {
                    updateLoadingState()
                }
            }

}
