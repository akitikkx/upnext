package com.theupnextapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.repository.UpnextRepository
import kotlinx.coroutines.SupervisorJob

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val database = getDatabase(application)

    private val upnextRepository = UpnextRepository(database)

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    private val _showProgressLoader = MutableLiveData<Boolean>()

    val navigateToSelectedShow: LiveData<ShowDetailArg>
        get() = _navigateToSelectedShow

    val showProgressLoader: LiveData<Boolean>
        get() = _showProgressLoader

    val recommendedShowsList = upnextRepository.recommendedShows

    val newShowsList = upnextRepository.newShows

    val yesterdayShowsList = upnextRepository.yesterdayShows

    val todayShowsList = upnextRepository.todayShows

    val tomorrowShowsList = upnextRepository.tomorrowShows

    val isLoading = upnextRepository.isLoading

    fun displayShowDetails(showDetailArg: ShowDetailArg) {
        _navigateToSelectedShow.value = showDetailArg
    }

    fun displayShowDetailsComplete() {
        _navigateToSelectedShow.value = null
    }

    fun displayProgressLoader() {
        _showProgressLoader.value = true
    }

    fun displayProgressLoaderComplete() {
        _showProgressLoader.value = false
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