package com.theupnextapp.network

import com.theupnextapp.database.DatabaseTraktHistory

class NetworkTraktHistoryResponse : ArrayList<NetworkTraktHistoryResponseItem>()

data class NetworkTraktHistoryResponseItem(
    val action: String?,
    val episode: NetworkTraktHistoryEpisode?,
    val id: Long?,
    val movie: NetworkTraktHistoryMovie?,
    val show: NetworkTraktHistoryShow?,
    val type: String?,
    val watched_at: String?
)

data class NetworkTraktHistoryEpisode(
    val ids: NetworkTraktHistoryEpisodeIds?,
    val number: Int?,
    val season: Int?,
    val title: String?
)

data class NetworkTraktHistoryMovie(
    val ids: NetworkTraktHistoryMovieIds?,
    val title: String?,
    val year: Int?
)

data class NetworkTraktHistoryShow(
    val ids: NetworkTraktHistoryShowIds?,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktHistoryEpisodeIds(
    val imdb: Any?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?
)

data class NetworkTraktHistoryMovieIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?
)

data class NetworkTraktHistoryShowIds(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int,
    var tvMaze: Int?
)

fun NetworkTraktHistoryResponseItem.asDatabaseModel(): DatabaseTraktHistory {
    return DatabaseTraktHistory(
        id = id?.toInt(),
        showTitle = show?.title,
        showYear = show?.year,
        episodeTitle = episode?.title,
        episodeSeasonNumber = episode?.season,
        episodeNumber = episode?.number,
        historyAction = action,
        historyType = type,
        watchedAt = watched_at,
        mediumImageUrl = show?.mediumImageUrl,
        originalImageUrl = show?.originalImageUrl,
        imdbID = show?.ids?.imdb,
        slug = show?.ids?.slug,
        tmdbID = show?.ids?.tmdb,
        traktID = show?.ids?.trakt,
        tvdbID = show?.ids?.tvdb,
        tvMazeID = show?.ids?.tvMaze
    )
}