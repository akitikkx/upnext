package com.theupnextapp.network.models.trakt

data class NetworkTraktRemoveFromWatchlistRequest(
    val movies: List<NetworkTraktRemoveFromWatchlistRequestMovy>,
    val shows: List<NetworkTraktRemoveFromWatchlistRequestShow>
)

data class NetworkTraktRemoveFromWatchlistRequestMovy(
    val ids: NetworkTraktRemoveFromWatchlistRequestIds,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveFromWatchlistRequestShow(
    val ids: Any,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveFromWatchlistRequestEpisodeIds(
    val imdb: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromWatchlistRequestIdsXX(
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromWatchlistRequestIds(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromWatchlistRequestSeasonX(
    val episodes: List<NetworkTraktRemoveFromWatchlistRequestEpisodeX?>,
    val number: Int?
)

data class NetworkTraktRemoveFromWatchlistRequestEpisodeX(
    val number: Int
)