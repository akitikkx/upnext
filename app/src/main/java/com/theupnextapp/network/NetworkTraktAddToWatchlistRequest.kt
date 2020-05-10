package com.theupnextapp.network

data class NetworkTraktAddToWatchlistRequest(
    val episodes: List<Episode>,
    val movies: List<NetworkTraktAddToWatchlistRequestMovy>,
    val seasons: List<Season>,
    val shows: List<NetworkTraktAddToWatchlistRequestShow>
)

data class Episode(
    val ids: NetworkTraktRemoveFromWatchlistRequestEpisodeIds
)

data class NetworkTraktAddToWatchlistRequestMovy(
    val ids: NetworkTraktAddToWatchlistResponseIds,
    val title: String,
    val year: Int
)

data class Season(
    val ids: NetworkTraktRemoveFromWatchlistRequestIdsXX
)

data class NetworkTraktAddToWatchlistRequestShow(
    val ids: Any,
    val seasons: List<NetworkTraktRemoveFromWatchlistRequestSeasonX?>,
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
    val episodes: List<NetworkTraktRemoveFromWatchlistRequestEpisodeX>,
    val number: Int
)

data class NetworkTraktAddToWatchlistRequestEpisodeX(
    val number: Int
)