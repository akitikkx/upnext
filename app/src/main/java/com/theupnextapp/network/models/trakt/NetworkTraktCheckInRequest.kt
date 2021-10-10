package com.theupnextapp.network.models.trakt

data class NetworkTraktCheckInRequest(
    val app_date: String? = null,
    val app_version: String? = null,
    val episode: NetworkTraktCheckInRequestEpisode?,
    val message: String? = null,
    val sharing: NetworkTraktCheckInRequestSharing? = null,
    val show: NetworkTraktCheckInRequestShow?
)

data class NetworkTraktCheckInRequestShow(
    val ids: NetworkTraktCheckInRequestShowIds?,
    val title: String?,
    val year: Int?
)

data class NetworkTraktCheckInRequestEpisode(
    val number: Int?,
    val season: Int?
)

data class NetworkTraktCheckInRequestSharing(
    val tumblr: Boolean?,
    val twitter: Boolean?
)

data class NetworkTraktCheckInRequestShowIds(
    val trakt: Int?,
    val tvdb: Int? = null
)