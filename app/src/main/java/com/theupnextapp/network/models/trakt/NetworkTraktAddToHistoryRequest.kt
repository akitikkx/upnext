package com.theupnextapp.network.models.trakt

data class NetworkTraktAddToHistoryRequest(
    val episodes: List<NetworkTraktAddToHistoryRequestEpisode?>,
    val movies: List<NetworkTraktAddToHistoryRequestMovy?>,
    val seasons: List<NetworkTraktAddToHistoryRequestSeason?>,
    val shows: List<NetworkTraktAddToHistoryRequestShow?>
)

data class NetworkTraktAddToHistoryRequestEpisode(
    val ids: NetworkTraktAddToHistoryRequestIds?,
    val watched_at: String?
)

data class NetworkTraktAddToHistoryRequestMovy(
    val ids: NetworkTraktAddToHistoryRequestIdsX?,
    val title: String?,
    val watched_at: String?,
    val year: Int?
)

data class NetworkTraktAddToHistoryRequestSeason(
    val ids: NetworkTraktAddToHistoryRequestIdsXX?,
    val watched_at: String?
)

data class NetworkTraktAddToHistoryRequestShow(
    val ids: Any,
    val seasons: List<NetworkTraktAddToHistoryRequestSeasonX?>,
    val title: String?,
    val year: Int?
)

data class NetworkTraktAddToHistoryRequestIds(
    val imdb: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?
)

data class NetworkTraktAddToHistoryRequestIdsX(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?
)

data class NetworkTraktAddToHistoryRequestIdsXX(
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?
)

data class NetworkTraktAddToHistoryRequestIdsXXX(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?
)

data class NetworkTraktAddToHistoryRequestSeasonX(
    val episodes: List<NetworkTraktAddToHistoryRequestEpisodeX?>,
    val number: Int?,
    val watched_at: String?
)

data class NetworkTraktAddToHistoryRequestEpisodeX(
    val number: Int?,
    val watched_at: String?
)