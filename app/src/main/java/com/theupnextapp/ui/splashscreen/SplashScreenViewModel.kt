package com.theupnextapp.ui.splashscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.repository.datastore.UpnextDataStoreManager
import com.theupnextapp.ui.dashboard.DashboardViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val upnextDataStoreManager: UpnextDataStoreManager,
    private val upnextRepository: UpnextRepository
) : ViewModel() {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _isFreshInstall = MutableLiveData<Boolean>()
    val isFreshInstall: LiveData<Boolean> = _isFreshInstall

    private val _isNormalInstall = MutableLiveData<Boolean>()
    val isNormalInstall: LiveData<Boolean> = _isNormalInstall

    private val _isUpgradedInstall = MutableLiveData<Boolean>()
    val isUpgradeInstall: LiveData<Boolean> = _isUpgradedInstall

    private val _showLoadingText = MutableLiveData<Boolean?>()
    val showLoadingText: LiveData<Boolean?> = _showLoadingText

    private val _navigateToDashboard = MutableLiveData<Boolean?>()
    val navigateToDashboard: LiveData<Boolean?> = _navigateToDashboard

    private val _loadingText = MutableLiveData<String>()
    val loadingText: LiveData<String> = _loadingText

    val isLoadingYesterdayShows = upnextRepository.isLoadingYesterdayShows

    val isLoadingTodayShows = upnextRepository.isLoadingTodayShows

    val isLoadingTomorrowShows = upnextRepository.isLoadingTomorrowShows

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun updateShows() = requestShowsUpdate()

    fun displayLoadingText(loadingText: String) {
        _showLoadingText.value = true
        _loadingText.value = loadingText
    }

    fun displayLoadingTextComplete() {
        _showLoadingText.value = null
    }

    fun showDashboard() {
        _navigateToDashboard.value = true
    }

    fun showDashboardComplete() {
        _navigateToDashboard.value = null
    }

    private fun requestShowsUpdate() {
        viewModelScope.launch {
            upnextRepository.refreshYesterdayShows(
                DashboardViewModel.DEFAULT_COUNTRY_CODE,
                DateUtils.yesterdayDate()
            )
            upnextRepository.refreshTodayShows(
                DashboardViewModel.DEFAULT_COUNTRY_CODE,
                DateUtils.currentDate()
            )
            upnextRepository.refreshTomorrowShows(
                DashboardViewModel.DEFAULT_COUNTRY_CODE,
                DateUtils.tomorrowDate()
            )
        }
    }

}