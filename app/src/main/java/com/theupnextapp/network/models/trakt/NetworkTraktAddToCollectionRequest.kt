package com.theupnextapp.network.models.trakt

data class NetworkTraktAddToCollectionRequest(
    val episodes: List<NetworkTraktAddToCollectionRequestEpisode>,
    val movies: List<NetworkTraktAddToCollectionRequestMovy>,
    val seasons: List<NetworkTraktAddToCollectionRequestSeason>,
    val shows: List<NetworkTraktAddToCollectionRequestShow>
)

data class NetworkTraktAddToCollectionRequestEpisode(
    val ids: NetworkTraktAddToCollectionRequestIds
)

data class NetworkTraktAddToCollectionRequestMovy(
    val collected_at: String,
    val ids: NetworkTraktAddToCollectionRequestIdsX,
    val title: String,
    val year: Int
)

data class NetworkTraktAddToCollectionRequestSeason(
    val ids: NetworkTraktAddToCollectionRequestIdsXX
)

data class NetworkTraktAddToCollectionRequestShow(
    val ids: NetworkTraktAddToCollectionRequestIdsXXX,
    val seasons: List<NetworkTraktAddToCollectionRequestSeasonX>,
    val title: String,
    val year: Int
)

data class NetworkTraktAddToCollectionRequestIds(
    val imdb: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktAddToCollectionRequestIdsX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int
)

data class NetworkTraktAddToCollectionRequestIdsXX(
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktAddToCollectionRequestIdsXXX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktAddToCollectionRequestSeasonX(
    val episodes: List<NetworkTraktAddToCollectionRequestEpisodeX>,
    val number: Int
)

data class NetworkTraktAddToCollectionRequestEpisodeX(
    val number: Int
)