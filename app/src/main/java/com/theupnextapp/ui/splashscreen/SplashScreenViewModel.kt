package com.theupnextapp.ui.splashscreen

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.theupnextapp.BuildConfig
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.ui.common.TraktViewModel
import com.theupnextapp.ui.dashboard.DashboardViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    application: Application,
    traktRepository: TraktRepository,
    private val upnextRepository: UpnextRepository
) : TraktViewModel(application, traktRepository) {

    private val _isFreshInstall = MutableLiveData<Boolean>()

    private val _isNormalInstall = MutableLiveData<Boolean>()

    private val _isUpgradedInstall = MutableLiveData<Boolean>()

    private val _showLoadingText = MutableLiveData<Boolean?>()
    val showLoadingText: LiveData<Boolean?> = _showLoadingText

    private val _navigateToDashboard = MutableLiveData<Boolean?>()
    val navigateToDashboard: LiveData<Boolean?> = _navigateToDashboard

    private val _loadingText = MutableLiveData<String>()
    val loadingText: LiveData<String> = _loadingText

    val isLoadingNewShows = upnextRepository.isLoadingNewShows

    val isLoadingYesterdayShows = upnextRepository.isLoadingYesterdayShows

    val isLoadingTodayShows = upnextRepository.isLoadingTodayShows

    val isLoadingTomorrowShows = upnextRepository.isLoadingTomorrowShows

    val isLoadingTraktRecommendations = traktRepository.isLoadingTraktRecommendations

    val isFreshInstall: LiveData<Boolean> = _isFreshInstall

    val isNormalInstall: LiveData<Boolean> = _isNormalInstall

    val isUpgradeInstall: LiveData<Boolean> = _isUpgradedInstall


    init {
        checkIfFirstRun()
    }

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
        viewModelScope?.launch {
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

    private fun checkIfFirstRun() {
        val currentAppVersionCode = BuildConfig.VERSION_CODE

        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        val savedVersionCode =
            preferences.getInt(SHARED_PREF_VERSION_CODE_KEY, SHARED_PREF_NOT_FOUND)

        when {
            currentAppVersionCode == savedVersionCode -> {
                _isNormalInstall.value = true
            }
            savedVersionCode == SHARED_PREF_NOT_FOUND -> {
                _isFreshInstall.value = true
            }
            currentAppVersionCode > savedVersionCode -> {
                _isUpgradedInstall.value = true
            }
        }

        preferences.edit().putInt(SHARED_PREF_VERSION_CODE_KEY, currentAppVersionCode).apply()
    }

    companion object {
        const val SHARED_PREF_VERSION_CODE_KEY = "version_code"
        const val SHARED_PREF_NOT_FOUND = -1
    }
}