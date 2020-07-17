package com.theupnextapp.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `trakt_watchlist` (`id` INTEGER, `listed_at` TEXT, `rank` INTEGER, `title` TEXT, `imdbID` TEXT, `slug` TEXT, `tmdbID` INTEGER, `traktID` INTEGER, `tvdbID` INTEGER, `tvrageID` INTEGER, `tvMazeID` INTEGER, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `trakt_watchlist` (`id` INTEGER NOT NULL, `listed_at` TEXT, `rank` INTEGER, `title` TEXT, `mediumImageUrl` TEXT, `originalImageUrl` TEXT, `imdbID` TEXT, `slug` TEXT, `tmdbID` INTEGER, `traktID` INTEGER, `tvdbID` INTEGER, `tvrageID` INTEGER, `tvMazeID` INTEGER, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `trakt_watchlist` (`id` INTEGER NOT NULL, `listed_at` TEXT, `rank` INTEGER, `title` TEXT, `mediumImageUrl` TEXT, `originalImageUrl` TEXT, `imdbID` TEXT, `slug` TEXT, `tmdbID` INTEGER, `traktID` INTEGER, `tvdbID` INTEGER, `tvrageID` INTEGER, `tvMazeID` INTEGER, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_14_15: Migration = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_popular(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER)")
    }
}

val MIGRATION_15_16: Migration = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_trending(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER)")
    }
}