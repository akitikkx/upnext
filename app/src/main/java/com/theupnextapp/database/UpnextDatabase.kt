package com.theupnextapp.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DatabaseYesterdaySchedule::class,
        DatabaseTodaySchedule::class,
        DatabaseTomorrowSchedule::class,
        DatabaseShowInfo::class,
        DatabaseTraktWatchlist::class,
        DatabaseTraktHistory::class,
        DatabaseTableUpdate::class,
        DatabaseTraktRecommendations::class,
        DatabaseTraktPopularShows::class,
        DatabaseTraktTrendingShows::class,
        DatabaseTraktMostAnticipated::class
    ],
    version = 20,
    exportSchema = true
)
abstract class UpnextDatabase : RoomDatabase() {
    abstract val upnextDao: UpnextDao
}