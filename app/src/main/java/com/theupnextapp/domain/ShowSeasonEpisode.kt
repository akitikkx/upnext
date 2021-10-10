package com.theupnextapp.domain

data class ShowSeasonEpisode(
    val id: Int?,
    val name: String?,
    val season: Int?,
    val number: Int?,
    val runtime: Int?,
    val originalImageUrl: String?,
    val mediumImageUrl: String?,
    val summary: String?,
    val type: String?,
    val airdate: String?,
    val airstamp: String?,
    val airtime: String?,
    var imdbID: String? = null
)