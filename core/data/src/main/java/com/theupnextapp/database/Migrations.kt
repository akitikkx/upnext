package com.theupnextapp.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_29_30 =
    object : Migration(
        29,
        30,
    ) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create the new table with the correct schema for schedule_yesterday
            db.execSQL(
                """
                CREATE TABLE `schedule_yesterday_new` (
                  `id` INTEGER NOT NULL, 
                  `showId` INTEGER, 
                  `image` TEXT, 
                  `mediumImage` TEXT, 
                  `language` TEXT, 
                  `name` TEXT, 
                  `officialSite` TEXT, 
                  `premiered` TEXT, 
                  `runtime` TEXT, 
                  `status` TEXT, 
                  `summary` TEXT, 
                  `type` TEXT, 
                  `updated` TEXT, 
                  `url` TEXT, 
                  PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            // Copy the data from the old table to the new table for schedule_yesterday
            db.execSQL(
                """
                INSERT INTO `schedule_yesterday_new` (
                  `id`, 
                  `image`, 
                  `mediumImage`, 
                  `language`, 
                  `name`, 
                  `officialSite`, 
                  `premiered`, 
                  `runtime`, 
                  `status`, 
                  `summary`, 
                  `type`, 
                  `updated`, 
                  `url`
                ) SELECT 
                  `id`, 
                  `image`, 
                  `mediumImage`, 
                  `language`, 
                  `name`, 
                  `officialSite`, 
                  `premiered`, 
                  `runtime`, 
                  `status`, 
                  `summary`, 
                  `type`, 
                  `updated`, 
                  `url`
                 FROM `schedule_yesterday`
                """.trimIndent(),
            )

            // Remove the old table for schedule_yesterday
            db.execSQL("DROP TABLE `schedule_yesterday`")

            // Rename the new table to the original table name for schedule_yesterday
            db.execSQL("ALTER TABLE `schedule_yesterday_new` RENAME TO `schedule_yesterday`")

            // Schedule Today
            // Create the new table with the correct schema for schedule_today
            db.execSQL(
                """
                CREATE TABLE `schedule_today_new` (
                  `id` INTEGER NOT NULL, 
                  `showId` INTEGER, 
                  `image` TEXT, 
                  `mediumImage` TEXT, 
                  `language` TEXT, 
                  `name` TEXT, 
                  `officialSite` TEXT, 
                  `premiered` TEXT, 
                  `runtime` TEXT, 
                  `status` TEXT, 
                  `summary` TEXT, 
                  `type` TEXT, 
                  `updated` TEXT, 
                  `url` TEXT, 
                  PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            // Copy the data from the old table to the new table for schedule_today
            db.execSQL(
                """
                INSERT INTO `schedule_today_new` (
                  `id`, 
                  `image`, 
                  `mediumImage`, 
                  `language`, 
                  `name`, 
                  `officialSite`, 
                  `premiered`, 
                  `runtime`, 
                  `status`, 
                  `summary`, 
                  `type`, 
                  `updated`, 
                  `url`
                ) SELECT 
                  `id`, 
                  `image`, 
                  `mediumImage`, 
                  `language`, 
                  `name`, 
                  `officialSite`, 
                  `premiered`, 
                  `runtime`, 
                  `status`, 
                  `summary`, 
                  `type`, 
                  `updated`, 
                  `url`
                 FROM `schedule_today`
                """.trimIndent(),
            )
            // Remove the old table for schedule_today
            db.execSQL("DROP TABLE `schedule_today`")
            // Rename the new table to the original table name for schedule_today
            db.execSQL("ALTER TABLE `schedule_today_new` RENAME TO `schedule_today`")

            // Schedule Tomorrow
            // Create the new table with the correct schema for schedule_tomorrow
            db.execSQL(
                """
                CREATE TABLE `schedule_tomorrow_new` (
                  `id` INTEGER NOT NULL, 
                  `showId` INTEGER, 
                  `image` TEXT, 
                  `mediumImage` TEXT, 
                  `language` TEXT, 
                  `name` TEXT, 
                  `officialSite` TEXT, 
                  `premiered` TEXT, 
                  `runtime` TEXT, 
                  `status` TEXT, 
                  `summary` TEXT, 
                  `type` TEXT, 
                  `updated` TEXT, 
                  `url` TEXT, 
                  PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            // Copy the data from the old table to the new table for schedule_tomorrow
            db.execSQL(
                """
                INSERT INTO `schedule_tomorrow_new` (
                  `id`, 
                  `image`, 
                  `mediumImage`, 
                  `language`, 
                  `name`, 
                  `officialSite`, 
                  `premiered`, 
                  `runtime`, 
                  `status`, 
                  `summary`, 
                  `type`, 
                  `updated`, 
                  `url`
                ) SELECT 
                  `id`, 
                  `image`, 
                  `mediumImage`, 
                  `language`, 
                  `name`, 
                  `officialSite`, 
                  `premiered`, 
                  `runtime`, 
                  `status`, 
                  `summary`, 
                  `type`, 
                  `updated`, 
                  `url`
                 FROM `schedule_tomorrow`
                """.trimIndent(),
            )
            // Remove the old table for schedule_tomorrow
            db.execSQL("DROP TABLE `schedule_tomorrow`")
            // Rename the new table to the original table name for schedule_tomorrow
            db.execSQL("ALTER TABLE `schedule_tomorrow_new` RENAME TO `schedule_tomorrow`")
        }
    }

val MIGRATION_30_31 =
    object : Migration(
        30,
        31,
    ) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create watched_episodes table for tracking episode watch progress
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `watched_episodes` (
                    `showTraktId` INTEGER NOT NULL,
                    `showTvMazeId` INTEGER,
                    `showImdbId` TEXT,
                    `seasonNumber` INTEGER NOT NULL,
                    `episodeNumber` INTEGER NOT NULL,
                    `episodeTraktId` INTEGER,
                    `watchedAt` INTEGER NOT NULL,
                    `syncStatus` INTEGER NOT NULL DEFAULT 0,
                    `lastModified` INTEGER NOT NULL,
                    PRIMARY KEY(`showTraktId`, `seasonNumber`, `episodeNumber`)
                )
                """.trimIndent(),
            )

            // Create index for efficient show-level queries
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_watched_episodes_showTraktId` ON `watched_episodes` (`showTraktId`)",
            )

            // Create index for sync status queries
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_watched_episodes_syncStatus` ON `watched_episodes` (`syncStatus`)",
            )
        }
    }
