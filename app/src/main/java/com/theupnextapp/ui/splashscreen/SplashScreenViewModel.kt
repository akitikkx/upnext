package com.theupnextapp.ui.splashscreen

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.theupnextapp.BuildConfig
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.ui.common.TraktViewModel
import com.theupnextapp.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.launch

class SplashScreenViewModel(application: Application) : TraktViewModel(application) {

    private val upnextRepository = UpnextRepository(database)

    private val _isFreshInstall = MutableLiveData<Boolean>()

    private val _isNormalInstall = MutableLiveData<Boolean>()

    private val _isUpgradedInstall = MutableLiveData<Boolean>()

    private val _showLoadingText = MutableLiveData<Boolean>()

    private val _navigateToDashboard = MutableLiveData<Boolean>()

    private val _loadingText = MutableLiveData<String>()

    val isLoadingNewShows = upnextRepository.isLoadingNewShows

    val isLoadingYesterdayShows = upnextRepository.isLoadingYesterdayShows

    val isLoadingTodayShows = upnextRepository.isLoadingTodayShows

    val isLoadingTomorrowShows = upnextRepository.isLoadingTomorrowShows

    val isLoadingTraktRecommendations = traktRepository.isLoadingTraktRecommendations

    val isFreshInstall: LiveData<Boolean> = _isFreshInstall

    val isNormalInstall: LiveData<Boolean> = _isNormalInstall

    val isUpgradeInstall: LiveData<Boolean> = _isUpgradedInstall

    val showLoadingText: LiveData<Boolean> = _showLoadingText

    val navigateToDashboard: LiveData<Boolean> = _navigateToDashboard

    val loadingText: LiveData<String>
        get() = _loadingText

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
            upnextRepository.refreshNewShows()
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
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.refreshTraktRecommendations(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
            }
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

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SplashScreenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SplashScreenViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}