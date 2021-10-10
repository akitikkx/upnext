package com.theupnextapp.network.models.trakt

class NetworkTraktIdLookupResponse : ArrayList<NetworkTraktIdLookupResponseItem>()

data class NetworkTraktIdLookupResponseItem(
    val show: NetworkTraktIdLookupResponseItemShow?,
    val score: Any?,
    val type: String?
)

data class NetworkTraktIdLookupResponseItemShow(
    val ids: NetworkTraktIdLookupResponseItemShowIds?,
    val title: String?,
    val year: Int?
)

data class NetworkTraktIdLookupResponseItemShowIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?
)