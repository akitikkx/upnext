package com.theupnextapp.ui.dashboard

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.work.RefreshTodayShowsWorker
import com.theupnextapp.work.RefreshTomorrowShowsWorker
import com.theupnextapp.work.RefreshYesterdayShowsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val upnextRepository: UpnextRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val isLoadingYesterdayShows = upnextRepository.isLoadingYesterdayShows

    val isLoadingTodayShows = upnextRepository.isLoadingTodayShows

    val isLoadingTomorrowShows = upnextRepository.isLoadingTomorrowShows

    val yesterdayShowsList = upnextRepository.yesterdayShows

    val todayShowsList = upnextRepository.todayShows

    val tomorrowShowsList = upnextRepository.tomorrowShows

    val yesterdayShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName)

    val todayShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_TODAY_SHOWS.tableName)

    val tomorrowShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_TOMORROW_SHOWS.tableName)

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

    fun onRefreshShowsClick() {
        requestShowsUpdate()
    }

    private fun requestShowsUpdate() {
        viewModelScope.launch {
            upnextRepository.refreshYesterdayShows(
                DEFAULT_COUNTRY_CODE,
                DateUtils.yesterdayDate()
            )
            upnextRepository.refreshTodayShows(
                DEFAULT_COUNTRY_CODE,
                DateUtils.currentDate()
            )
            upnextRepository.refreshTomorrowShows(
                DEFAULT_COUNTRY_CODE,
                DateUtils.tomorrowDate()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    companion object {
        const val DEFAULT_COUNTRY_CODE = "US"
    }
}