package com.theupnextapp.common.utils.models

enum class DatabaseTables(val tableName: String) {
    TABLE_WATCHLIST("trakt_watchlist"),
    TABLE_COLLECTION("trakt_collection"),
    TABLE_HISTORY("trakt_history"),
    TABLE_TOMORROW_SHOWS("schedule_tomorrow"),
    TABLE_TODAY_SHOWS("schedule_today"),
    TABLE_YESTERDAY_SHOWS("schedule_yesterday"),
}