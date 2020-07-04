package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktTrendingShows

class NetworkTraktTrendingShowsResponse : ArrayList<NetworkTraktTrendingShowsResponseItem>()

data class NetworkTraktTrendingShowsResponseItem(
    val show: NetworkTraktTrendingShowsResponseItemShow?,
    val watchers: Int?
)

data class NetworkTraktTrendingShowsResponseItemShow(
    val ids: NetworkTraktTrendingShowsResponseItemShowIds?,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktTrendingShowsResponseItemShowIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?,
    var tvMazeID: Int?
)

fun NetworkTraktTrendingShowsResponseItem.asDomainModel(): TraktTrendingShows {
    return TraktTrendingShows(
        title = show?.title,
        year = show?.year.toString(),
        mediumImageUrl = show?.mediumImageUrl,
        originalImageUrl = show?.originalImageUrl,
        imdbID = show?.ids?.imdb,
        slug = show?.ids?.imdb,
        tmdbID = show?.ids?.tmdb,
        traktID = show?.ids?.trakt,
        tvMazeID = show?.ids?.tvMazeID,
        tvdbID = show?.ids?.tvdb
    )

}