package com.theupnextapp.domain

data class TraktRemoveFromCollection(
    val deletedEpisodes: Int?,
    val notFoundEpisodes: Int?,
    val notFoundSeasons: Int?,
    val notFoundShows: Int?
)