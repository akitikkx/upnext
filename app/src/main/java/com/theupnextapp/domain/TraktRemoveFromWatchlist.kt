package com.theupnextapp.domain

data class TraktRemoveFromWatchlist(
    val deletedShows: Int?,
    val deletedSeasons: Int?,
    val deletedEpisodes: Int?,
    val notFoundShows: Int?,
    val notFoundSeasons: Int?,
    val notFoundEpisodes: Int?
)