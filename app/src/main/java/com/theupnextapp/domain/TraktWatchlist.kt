package com.theupnextapp.domain

data class TraktWatchlist(
    val id: Int?,
    val listed_at: String?,
    val rank: Int?,
    val title: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvrageID: Int?,
    val tvMazeID: Int?
)