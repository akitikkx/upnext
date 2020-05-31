package com.theupnextapp.network.models.trakt

import com.theupnextapp.database.DatabaseTraktWatchlist

class NetworkTraktWatchlistResponse : ArrayList<NetworkTraktWatchlistResponseItem>()

data class NetworkTraktWatchlistResponseItem(
    val id: Int,
    val listed_at: String?,
    val rank: Int?,
    val show: TraktWatchlistShow?,
    val type: String?
)

data class TraktWatchlistShow(
    val ids: TraktWatchlistIds?,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class TraktWatchlistIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?,
    val tvrage: Int?,
    var tvMaze: Int?
)


fun NetworkTraktWatchlistResponseItem.asDatabaseModel(): DatabaseTraktWatchlist {
    return DatabaseTraktWatchlist(
        id = id,
        listed_at = listed_at,
        rank = rank,
        title = show?.title,
        mediumImageUrl = show?.mediumImageUrl,
        originalImageUrl = show?.originalImageUrl,
        imdbID = show?.ids?.imdb,
        slug = show?.ids?.slug,
        tmdbID = show?.ids?.tmdb,
        traktID = show?.ids?.trakt,
        tvdbID = show?.ids?.tvdb,
        tvrageID = show?.ids?.tvrage,
        tvMazeID = show?.ids?.tvMaze
    )
}