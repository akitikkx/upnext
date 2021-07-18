package com.theupnextapp.network.models.trakt

data class NetworkTraktShowInfoResponse(
    val ids: NetworkTraktShowInfoResponseIds?,
    val title: String?,
    val year: Int?
)

data class NetworkTraktShowInfoResponseIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?
)