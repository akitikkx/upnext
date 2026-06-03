/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.repository.fakes

import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeDashboardRepository : DashboardRepository {
    private val _isLoadingYesterdayShows = MutableStateFlow(false)
    override val isLoadingYesterdayShows: StateFlow<Boolean> = _isLoadingYesterdayShows.asStateFlow()

    private val _isLoadingTodayShows = MutableStateFlow(false)
    override val isLoadingTodayShows: StateFlow<Boolean> = _isLoadingTodayShows.asStateFlow()

    private val _isLoadingTomorrowShows = MutableStateFlow(false)
    override val isLoadingTomorrowShows: StateFlow<Boolean> = _isLoadingTomorrowShows.asStateFlow()

    private val _yesterdayShows = MutableStateFlow<List<ScheduleShow>>(emptyList())
    override val yesterdayShows: Flow<List<ScheduleShow>> = _yesterdayShows.asStateFlow()

    private val _todayShows = MutableStateFlow<List<ScheduleShow>>(emptyList())
    override val todayShows: Flow<List<ScheduleShow>> = _todayShows.asStateFlow()

    private val _tomorrowShows = MutableStateFlow<List<ScheduleShow>>(emptyList())
    override val tomorrowShows: Flow<List<ScheduleShow>> = _tomorrowShows.asStateFlow()

    override fun tableUpdate(tableName: String): Flow<TableUpdate?> {
        return flowOf(null)
    }

    override suspend fun refreshYesterdayShows(
        countryCode: String,
        date: String?,
    ) {
        _isLoadingYesterdayShows.value = true
        _isLoadingYesterdayShows.value = false
    }

    override suspend fun refreshTodayShows(
        countryCode: String,
        date: String?,
    ) {
        _isLoadingTodayShows.value = true
        _isLoadingTodayShows.value = false
    }

    override suspend fun refreshTomorrowShows(
        countryCode: String,
        date: String?,
    ) {
        _isLoadingTomorrowShows.value = true
        _isLoadingTomorrowShows.value = false
    }

    override suspend fun getShowImageAndTvmazeId(imdbId: String?): Pair<String?, Int?> {
        return Pair(null, null)
    }

    override suspend fun getEpisodeImageAndTvmazeId(
        imdbId: String?,
        season: Int,
        number: Int,
    ): Pair<String?, Int?> {
        return Pair(null, null)
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
