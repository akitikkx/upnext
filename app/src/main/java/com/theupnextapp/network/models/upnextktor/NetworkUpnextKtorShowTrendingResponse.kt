package com.theupnextapp.network.models.upnextktor

import com.theupnextapp.database.DatabaseTraktTrendingShows

class NetworkUpnextKtorShowTrendingResponse : ArrayList<NetworkUpnextKtorShowTrendingResponseItem>()

data class NetworkUpnextKtorShowTrendingResponseItem(
    val show: Show,
    val watchers: Int
)

data class Show(
    val ids: Ids,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val title: String?,
    val year: Int?
)

data class Ids(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int,
    val tvMazeID: Int?,
    val tvdb: Int?
)

fun NetworkUpnextKtorShowTrendingResponseItem.asDatabaseModel(): DatabaseTraktTrendingShows {
    return DatabaseTraktTrendingShows(
        id = show.ids.trakt,
        title = show.title,
        year = show.year.toString(),
        medium_image_url = show.mediumImageUrl,
        original_image_url = show.originalImageUrl,
        imdbID = show.ids.imdb,
        slug = show.ids.slug,
        tmdbID = show.ids.tmdb,
        traktID = show.ids.trakt,
        tvdbID = show.ids.tvdb,
        tvMazeID = show.ids.tvMazeID
    )
}