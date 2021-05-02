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
        DatabaseTraktCollection::class,
        DatabaseTraktCollectionSeason::class,
        DatabaseTraktCollectionEpisode::class,
        DatabaseTableUpdate::class,
        DatabaseTraktRecommendations::class,
        DatabaseTraktPopularShows::class,
        DatabaseTraktTrendingShows::class,
        DatabaseTraktMostAnticipated::class
    ],
    version = 19,
    exportSchema = true
)
abstract class UpnextDatabase : RoomDatabase() {
    abstract val upnextDao: UpnextDao
}