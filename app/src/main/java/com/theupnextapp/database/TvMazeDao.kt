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
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.theupnextapp.domain.ShowInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface TvMazeDao {
    @Query("select * from schedule_yesterday")
    fun getYesterdayShows(): Flow<List<DatabaseYesterdaySchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllYesterdayShows(vararg yesterdayShows: DatabaseYesterdaySchedule)

    @Query("delete from schedule_yesterday")
    fun deleteAllYesterdayShows()

    @Query("select * from schedule_today")
    fun getTodayShows(): Flow<List<DatabaseTodaySchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTodayShows(vararg todayShows: DatabaseTodaySchedule)

    @Query("delete from schedule_today")
    fun deleteAllTodayShows()

    @Query("select * from schedule_tomorrow")
    fun getTomorrowShows(): Flow<List<DatabaseTomorrowSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTomorrowShows(vararg tomorrowShows: DatabaseTomorrowSchedule)

    @Query("delete from schedule_tomorrow")
    fun deleteAllTomorrowShows()

    @Query("delete from shows_info where id = :id")
    fun deleteAllShowInfo(id: Int)

    @Query("select * from shows_info where id = :id")
    fun getShowWithId(id: Int): ShowInfo
}
