package com.theupnextapp.domain

data class TraktRemoveFromHistory(
    val deletedEpisodes: Int?,
    val notFoundShows: Int?,
    val notFoundSeasons: Int?,
    val notFoundEpisodes: Int?
)