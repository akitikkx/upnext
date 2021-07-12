package com.theupnextapp.network.models.trakt

import com.theupnextapp.database.DatabaseFavoriteShows

class NetworkTraktUserListItemResponse : ArrayList<NetworkTraktUserListItemResponseItem>()

data class NetworkTraktUserListItemResponseItem(
    val id: Int?,
    val listed_at: String?,
    val rank: Int?,
    val show: NetworkTraktUserListItemResponseItemShow?,
    val type: String?
)

data class NetworkTraktUserListItemResponseItemShow(
    val ids: NetworkTraktUserListItemResponseItemShowIds?,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktUserListItemResponseItemShowIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?,
    var tvMazeID: Int?
)

fun NetworkTraktUserListItemResponseItem.asDatabaseModel(): DatabaseFavoriteShows {
    return DatabaseFavoriteShows(
        id = id,
        title = show?.title,
        slug = show?.ids?.slug,
        year = show?.year.toString(),
        mediumImageUrl = show?.mediumImageUrl,
        originalImageUrl = show?.originalImageUrl,
        imdbID = show?.ids?.imdb,
        tmdbID = show?.ids?.tmdb,
        traktID = show?.ids?.trakt,
        tvdbID = show?.ids?.tvdb,
        tvMazeID = show?.ids?.tvMazeID
    )
}