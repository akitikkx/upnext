package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktPopularShows

class NetworkTraktPopularShowsResponse : ArrayList<NetworkTraktPopularShowsResponseItem>()

data class NetworkTraktPopularShowsResponseItem(
    val ids: NetworkTraktPopularShowsResponseItemIds?,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktPopularShowsResponseItemIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?,
    var tvMazeID: Int?
)

fun NetworkTraktPopularShowsResponseItem.asDomainModel() : TraktPopularShows {
    return TraktPopularShows(
        title = title,
        year = year.toString(),
        mediumImageUrl = mediumImageUrl,
        originalImageUrl = originalImageUrl,
        imdbID = ids?.imdb,
        slug = ids?.imdb,
        tmdbID = ids?.tmdb,
        traktID = ids?.trakt,
        tvMazeID = ids?.tvMazeID,
        tvdbID = ids?.tvdb
    )
}