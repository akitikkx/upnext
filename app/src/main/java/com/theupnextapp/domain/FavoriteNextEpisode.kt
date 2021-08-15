package com.theupnextapp.domain

data class FavoriteNextEpisode(
    val tvMazeID: Int?,
    val number: Int?,
    val season: Int?,
    val title: String?,
    val airStamp: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdb: String?
)