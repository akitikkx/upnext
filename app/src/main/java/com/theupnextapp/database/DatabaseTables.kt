package com.theupnextapp.database

enum class DatabaseTables(val tableName: String) {
    TABLE_WATCHLIST("trakt_watchlist"),
    TABLE_COLLECTION("trakt_collection"),
    TABLE_HISTORY("trakt_history")
}