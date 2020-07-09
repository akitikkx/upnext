package com.theupnextapp.domain

data class TraktRecommendations(
    val id: Int?,
    val title: String?,
    val year: Int?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?
)