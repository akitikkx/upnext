package com.theupnextapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : TraktViewModel(application) {

    private val upnextRepository = UpnextRepository(database)

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    private val _showFeaturesBottomSheet = MutableLiveData<Boolean>()

    val isLoadingNewShows = upnextRepository.isLoadingNewShows

    val isLoadingYesterdayShows = upnextRepository.isLoadingYesterdayShows

    val isLoadingTodayShows = upnextRepository.isLoadingTodayShows

    val isLoadingTomorrowShows = upnextRepository.isLoadingTomorrowShows

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val showFeaturesBottomSheet: LiveData<Boolean> = _showFeaturesBottomSheet

    val newShowsList = upnextRepository.newShows

    val yesterdayShowsList = upnextRepository.yesterdayShows

    val todayShowsList = upnextRepository.todayShows

    val tomorrowShowsList = upnextRepository.tomorrowShows

    val yesterdayShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName)

    val todayShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_TODAY_SHOWS.tableName)

    val tomorrowShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_TOMORROW_SHOWS.tableName)

    private val newShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(newShowsList) {
            value = it.isNullOrEmpty() == true
        }
    }

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
        addSource(isLoadingNewShows) {
            value = it
        }
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
                    type = "minutes"
                )
            }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.DASHBOARD_ITEMS.intervalMins && (isLoadingYesterdayShows.value == false || isLoadingYesterdayShows.value == null)) {
                viewModelScope?.launch {
                    upnextRepository.refreshYesterdayShows(
                        DEFAULT_COUNTRY_CODE,
                        DateUtils.yesterdayDate()
                    )
                }
            }
            // no updates have been done yet for this table
        } else if ((yesterdayShowsEmpty.value == true && isLoadingYesterdayShows.value == false) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                upnextRepository.refreshYesterdayShows(
                    DEFAULT_COUNTRY_CODE,
                    DateUtils.yesterdayDate()
                )
            }
        }
    }

    fun onTodayShowsTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = "minutes"
                )
            }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.DASHBOARD_ITEMS.intervalMins && (isLoadingTodayShows.value == false || isLoadingTodayShows.value == null)) {
                viewModelScope?.launch {
                    upnextRepository.refreshTodayShows(
                        DEFAULT_COUNTRY_CODE,
                        DateUtils.currentDate()
                    )
                }
            }
            // no updates have been done yet for this table
        } else if ((todayShowsEmpty.value == true && isLoadingTodayShows.value == false) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                upnextRepository.refreshTodayShows(
                    DEFAULT_COUNTRY_CODE,
                    DateUtils.currentDate()
                )
            }
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

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.DASHBOARD_ITEMS.intervalMins && (isLoadingTomorrowShows.value == false || isLoadingTomorrowShows.value == null)) {
                viewModelScope?.launch {
                    upnextRepository.refreshTomorrowShows(
                        DEFAULT_COUNTRY_CODE,
                        DateUtils.tomorrowDate()
                    )
                }
            }
            // no updates have been done yet for this table
        } else if ((tomorrowShowsEmpty.value == true && isLoadingTomorrowShows.value == false) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                upnextRepository.refreshTomorrowShows(
                    DEFAULT_COUNTRY_CODE,
                    DateUtils.tomorrowDate()
                )
            }
        }
    }

    fun onRefreshShowsClick() {
        requestShowsUpdate()
    }

    fun onDashboardItemClick(showDetailArg: ShowDetailArg) {
        _navigateToSelectedShow.value = showDetailArg
    }

    fun displayShowDetailsComplete() {
        _navigateToSelectedShow.value = null
    }

    fun showFeaturesBottomSheetComplete() {
        _showFeaturesBottomSheet.value = false
    }

    private fun requestShowsUpdate() {
        viewModelScope?.launch {
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
            upnextRepository.refreshNewShows()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    companion object {
        const val DEFAULT_COUNTRY_CODE = "US"
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}