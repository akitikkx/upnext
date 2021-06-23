package com.theupnextapp.common.utils.models

enum class TableUpdateInterval(val intervalMins: Long, val intervalHours: Long) {
    DASHBOARD_ITEMS(240, 4),
    TRAKT_POPULAR_ITEMS(30, 0),
    TRAKT_TRENDING_ITEMS(60, 1),
    TRAKT_MOST_ANTICIPATED_ITEMS(30, 0)
}