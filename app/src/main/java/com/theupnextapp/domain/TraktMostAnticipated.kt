package com.theupnextapp.domain

data class TraktMostAnticipated(
    val id: Long?,
    val title: String?,
    val year: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?,
    val listCount: Int?
)