package com.theupnextapp.domain

data class TraktAddToCollection(
    val addedEpisodes: Int?,
    val existingEpisodes: Int?,
    val notFoundEpisodes: Int?,
    val notFoundShows: Int?,
    val notFoundSeasons: Int?,
    val updatedEpisodes: Int?
)