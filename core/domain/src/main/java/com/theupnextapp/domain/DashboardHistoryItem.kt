package com.theupnextapp.domain

data class DashboardHistoryItem(
    val showTraktId: Int?,
    val showImdbId: String?,
    val showTvMazeId: Int?,
    val season: Int?,
    val number: Int?,
    val watchedAt: String?,
    val showTitle: String? = null
)
