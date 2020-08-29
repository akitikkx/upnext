package com.theupnextapp.network.models.trakt

import com.theupnextapp.database.DatabaseTraktPopularShows
import com.theupnextapp.domain.TraktPopularShows

class NetworkTraktPopularShowsResponse : ArrayList<NetworkTraktPopularShowsResponseItem>()

data class NetworkTraktPopularShowsResponseItem(
    val ids: NetworkTraktPopularShowsResponseItemIds,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktPopularShowsResponseItemIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int,
    val tvdb: Int?,
    var tvMazeID: Int?
)

fun NetworkTraktPopularShowsResponseItem.asDomainModel(): TraktPopularShows {
    return TraktPopularShows(
        id = ids.trakt,
        title = title,
        year = year.toString(),
        mediumImageUrl = mediumImageUrl,
        originalImageUrl = originalImageUrl,
        imdbID = ids.imdb,
        slug = ids.imdb,
        tmdbID = ids.tmdb,
        traktID = ids.trakt,
        tvMazeID = ids.tvMazeID,
        tvdbID = ids.tvdb
    )
}

fun NetworkTraktPopularShowsResponseItem.asDatabaseModel(): DatabaseTraktPopularShows {
    return DatabaseTraktPopularShows(
        id = ids.trakt,
        title = title,
        year = year.toString(),
        medium_image_url = mediumImageUrl,
        original_image_url = originalImageUrl,
        imdbID = ids.imdb,
        slug = ids.slug,
        tmdbID = ids.tmdb,
        traktID = ids.trakt,
        tvdbID = ids.tvdb,
        tvMazeID = ids.tvMazeID
    )
}