package com.theupnextapp.network.models.tvmaze

data class NetworkShowPreviousEpisodeResponse constructor(
    val _links: NetworkShowPreviousEpisodeLinks?,
    val airdate: String?,
    val airstamp: String?,
    val airtime: String?,
    val id: Int,
    val image: NetworkShowPreviousEpisodeImage?,
    val name: String?,
    val number: Int,
    val runtime: Int?,
    val season: Int?,
    val summary: String?,
    val url: String?
)

data class NetworkShowPreviousEpisodeLinks(
    val self: NetworkShowPreviousEpisodeSelf
)

data class NetworkShowPreviousEpisodeSelf(
    val href: String
)

data class NetworkShowPreviousEpisodeImage(
    val medium: String,
    val original: String
)