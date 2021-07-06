package com.theupnextapp.network.models.tvmaze

import com.theupnextapp.domain.ShowSeasonEpisode

class NetworkTvMazeEpisodesResponse : ArrayList<NetworkTvMazeEpisodesResponseItem>()

data class NetworkTvMazeEpisodesResponseItem(
    val _links: NetworkTvMazeEpisodesResponseLinks?,
    val airdate: String?,
    val airstamp: String?,
    val airtime: String?,
    val id: Int?,
    val image: NetworkTvMazeEpisodesResponseImage?,
    val name: String?,
    val number: Int?,
    val runtime: Int?,
    val season: Int?,
    val summary: String?,
    val type: String?,
    val url: String?
)

data class NetworkTvMazeEpisodesResponseLinks(
    val self: NetworkTvMazeEpisodesResponseSelf?
)

data class NetworkTvMazeEpisodesResponseImage(
    val medium: String?,
    val original: String?
)

data class NetworkTvMazeEpisodesResponseSelf(
    val href: String
)

fun List<NetworkTvMazeEpisodesResponseItem>.asDomainModel(): List<ShowSeasonEpisode> {
    return map {
        ShowSeasonEpisode(
            id = it.id,
            name = it.name,
            season = it.season,
            number = it.number,
            runtime = it.runtime,
            originalImageUrl = it.image?.original,
            mediumImageUrl = it.image?.medium,
            summary = it.summary,
            type = it.type,
            airdate = it.airdate,
            airstamp = it.airstamp,
            airtime = it.airtime
        )
    }
}