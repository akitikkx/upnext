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
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.work.RefreshTodayShowsWorker
import com.theupnextapp.work.RefreshTomorrowShowsWorker
import com.theupnextapp.work.RefreshYesterdayShowsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val isLoadingYesterdayShows = dashboardRepository.isLoadingYesterdayShows

    val isLoadingTodayShows = dashboardRepository.isLoadingTodayShows

    val isLoadingTomorrowShows = dashboardRepository.isLoadingTomorrowShows

    val yesterdayShowsList = dashboardRepository.yesterdayShows.asLiveData()

    val todayShowsList = dashboardRepository.todayShows.asLiveData()

    val tomorrowShowsList = dashboardRepository.tomorrowShows.asLiveData()

    val yesterdayShowsTableUpdate =
        dashboardRepository.tableUpdate(DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName).asLiveData()

    val todayShowsTableUpdate =
        dashboardRepository.tableUpdate(DatabaseTables.TABLE_TODAY_SHOWS.tableName).asLiveData()

    val tomorrowShowsTableUpdate =
        dashboardRepository.tableUpdate(DatabaseTables.TABLE_TOMORROW_SHOWS.tableName).asLiveData()

    private val yesterdayShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(yesterdayShowsList) {
            value = it.isNullOrEmpty() == true
        }
    }

    private val todayShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(todayShowsList) {
            value = it.isNullOrEmpty() == true
        }
    }

    private val tomorrowShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(tomorrowShowsList) {
            value = it.isNullOrEmpty() == true
        }
    }

    val isLoading = MediatorLiveData<Boolean>().apply {
        addSource(isLoadingYesterdayShows) {
            value = it
        }
        addSource(isLoadingTodayShows) {
            value = it
        }
        addSource(isLoadingTomorrowShows) {
            value = it
        }
    }


    fun onYesterdayShowsTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = DateUtils.MINUTES
                )
            }
        val isYesterdayShowsEmpty =
            yesterdayShowsEmpty.value == null || yesterdayShowsEmpty.value == true

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.DASHBOARD_ITEMS.intervalMins && (isLoadingYesterdayShows.value == false || isLoadingYesterdayShows.value == null)) {
                workManager.enqueue(OneTimeWorkRequest.from(RefreshYesterdayShowsWorker::class.java))
            }
            // no updates have been done yet for this table
        } else if (isYesterdayShowsEmpty && (isLoadingYesterdayShows.value == null || isLoadingYesterdayShows.value == false) && tableUpdate == null && diffInMinutes == null) {
            workManager.enqueue(OneTimeWorkRequest.from(RefreshYesterdayShowsWorker::class.java))
        }
    }

    fun onTodayShowsTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = DateUtils.MINUTES
                )
            }

        val isTodayShowsEmpty = todayShowsEmpty.value == null || todayShowsEmpty.value == true

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.DASHBOARD_ITEMS.intervalMins && (isLoadingTodayShows.value == false || isLoadingTodayShows.value == null)) {
                workManager.enqueue(OneTimeWorkRequest.from(RefreshTodayShowsWorker::class.java))
            }
            // no updates have been done yet for this table
        } else if (isTodayShowsEmpty && (isLoadingTodayShows.value == null || isLoadingTodayShows.value == false) && tableUpdate == null && diffInMinutes == null) {
            workManager.enqueue(OneTimeWorkRequest.from(RefreshTodayShowsWorker::class.java))
        }
    }

    fun onTomorrowShowsTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = "minutes"
                )
            }

        val isTomorrowShowsEmpty =
            tomorrowShowsEmpty.value == null || tomorrowShowsEmpty.value == true

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.DASHBOARD_ITEMS.intervalMins && (isLoadingTomorrowShows.value == false || isLoadingTomorrowShows.value == null)) {
                workManager.enqueue(OneTimeWorkRequest.from(RefreshTomorrowShowsWorker::class.java))
            }
            // no updates have been done yet for this table
        } else if (isTomorrowShowsEmpty && (isLoadingTomorrowShows.value == null || isLoadingTomorrowShows.value == false) && tableUpdate == null && diffInMinutes == null) {
            workManager.enqueue(OneTimeWorkRequest.from(RefreshTomorrowShowsWorker::class.java))
        }
    }
}