package com.theupnextapp.network

class NetworkTraktHistoryResponse : ArrayList<NetworkTraktHistoryResponseItem>()

data class NetworkTraktHistoryResponseItem(
    val action: String,
    val episode: NetworkTraktHistoryEpisode,
    val id: Int,
    val movie: NetworkTraktHistoryMovie,
    val show: NetworkTraktHistoryShow,
    val type: String,
    val watched_at: String
)

data class NetworkTraktHistoryEpisode(
    val ids: NetworkTraktHistoryEpisodeIds,
    val number: Int,
    val season: Int,
    val title: String
)

data class NetworkTraktHistoryMovie(
    val ids: NetworkTraktHistoryMovieIds,
    val title: String,
    val year: Int
)

data class NetworkTraktHistoryShow(
    val ids: NetworkTraktHistoryShowIds,
    val title: String,
    val year: Int
)

data class NetworkTraktHistoryEpisodeIds(
    val imdb: Any,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktHistoryMovieIds(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int
)

data class NetworkTraktHistoryShowIds(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)