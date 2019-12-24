package com.theupnextapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.database.getDatabase
import com.theupnextapp.repository.UpnextRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)

    private val upnextRepository = UpnextRepository(database)

    private val _navigateToSelectedShow = MutableLiveData<Int>()

    val navigateToSelectedShow: LiveData<Int>
        get() = _navigateToSelectedShow

    init {
        viewModelScope.launch {
            upnextRepository.refreshRecommendedShows()
            upnextRepository.refreshNewShows()
            upnextRepository.refreshYesterdayShows(DEFAULT_COUNTRY_CODE, yesterdayDate())
            upnextRepository.refreshTodayShows(DEFAULT_COUNTRY_CODE, currentDate())
            upnextRepository.refreshTomorrowShows(DEFAULT_COUNTRY_CODE, tomorrowDate())
        }
    }

    val recommendedShowsList = upnextRepository.recommendedShows

    val newShowsList = upnextRepository.newShows

    val yesterdayShowsList = upnextRepository.yesterdayShows

    val todayShowsList = upnextRepository.todayShows

    val tomorrowShowsList = upnextRepository.tomorrowShows

    fun displayShowDetails(showId: Int) {
        _navigateToSelectedShow.value = showId
    }

    fun displayShowDetailsComplete() {
        _navigateToSelectedShow.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun currentDate(): String? {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(calendar.time)
    }

    private fun tomorrowDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(tomorrow)
    }

    private fun yesterdayDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(yesterday)
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