package com.theupnextapp.network

data class NetworkTraktAddToWatchlistRequest(
    val episodes: List<Episode>,
    val movies: List<NetworkTraktAddToWatchlistRequestMovy>,
    val seasons: List<Season>,
    val shows: List<NetworkTraktAddToWatchlistRequestShow>
)

data class Episode(
    val ids: NetworkTraktAddToWatchlistRequestEpisodeIds
)

data class NetworkTraktAddToWatchlistRequestMovy(
    val ids: NetworkTraktAddToWatchlistResponseIds,
    val title: String,
    val year: Int
)

data class Season(
    val ids: NetworkTraktAddToWatchlistRequestIdsXX
)

data class NetworkTraktAddToWatchlistRequestShow(
    val ids: NetworkTraktAddToWatchlistResponseIds,
    val seasons: List<NetworkTraktAddToWatchlistRequestSeasonX?>,
    val title: String,
    val year: Int
)

data class NetworkTraktAddToWatchlistRequestEpisodeIds(
    val imdb: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktAddToWatchlistRequestIdsX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int
)

data class NetworkTraktAddToWatchlistRequestIdsXX(
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktAddToWatchlistRequestIds(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktAddToWatchlistRequestSeasonX(
    val episodes: List<NetworkTraktAddToWatchlistRequestEpisodeX>,
    val number: Int
)

data class NetworkTraktAddToWatchlistRequestEpisodeX(
    val number: Int
)