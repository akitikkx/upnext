package com.theupnextapp.repository.fakes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeDashboardRepository : DashboardRepository {

    private val _isLoadingYesterdayShows = MutableLiveData(false)
    override val isLoadingYesterdayShows: LiveData<Boolean> = _isLoadingYesterdayShows

    private val _isLoadingTodayShows = MutableLiveData(false)
    override val isLoadingTodayShows: LiveData<Boolean> = _isLoadingTodayShows

    private val _isLoadingTomorrowShows = MutableLiveData(false)
    override val isLoadingTomorrowShows: LiveData<Boolean> = _isLoadingTomorrowShows

    private val _yesterdayShows = MutableStateFlow<List<ScheduleShow>>(emptyList())
    override val yesterdayShows: Flow<List<ScheduleShow>> = _yesterdayShows.asStateFlow()

    private val _todayShows = MutableStateFlow<List<ScheduleShow>>(emptyList())
    override val todayShows: Flow<List<ScheduleShow>> = _todayShows.asStateFlow()

    private val _tomorrowShows = MutableStateFlow<List<ScheduleShow>>(emptyList())
    override val tomorrowShows: Flow<List<ScheduleShow>> = _tomorrowShows.asStateFlow()

    override fun tableUpdate(tableName: String): Flow<TableUpdate?> {
        return flowOf(null)
    }

    override suspend fun refreshYesterdayShows(countryCode: String, date: String?) {
        _isLoadingYesterdayShows.postValue(true)
        // Simulate network/db work
        _isLoadingYesterdayShows.postValue(false)
    }

    override suspend fun refreshTodayShows(countryCode: String, date: String?) {
        _isLoadingTodayShows.postValue(true)
        _isLoadingTodayShows.postValue(false)
    }

    override suspend fun refreshTomorrowShows(countryCode: String, date: String?) {
        _isLoadingTomorrowShows.postValue(true)
        _isLoadingTomorrowShows.postValue(false)
    }

    // Helper methods for testing
    fun setLoadingYesterday(isLoading: Boolean) {
        _isLoadingYesterdayShows.value = isLoading
    }

    fun setLoadingToday(isLoading: Boolean) {
        _isLoadingTodayShows.value = isLoading
    }

    fun setLoadingTomorrow(isLoading: Boolean) {
        _isLoadingTomorrowShows.value = isLoading
    }

    fun setYesterdayShows(shows: List<ScheduleShow>) {
        _yesterdayShows.value = shows
    }

    fun setTodayShows(shows: List<ScheduleShow>) {
        _todayShows.value = shows
    }

    fun setTomorrowShows(shows: List<ScheduleShow>) {
        _tomorrowShows.value = shows
    }
}
