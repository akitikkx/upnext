package com.theupnextapp.common.utils.models

enum class DatabaseTables(val tableName: String) {
    TABLE_TOMORROW_SHOWS("schedule_tomorrow"),
    TABLE_TODAY_SHOWS("schedule_today"),
    TABLE_YESTERDAY_SHOWS("schedule_yesterday"),
    TABLE_TRAKT_POPULAR("trakt_popular"),
    TABLE_TRAKT_TRENDING("trakt_trending"),
    TABLE_TRAKT_MOST_ANTICIPATED("trakt_most_anticipated"),
    TABLE_FAVORITE_SHOWS("favorite_shows"),
    TABLE_FAVORITE_EPISODES("favorite_next_episodes"),
}