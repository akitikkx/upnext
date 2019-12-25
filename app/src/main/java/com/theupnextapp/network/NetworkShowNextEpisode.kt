package com.theupnextapp.network

data class NetworkShowNextEpisode constructor(
    val _links: NetworkShowNextEpisodeLinks?,
    val airdate: String?,
    val airstamp: String?,
    val airtime: String?,
    val id: Int,
    val image: NetworkShowNextEpisodeImage?,
    val name: String?,
    val number: Int,
    val runtime: Int?,
    val season: Int?,
    val summary: String?,
    val url: String?
)

data class NetworkShowNextEpisodeLinks(
    val self: NetworkShowNextEpisodeSelf
)

data class NetworkShowNextEpisodeSelf(
    val href: String
)

data class NetworkShowNextEpisodeImage(
    val medium: String,
    val original: String
)