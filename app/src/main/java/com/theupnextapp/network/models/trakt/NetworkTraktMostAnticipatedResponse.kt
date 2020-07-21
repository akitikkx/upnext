package com.theupnextapp.network.models.trakt

import com.theupnextapp.database.DatabaseTraktMostAnticipated

class NetworkTraktMostAnticipatedResponse : ArrayList<NetworkTraktMostAnticipatedResponseItem>()

data class NetworkTraktMostAnticipatedResponseItem(
    val list_count: Int?,
    val show: NetworkTraktMostAnticipatedResponseItemShow?
)

data class NetworkTraktMostAnticipatedResponseItemShow(
    val ids: NetworkTraktMostAnticipatedResponseItemIds?,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktMostAnticipatedResponseItemIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?,
    var tvMazeID: Int?
)

fun NetworkTraktMostAnticipatedResponseItem.asDatabaseModel() : DatabaseTraktMostAnticipated {
    return DatabaseTraktMostAnticipated(
        id = show?.ids?.trakt?.toLong(),
        title = show?.title,
        year = show?.year.toString(),
        medium_image_url = show?.mediumImageUrl,
        original_image_url = show?.originalImageUrl,
        imdbID = show?.ids?.imdb,
        slug = show?.ids?.slug,
        tmdbID = show?.ids?.tmdb,
        traktID = show?.ids?.trakt,
        tvdbID = show?.ids?.tvdb,
        tvMazeID = show?.ids?.tvMazeID,
        list_count = list_count
    )
}