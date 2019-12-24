package com.theupnextapp.network

data class TodayNetworkSchedule(
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
    val url: String
)