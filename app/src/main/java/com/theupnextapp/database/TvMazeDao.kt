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