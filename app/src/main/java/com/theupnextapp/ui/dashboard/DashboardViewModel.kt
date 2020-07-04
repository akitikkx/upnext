package com.theupnextapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.repository.UpnextRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)

    private val upnextRepository = UpnextRepository(database)

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    private val _showFeaturesBottomSheet = MutableLiveData<Boolean>()

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val recommendedShowsList = upnextRepository.recommendedShows

    val newShowsList = upnextRepository.newShows

    val yesterdayShowsList = upnextRepository.yesterdayShows

    val todayShowsList = upnextRepository.todayShows

    val tomorrowShowsList = upnextRepository.tomorrowShows

    val isLoadingRecommendedShows = upnextRepository.isLoadingRecommendedShows

    val isLoadingNewShows = upnextRepository.isLoadingNewShows

    val isLoadingYesterdayShows = upnextRepository.isLoadingYesterdayShows

    val isLoadingTodayShows = upnextRepository.isLoadingTodayShows

    val isLoadingTomorrowShows = upnextRepository.isLoadingTomorrowShows

    val showFeaturesBottomSheet: LiveData<Boolean> = _showFeaturesBottomSheet

    fun onRecommendedShowsListEmpty() {
        viewModelScope.launch {
            upnextRepository.refreshRecommendedShows()
        }
    }

    fun onNewShowsListEmpty() {
        viewModelScope.launch {
            upnextRepository.refreshNewShows()
        }
    }

    fun onYesterdayShowsListEmpty() {
        viewModelScope.launch {
            upnextRepository.refreshYesterdayShows(
                DEFAULT_COUNTRY_CODE,
                DateUtils.yesterdayDate()
            )
        }
    }

    fun onTodayShowsListEmpty() {
        viewModelScope.launch {
            upnextRepository.refreshTodayShows(
                DEFAULT_COUNTRY_CODE,
                DateUtils.currentDate()
            )
        }
    }

    fun onTomorrowShowsListEmpty() {
        viewModelScope.launch {
            upnextRepository.refreshTomorrowShows(
                DEFAULT_COUNTRY_CODE,
                DateUtils.tomorrowDate()
            )
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
            upnextRepository.refreshNewShows()
            upnextRepository.refreshRecommendedShows()
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