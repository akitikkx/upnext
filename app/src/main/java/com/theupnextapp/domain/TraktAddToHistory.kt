package com.theupnextapp.domain

data class TraktAddToHistory(
    val addedEpisodes: Int?,
    val notFoundShows: Int?,
    val notFoundSeasons: Int?,
    val notFoundEpisodes: Int?
)