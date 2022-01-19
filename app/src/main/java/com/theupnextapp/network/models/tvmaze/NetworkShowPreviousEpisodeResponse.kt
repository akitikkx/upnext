package com.theupnextapp.network.models.tvmaze

import com.theupnextapp.domain.ShowPreviousEpisode

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

fun NetworkShowPreviousEpisodeResponse.asDomainModel(): ShowPreviousEpisode {
    return ShowPreviousEpisode(
        previousEpisodeId = id,
        previousEpisodeUrl = url,
        previousEpisodeSummary = summary,
        previousEpisodeSeason = season.toString(),
        previousEpisodeRuntime = runtime.toString(),
        previousEpisodeNumber = number.toString(),
        previousEpisodeName = name,
        previousEpisodeMediumImageUrl = image?.medium,
        previousEpisodeOriginalImageUrl = image?.original,
        previousEpisodeAirtime = airtime,
        previousEpisodeAirstamp = airstamp,
        previousEpisodeAirdate = airdate
    )
}