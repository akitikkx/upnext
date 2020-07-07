package com.theupnextapp.common.utils.models

enum class TableUpdateInterval(val intervalMins: Long, val intervalHours: Long) {
    DASHBOARD_ITEMS(240, 4),
    WATCHLIST_ITEMS(30, 0),
    HISTORY_ITEMS(30, 0),
    COLLECTION_ITEMS(30, 0),
    RECOMMENDED_ITEMS(60, 0)
}