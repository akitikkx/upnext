package com.theupnextapp.network.models.tvmaze

data class NetworkTodayScheduleResponse(
    val _links: NetworkShowEpisodeLinks,
    val airdate: String,
    val airstamp: String,
    val airtime: String,
    val id: Int,
    val image: Any,
    val name: String,
    val number: Int,
    val runtime: Int,
    val season: Int,
    val show: NetworkScheduleShow,
    val summary: String,
    val url: String,
    val imdbId: String?
)