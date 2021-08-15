package com.theupnextapp.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DatabaseYesterdaySchedule::class,
        DatabaseTodaySchedule::class,
        DatabaseTomorrowSchedule::class,
        DatabaseShowInfo::class,
        DatabaseTableUpdate::class,
        DatabaseTraktPopularShows::class,
        DatabaseTraktTrendingShows::class,
        DatabaseTraktMostAnticipated::class,
        DatabaseFavoriteShows::class,
        DatabaseTraktAccess::class,
        DatabaseFavoriteNextEpisode::class
    ],
    version = 25,
    exportSchema = true
)
abstract class UpnextDatabase : RoomDatabase() {
    abstract val upnextDao: UpnextDao
}