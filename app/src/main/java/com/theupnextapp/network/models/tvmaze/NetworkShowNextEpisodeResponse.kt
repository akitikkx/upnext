package com.theupnextapp.network.models.tvmaze

import com.theupnextapp.database.DatabaseFavoriteNextEpisode
import com.theupnextapp.domain.ShowNextEpisode

data class NetworkShowNextEpisodeResponse constructor(
    val _links: NetworkShowNextEpisodeLinks?,
    val airdate: String?,
    val airstamp: String?,
    val airtime: String?,
    val id: Int,
    val image: NetworkShowNextEpisodeImage?,
    var mediumShowImageUrl: String?,
    var originalShowImageUrl: String?,
    var tvMazeID: Int?,
    var imdb: String?,
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

fun NetworkShowNextEpisodeResponse.asDatabaseModel(): DatabaseFavoriteNextEpisode {
    return DatabaseFavoriteNextEpisode(
        tvMazeID = tvMazeID,
        season = season,
        number = number,
        title = name,
        airStamp = airstamp,
        mediumImageUrl = mediumShowImageUrl,
        originalImageUrl = originalShowImageUrl,
        imdb = imdb
    )
}

fun NetworkShowNextEpisodeResponse.asDomainModel(): ShowNextEpisode {
    return ShowNextEpisode(
        nextEpisodeId = id,
        nextEpisodeUrl = url,
        nextEpisodeSummary = summary,
        nextEpisodeSeason = season.toString(),
        nextEpisodeRuntime = runtime.toString(),
        nextEpisodeNumber = number.toString(),
        nextEpisodeName = name,
        nextEpisodeMediumImageUrl = image?.medium,
        nextEpisodeOriginalImageUrl = image?.original,
        nextEpisodeAirtime = airtime,
        nextEpisodeAirstamp = airstamp,
        nextEpisodeAirdate = airdate,
    )
}