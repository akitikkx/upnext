package com.theupnextapp.network.models.tmdb

data class NetworkTmdbPersonImagesResponse(
    val id: Int?, // TMDB Person ID
    val profiles: List<NetworkTmdbPersonProfile>?
)

data class NetworkTmdbPersonProfile(
    val aspect_ratio: Double?,
    val height: Int?,
    val iso_639_1: String?,
    val file_path: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val width: Int?
)
