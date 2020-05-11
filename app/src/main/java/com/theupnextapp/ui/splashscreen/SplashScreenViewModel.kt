package com.theupnextapp.ui.splashscreen

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.theupnextapp.BuildConfig
import com.theupnextapp.database.getDatabase
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SplashScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)

    private val upnextRepository = UpnextRepository(database)

    private val _isFreshInstall = MutableLiveData<Boolean>()

    private val _isNormalInstall = MutableLiveData<Boolean>()

    private val _isUpgradedInstall = MutableLiveData<Boolean>()

    private val _showLoadingText = MutableLiveData<Boolean>()

    private val _navigateToDashboard = MutableLiveData<Boolean>()

    private val _loadingText = MutableLiveData<String>()

    val isLoadingRecommendedShows = upnextRepository.isLoadingRecommendedShows

    val isLoadingNewShows = upnextRepository.isLoadingNewShows

    val isLoadingYesterdayShows = upnextRepository.isLoadingYesterdayShows

    val isLoadingTodayShows = upnextRepository.isLoadingTodayShows

    val isLoadingTomorrowShows = upnextRepository.isLoadingTomorrowShows

    val isFreshInstall: LiveData<Boolean>
        get() = _isFreshInstall

    val isNormalInstall : LiveData<Boolean>
        get() = _isNormalInstall

    val isUpgradeInstall : LiveData<Boolean>
        get() = _isUpgradedInstall

    val showLoadingText : LiveData<Boolean>
        get() = _showLoadingText

    val navigateToDashboard : LiveData<Boolean>
        get() = _navigateToDashboard

    val loadingText : LiveData<String>
        get() = _loadingText

    init {
        checkIfFirstRun()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun updateShows() = requestShowsUpdate()

    fun displayLoadingText(loadingText : String) {
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
            upnextRepository.refreshRecommendedShows()
            upnextRepository.refreshNewShows()
            upnextRepository.refreshYesterdayShows(
                DashboardViewModel.DEFAULT_COUNTRY_CODE,
                yesterdayDate()
            )
            upnextRepository.refreshTodayShows(
                DashboardViewModel.DEFAULT_COUNTRY_CODE,
                currentDate()
            )
            upnextRepository.refreshTomorrowShows(
                DashboardViewModel.DEFAULT_COUNTRY_CODE,
                tomorrowDate()
            )
        }
    }

    private fun currentDate(): String? {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }

    private fun tomorrowDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(tomorrow)
    }

    private fun yesterdayDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(yesterday)
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