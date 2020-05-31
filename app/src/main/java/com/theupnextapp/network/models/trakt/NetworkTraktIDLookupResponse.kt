package com.theupnextapp.network.models.trakt

class NetworkTraktIDLookupResponse : ArrayList<NetworkTraktIDLookupResponseItem>()

data class NetworkTraktIDLookupResponseItem(
    val show: NetworkTraktIDLookupShow,
    val type: String
)

data class NetworkTraktIDLookupShow(
    val ids: Any,
    val title: String,
    val year: Int
)

data class Ids(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int
)