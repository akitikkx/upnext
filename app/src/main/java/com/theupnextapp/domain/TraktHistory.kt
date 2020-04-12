package com.theupnextapp.domain

data class TraktHistory(
    val id: Int?,
    val showTitle: String?,
    val showYear: Int?,
    val episodeTitle: String?,
    val episodeSeasonNumber: Int?,
    val episodeNumber : Int?,
    val historyType: String?,
    val watchedAt: String?,
    val historyAction: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?
)