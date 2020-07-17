package com.theupnextapp.common.utils.models

enum class DatabaseTables(val tableName: String) {
    TABLE_TRAKT_WATCHLIST("trakt_watchlist"),
    TABLE_TRAKT_COLLECTION("trakt_collection"),
    TABLE_TRAKT_HISTORY("trakt_history"),
    TABLE_TOMORROW_SHOWS("schedule_tomorrow"),
    TABLE_TODAY_SHOWS("schedule_today"),
    TABLE_YESTERDAY_SHOWS("schedule_yesterday"),
    TABLE_TRAKT_RECOMMENDATIONS("trakt_recommendations"),
    TABLE_TRAKT_POPULAR("trakt_popular"),
    TABLE_TRAKT_TRENDING("trakt_trending"),
}