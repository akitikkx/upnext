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

    val isLoadingTraktRecommendations = traktRepository.isLoadingTraktRecommendations

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val showFeaturesBottomSheet: LiveData<Boolean> = _showFeaturesBottomSheet

    val newShowsList = upnextRepository.newShows

    val yesterdayShowsList = upnextRepository.yesterdayShows

    val todayShowsList = upnextRepository.todayShows

    val tomorrowShowsList = upnextRepository.tomorrowShows

    val traktRecommendationsList = traktRepository.traktRecommendations

    val yesterdayShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_YESTERDAY_SHOWS.tableName)

    val todayShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_TODAY_SHOWS.tableName)

    val tomorrowShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_TOMORROW_SHOWS.tableName)

    val traktRecommendedShowsTableUpdate =
        upnextRepository.tableUpdate(DatabaseTables.TABLE_TOMORROW_SHOWS.tableName)

    private val traktRecommendationsShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktRecommendationsList) {
            value = it.isNullOrEmpty() == true
        }
    }

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
        addSource(isLoadingTraktRecommendations) {
            value = it
        }
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
            tableUpdate?.lastUpdated?.let { it -> DateUtils.dateDifference(it, "minutes") }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.DASHBOARD_ITEMS.intervalMins && (isLoadingYesterdayShows.value == false || isLoadingYesterdayShows.value == null)) {
                viewModelScope?.launch {
                    upnextRepository.refreshYesterdayShows(
                        DEFAULT_COUNTRY_CODE,
                        DateUtils.yesterdayDate()
                    )
                }
            }
        } else if (yesterdayShowsEmpty.value == true) {
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
            tableUpdate?.lastUpdated?.let { it -> DateUtils.dateDifference(it, "minutes") }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.DASHBOARD_ITEMS.intervalMins && (isLoadingTodayShows.value == false || isLoadingTodayShows.value == null)) {
                viewModelScope?.launch {
                    upnextRepository.refreshTodayShows(
                        DEFAULT_COUNTRY_CODE,
                        DateUtils.currentDate()
                    )
                }
            }
        } else if (todayShowsEmpty.value == true) {
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
            tableUpdate?.lastUpdated?.let { it -> DateUtils.dateDifference(it, "minutes") }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.DASHBOARD_ITEMS.intervalMins && (isLoadingTomorrowShows.value == false || isLoadingTomorrowShows.value == null)) {
                viewModelScope?.launch {
                    upnextRepository.refreshTomorrowShows(
                        DEFAULT_COUNTRY_CODE,
                        DateUtils.tomorrowDate()
                    )
                }
            }
        } else if (tomorrowShowsEmpty.value == true) {
            viewModelScope?.launch {
                upnextRepository.refreshTomorrowShows(
                    DEFAULT_COUNTRY_CODE,
                    DateUtils.tomorrowDate()
                )
            }
        }
    }

    fun onTraktRecommendationsShowsTableUpdateReceived(tableUpdate: TableUpdate?) {
        if (isAuthorizedOnTrakt.value == false) {
            return
        }

        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.dateDifference(it, "minutes") }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.RECOMMENDED_ITEMS.intervalMins && (isLoadingTraktRecommendations.value == false || isLoadingTraktRecommendations.value == null)) {
                viewModelScope?.launch {
                    traktRepository.refreshTraktRecommendations(
                        UpnextPreferenceManager(
                            getApplication()
                        ).getTraktAccessToken()
                    )
                }
            }
        } else if (traktRecommendationsShowsEmpty.value == true) {
            viewModelScope?.launch {
                traktRepository.refreshTraktRecommendations(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
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

            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.refreshTraktRecommendations(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
            }
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