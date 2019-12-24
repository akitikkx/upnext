package com.theupnextapp.network

data class NetworkShowEpisode constructor(
    val _links: NetworkShowEpisodeLinks?,
    val airdate: String?,
    val airstamp: String?,
    val airtime: String?,
    val id: Int,
    val image: NetworkShowEpisodeImage?,
    val name: String?,
    val number: Int,
    val runtime: Int?,
    val season: Int?,
    val summary: String?,
    val url: String?
)

data class NetworkShowEpisodeLinks(
    val self: NetworkShowEpisodeSelf
)

data class NetworkShowEpisodeSelf(
    val href: String
)

data class NetworkShowEpisodeImage(
    val medium: String,
    val original: String
)