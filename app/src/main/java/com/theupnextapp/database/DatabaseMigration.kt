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

package com.theupnextapp.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_watchlist (id INTEGER, listed_at TEXT, rank INTEGER, title TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvrageID INTEGER, tvMazeID INTEGER, PRIMARY KEY(id))")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_watchlist (id INTEGER NOT NULL, listed_at TEXT, rank INTEGER, title TEXT, mediumImageUrl TEXT, originalImageUrl TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvrageID INTEGER, tvMazeID INTEGER, PRIMARY KEY(id))")
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_watchlist (id INTEGER NOT NULL, listed_at TEXT, rank INTEGER, title TEXT, mediumImageUrl TEXT, originalImageUrl TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvrageID INTEGER, tvMazeID INTEGER, PRIMARY KEY(id))")
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

val MIGRATION_16_17: Migration = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_most_anticipated(id INTEGER, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, list_count INTEGER, PRIMARY KEY(id))")
    }
}

val MIGRATION_17_18: Migration = object : Migration(17, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_most_anticipated (id INTEGER, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, list_count INTEGER, PRIMARY KEY(id))")
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_trending (id INTEGER NOT NULL, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, PRIMARY KEY(id))")
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_popular (id INTEGER NOT NULL, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, PRIMARY KEY(id))")
    }
}

val MIGRATION_18_19: Migration = object : Migration(18, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS new_shows")
    }
}

val MIGRATION_19_20: Migration = object : Migration(19, 20) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS trakt_collection")
        database.execSQL("DROP TABLE IF EXISTS trakt_collection_seasons")
        database.execSQL("DROP TABLE IF EXISTS trakt_collection_episodes")
    }
}

val MIGRATION_20_21: Migration = object : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS trakt_watchlist")
        database.execSQL("DROP TABLE IF EXISTS trakt_history")
        database.execSQL("DROP TABLE IF EXISTS trakt_recommendations")
    }
}

val MIGRATION_21_22: Migration = object : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE schedule_yesterday ADD COLUMN mediumImage TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE schedule_today ADD COLUMN mediumImage TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE schedule_tomorrow ADD COLUMN mediumImage TEXT DEFAULT NULL")
    }
}

val MIGRATION_22_23: Migration = object : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS favorite_shows (id INTEGER, title TEXT, year TEXT, mediumImageUrl TEXT, originalImageUrl TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, PRIMARY KEY(id))")
    }
}

val MIGRATION_23_24: Migration = object : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS trakt_access (id INTEGER NOT NULL, access_token TEXT, created_at INTEGER, expires_in INTEGER, refresh_token TEXT, scope TEXT, token_type TEXT, PRIMARY KEY(id))")
    }
}

val MIGRATION_24_25: Migration = object : Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS favorite_next_episodes (tvMazeID INTEGER, number INTEGER, season INTEGER, title TEXT, airStamp TEXT, mediumImageUrl TEXT, originalImageUrl TEXT, imdb TEXT, PRIMARY KEY(tvMazeID))")
    }
}

val MIGRATION_25_26: Migration = object : Migration(25, 26) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE favorite_shows ADD COLUMN airStamp TEXT DEFAULT NULL")
    }
}
