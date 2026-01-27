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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_14_15: Migration =
    object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS trakt_popular(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER)",
            )
        }
    }

val MIGRATION_15_16: Migration =
    object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS trakt_trending(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER)",
            )
        }
    }

val MIGRATION_16_17: Migration =
    object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS trakt_most_anticipated(id INTEGER, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, list_count INTEGER, PRIMARY KEY(id))",
            )
        }
    }

val MIGRATION_17_18: Migration =
    object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS trakt_most_anticipated (id INTEGER, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, list_count INTEGER, PRIMARY KEY(id))",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS trakt_trending (id INTEGER NOT NULL, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, PRIMARY KEY(id))",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS trakt_popular (id INTEGER NOT NULL, title TEXT, year TEXT, medium_image_url TEXT, original_image_url TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, PRIMARY KEY(id))",
            )
        }
    }

val MIGRATION_18_19: Migration =
    object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS new_shows")
        }
    }

val MIGRATION_19_20: Migration =
    object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS trakt_collection")
            db.execSQL("DROP TABLE IF EXISTS trakt_collection_seasons")
            db.execSQL("DROP TABLE IF EXISTS trakt_collection_episodes")
        }
    }

val MIGRATION_20_21: Migration =
    object : Migration(20, 21) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS trakt_watchlist")
            db.execSQL("DROP TABLE IF EXISTS trakt_history")
            db.execSQL("DROP TABLE IF EXISTS trakt_recommendations")
        }
    }

val MIGRATION_21_22: Migration =
    object : Migration(21, 22) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE schedule_yesterday ADD COLUMN mediumImage TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE schedule_today ADD COLUMN mediumImage TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE schedule_tomorrow ADD COLUMN mediumImage TEXT DEFAULT NULL")
        }
    }

val MIGRATION_22_23: Migration =
    object : Migration(22, 23) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS favorite_shows (id INTEGER, title TEXT, year TEXT, mediumImageUrl TEXT, originalImageUrl TEXT, imdbID TEXT, slug TEXT, tmdbID INTEGER, traktID INTEGER, tvdbID INTEGER, tvMazeID INTEGER, PRIMARY KEY(id))",
            )
        }
    }

val MIGRATION_23_24: Migration =
    object : Migration(23, 24) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS trakt_access (id INTEGER NOT NULL, access_token TEXT, created_at INTEGER, expires_in INTEGER, refresh_token TEXT, scope TEXT, token_type TEXT, PRIMARY KEY(id))",
            )
        }
    }

val MIGRATION_24_25: Migration =
    object : Migration(24, 25) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS favorite_next_episodes (tvMazeID INTEGER, number INTEGER, season INTEGER, title TEXT, airStamp TEXT, mediumImageUrl TEXT, originalImageUrl TEXT, imdb TEXT, PRIMARY KEY(tvMazeID))",
            )
        }
    }

val MIGRATION_25_26: Migration =
    object : Migration(25, 26) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE favorite_shows ADD COLUMN airStamp TEXT DEFAULT NULL")
        }
    }

val MIGRATION_26_27 =
    object : Migration(26, 27) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS favorite_next_episodes")
            db.execSQL(
                """
                CREATE TABLE favorite_shows_new (
                    id INTEGER, 
                    title TEXT, 
                    year TEXT, 
                    mediumImageUrl TEXT, 
                    originalImageUrl TEXT, 
                    imdbID TEXT, 
                    slug TEXT, 
                    tmdbID INTEGER, 
                    traktID INTEGER, 
                    tvdbID INTEGER, 
                    tvMazeID INTEGER, 
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )

            // 2. Copy data from the old table to the new table
            // Make sure to list all columns EXCEPT airStamp
            db.execSQL(
                """
                INSERT INTO favorite_shows_new (id, title, year, mediumImageUrl, originalImageUrl, imdbID, slug, tmdbID, traktID, tvdbID, tvMazeID)
                SELECT id, title, year, mediumImageUrl, originalImageUrl, imdbID, slug, tmdbID, traktID, tvdbID, tvMazeID 
                FROM favorite_shows
                """.trimIndent(),
            )

            // 3. Drop the old table
            db.execSQL("DROP TABLE favorite_shows")

            // 4. Rename the new table to the original table name
            db.execSQL("ALTER TABLE favorite_shows_new RENAME TO favorite_shows")
        }
    }

val MIGRATION_27_28: Migration =
    object : Migration(27, 28) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            CREATE TABLE trakt_most_anticipated_new (
                id INTEGER PRIMARY KEY, 
                title TEXT, 
                year TEXT, 
                medium_image_url TEXT, 
                original_image_url TEXT, 
                imdbID TEXT, 
                slug TEXT, 
                tmdbID INTEGER, 
                traktID INTEGER,
                tvdbID INTEGER, 
                tvMazeID INTEGER
            )
        """,
            )
            db.execSQL(
                """
            INSERT INTO trakt_most_anticipated_new (
                id, title, year, medium_image_url, original_image_url, imdbID, slug, tmdbID, traktID, tvdbID, tvMazeID
            )
            SELECT 
                id, title, year, medium_image_url, original_image_url, imdbID, slug, tmdbID, traktID, tvdbID, tvMazeID
            FROM trakt_most_anticipated
        """,
            )
            db.execSQL("DROP TABLE trakt_most_anticipated")

            db.execSQL("ALTER TABLE trakt_most_anticipated_new RENAME TO trakt_most_anticipated")
        }
    }

val MIGRATION_28_29 =
    object : Migration(28, 29) { // Replace Y and Y+1 accordingly
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Create the new table with id as NON NULL
            db.execSQL(
                """
            CREATE TABLE trakt_most_anticipated_new (
                id INTEGER NOT NULL PRIMARY KEY, 
                title TEXT, 
                year TEXT, 
                medium_image_url TEXT, 
                original_image_url TEXT, 
                imdbID TEXT, 
                slug TEXT, 
                tmdbID INTEGER, 
                traktID INTEGER,
                tvdbID INTEGER, 
                tvMazeID INTEGER
            )
        """,
            )

            db.execSQL(
                """
            INSERT INTO trakt_most_anticipated_new (
                id, title, year, medium_image_url, original_image_url, imdbID, slug, tmdbID, traktID, tvdbID, tvMazeID 
            )
            SELECT 
                id, title, year, medium_image_url, original_image_url, imdbID, slug, tmdbID, traktID, tvdbID, tvMazeID
            FROM trakt_most_anticipated 
            WHERE id IS NOT NULL 
        """,
            )

            // 3. Drop the old table
            db.execSQL("DROP TABLE trakt_most_anticipated")

            // 4. Rename the new table to the original name
            db.execSQL("ALTER TABLE trakt_most_anticipated_new RENAME TO trakt_most_anticipated")
        }
    }
