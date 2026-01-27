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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.repository.fakes

import com.theupnextapp.database.DatabaseTodaySchedule
import com.theupnextapp.database.DatabaseTomorrowSchedule
import com.theupnextapp.database.DatabaseYesterdaySchedule
import com.theupnextapp.database.TvMazeDao
import com.theupnextapp.domain.ShowInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTvMazeDao : TvMazeDao {
    private val yesterdayShowsFlow = MutableStateFlow<List<DatabaseYesterdaySchedule>>(emptyList())
    val yesterdayShowsList = mutableListOf<DatabaseYesterdaySchedule>()

    private val todayShowsFlow = MutableStateFlow<List<DatabaseTodaySchedule>>(emptyList())
    val todayShowsList = mutableListOf<DatabaseTodaySchedule>()

    private val tomorrowShowsFlow = MutableStateFlow<List<DatabaseTomorrowSchedule>>(emptyList())
    val tomorrowShowsList = mutableListOf<DatabaseTomorrowSchedule>()

    private val showInfoMap = mutableMapOf<Int, ShowInfo>()

    override fun getYesterdayShows(): Flow<List<DatabaseYesterdaySchedule>> = yesterdayShowsFlow.asStateFlow()

    override fun insertAllYesterdayShows(vararg yesterdayShows: DatabaseYesterdaySchedule) {
        yesterdayShowsList.clear()
        yesterdayShowsList.addAll(yesterdayShows)
        yesterdayShowsFlow.value = yesterdayShowsList.toList()
    }

    override fun deleteAllYesterdayShows() {
        yesterdayShowsList.clear()
        yesterdayShowsFlow.value = emptyList()
    }

    override fun getTodayShows(): Flow<List<DatabaseTodaySchedule>> = todayShowsFlow.asStateFlow()

    override fun insertAllTodayShows(vararg todayShows: DatabaseTodaySchedule) {
        todayShowsList.clear()
        todayShowsList.addAll(todayShows)
        todayShowsFlow.value = todayShowsList.toList()
    }

    override fun deleteAllTodayShows() {
        todayShowsList.clear()
        todayShowsFlow.value = emptyList()
    }

    override fun getTomorrowShows(): Flow<List<DatabaseTomorrowSchedule>> = tomorrowShowsFlow.asStateFlow()

    override fun insertAllTomorrowShows(vararg tomorrowShows: DatabaseTomorrowSchedule) {
        tomorrowShowsList.clear()
        tomorrowShowsList.addAll(tomorrowShows)
        tomorrowShowsFlow.value = tomorrowShowsList.toList()
    }

    override fun deleteAllTomorrowShows() {
        tomorrowShowsList.clear()
        tomorrowShowsFlow.value = emptyList()
    }

    override fun deleteAllShowInfo(id: Int) {
        showInfoMap.remove(id)
    }

    override fun getShowWithId(id: Int): ShowInfo {
        return showInfoMap[id] ?: throw NoSuchElementException("Show with id $id not found in FakeTvMazeDao")
    }

    fun clearAllData() {
        deleteAllYesterdayShows()
        deleteAllTodayShows()
        deleteAllTomorrowShows()
        showInfoMap.clear()
    }
}
