package com.theupnextapp.network.models.trakt

data class NetworkTraktRemoveSeasonFromHistoryRequest(
    val episodes: List<NetworkTraktRemoveSeasonFromHistoryRequestEpisode>,
    val ids: List<Int>,
    val movies: List<NetworkTraktRemoveSeasonFromHistoryRequestMovy>,
    val seasons: List<NetworkTraktRemoveSeasonFromHistoryRequestSeason>,
    val shows: List<NetworkTraktRemoveSeasonFromHistoryRequestShow>
)

data class NetworkTraktRemoveSeasonFromHistoryRequestEpisode(
    val ids: NetworkTraktRemoveSeasonFromHistoryRequestIds
)

data class NetworkTraktRemoveSeasonFromHistoryRequestMovy(
    val ids: NetworkTraktRemoveSeasonFromHistoryRequestIdsX,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveSeasonFromHistoryRequestSeason(
    val ids: NetworkTraktRemoveSeasonFromHistoryRequestIdsXX
)

data class NetworkTraktRemoveSeasonFromHistoryRequestShow(
    val ids: Any,
    val seasons: List<NetworkTraktRemoveSeasonFromHistoryRequestSeasonX?>,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveSeasonFromHistoryRequestIds(
    val imdb: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveSeasonFromHistoryRequestIdsX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int
)

data class NetworkTraktRemoveSeasonFromHistoryRequestIdsXX(
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveSeasonFromHistoryRequestIdsXXX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveSeasonFromHistoryRequestSeasonX(
    val episodes: List<NetworkTraktRemoveSeasonFromHistoryRequestEpisodeX>,
    val number: Int?
)

data class NetworkTraktRemoveSeasonFromHistoryRequestEpisodeX(
    val number: Int
)