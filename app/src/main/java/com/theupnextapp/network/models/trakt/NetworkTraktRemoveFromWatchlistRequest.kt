package com.theupnextapp.network.models.trakt

data class NetworkTraktRemoveFromWatchlistRequest(
    val episodes: List<NetworkTraktWatchedShowProgressSeasonEpisode>,
    val movies: List<NetworkTraktRemoveFromWatchlistRequestMovy>,
    val seasons: List<NetworkTraktWatchedShowProgressSeason>,
    val shows: List<NetworkTraktRemoveFromWatchlistRequestShow>
)

data class NetworkTraktRemoveFromWatchlistEpisode(
    val ids: NetworkTraktRemoveFromWatchlistRequestEpisodeIds
)

data class NetworkTraktRemoveFromWatchlistRequestMovy(
    val ids: NetworkTraktRemoveFromWatchlistRequestIds,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveFromWatchlistSeason(
    val ids: NetworkTraktRemoveFromWatchlistRequestIdsXX
)

data class NetworkTraktRemoveFromWatchlistRequestShow(
    val ids: Any,
    val seasons: List<NetworkTraktRemoveFromWatchlistSeason?>,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveFromWatchlistRequestEpisodeIds(
    val imdb: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromWatchlistRequestIdsX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int
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
    val episodes: List<NetworkTraktRemoveFromWatchlistRequestEpisodeX>,
    val number: Int
)

data class NetworkTraktRemoveFromWatchlistRequestEpisodeX(
    val number: Int
)