package com.theupnextapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    init {
        viewModelScope.launch {
            upnextRepository.refreshRecommendedShows()
            upnextRepository.refreshNewShows()
            upnextRepository.refreshYesterdayShows(DEFAULT_COUNTRY_CODE, yesterdayDate())
        }
    }

    val recommendedShowsList = upnextRepository.recommendedShows

    val newShowsList = upnextRepository.newShows

    val yesterdayShowsList = upnextRepository.yesterdayShows

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

    class Factory(val app : Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}