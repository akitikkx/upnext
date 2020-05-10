package com.theupnextapp.domain

data class TraktAddToWatchlist(
    val addedShows: Int?,
    val addedSeasons: Int?,
    val addedEpisodes: Int?,
    val existingShows: Int?,
    val existingSeasons: Int?,
    val existingEpisodes: Int?,
    val notFoundShows: Int?,
    val notFoundSeasons: Int?,
    val notFoundEpisodes: Int?
)