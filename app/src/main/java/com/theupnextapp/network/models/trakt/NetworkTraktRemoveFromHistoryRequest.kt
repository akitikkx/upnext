package com.theupnextapp.network.models.trakt

data class NetworkTraktRemoveFromHistoryRequest(
    val episodes: List<NetworkTraktRemoveFromHistoryRequestEpisode>,
    val ids: List<Int>,
    val movies: List<NetworkTraktRemoveFromHistoryRequestMovy>,
    val seasons: List<NetworkTraktRemoveFromHistoryRequestSeason>,
    val shows: List<NetworkTraktRemoveFromHistoryRequestShow>
)

data class NetworkTraktRemoveFromHistoryRequestEpisode(
    val ids: NetworkTraktRemoveFromHistoryRequestIds
)

data class NetworkTraktRemoveFromHistoryRequestMovy(
    val ids: NetworkTraktRemoveFromHistoryRequestIdsX,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveFromHistoryRequestSeason(
    val ids: NetworkTraktRemoveFromHistoryRequestIdsXX
)

data class NetworkTraktRemoveFromHistoryRequestShow(
    val ids: Any,
    val seasons: List<NetworkTraktRemoveFromHistoryRequestSeasonX?>,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveFromHistoryRequestIds(
    val imdb: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromHistoryRequestIdsX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int
)

data class NetworkTraktRemoveFromHistoryRequestIdsXX(
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromHistoryRequestIdsXXX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromHistoryRequestSeasonX(
    val episodes: List<NetworkTraktRemoveFromHistoryRequestEpisodeX?>,
    val number: Int?
)

data class NetworkTraktRemoveFromHistoryRequestEpisodeX(
    val number: Int
)