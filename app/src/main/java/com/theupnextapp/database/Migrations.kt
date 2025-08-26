package com.theupnextapp.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_29_30 = object : Migration(29, 30) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE `schedule_yesterday_new` (`id` INTEGER NOT NULL, `showId` INTEGER, `image` TEXT, `mediumImage` TEXT, `language` TEXT, `name` TEXT, `officialSite` TEXT, `premiered` TEXT, `runtime` TEXT, `status` TEXT, `summary` TEXT, `type` TEXT, `updated` TEXT, `url` TEXT, PRIMARY KEY(`id`))")
        db.execSQL("INSERT INTO `schedule_yesterday_new` (`id`, `showId`, `image`, `mediumImage`, `language`, `name`, `officialSite`, `premiered`, `runtime`, `status`, `summary`, `type`, `updated`, `url`) SELECT `id`, `showId`, `image`, `mediumImage`, `language`, `name`, `officialSite`, `premiered`, `runtime`, `status`, `summary`, `type`, `updated`, `url` FROM `schedule_yesterday`")
        db.execSQL("DROP TABLE `schedule_yesterday`")
        db.execSQL("ALTER TABLE `schedule_yesterday_new` RENAME TO `schedule_yesterday`")
    }
}
