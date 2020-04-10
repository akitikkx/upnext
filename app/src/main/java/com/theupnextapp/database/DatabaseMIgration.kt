package com.theupnextapp.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `trakt_watchlist` (`id` INTEGER, `listed_at` TEXT, `rank` INTEGER, `title` TEXT, `imdbID` TEXT, `slug` TEXT, `tmdbID` INTEGER, `traktID` INTEGER, `tvdbID` INTEGER, `tvrageID` INTEGER, `tvMazeID` INTEGER, PRIMARY KEY(`id`))")
    }
}