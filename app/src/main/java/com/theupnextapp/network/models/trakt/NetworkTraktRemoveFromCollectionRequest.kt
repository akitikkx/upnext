package com.theupnextapp.network.models.trakt

data class NetworkTraktRemoveFromCollectionRequest(
    val episodes: List<NetworkTraktRemoveFromCollectionRequestEpisode>,
    val movies: List<NetworkTraktRemoveFromCollectionRequestMovy>,
    val seasons: List<NetworkTraktRemoveFromCollectionRequestSeason>,
    val shows: List<NetworkTraktRemoveFromCollectionRequestShow>
)

data class NetworkTraktRemoveFromCollectionRequestEpisode(
    val ids: NetworkTraktRemoveFromCollectionRequestIds
)

data class NetworkTraktRemoveFromCollectionRequestMovy(
    val ids: NetworkTraktRemoveFromCollectionRequestIdsX,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveFromCollectionRequestSeason(
    val ids: NetworkTraktRemoveFromCollectionRequestIdsXX
)

data class NetworkTraktRemoveFromCollectionRequestShow(
    val ids: NetworkTraktRemoveFromCollectionRequestIdsXXX,
    val seasons: List<NetworkTraktRemoveFromCollectionRequestSeasonX>,
    val title: String,
    val year: Int
)

data class NetworkTraktRemoveFromCollectionRequestIds(
    val imdb: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromCollectionRequestIdsX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int
)

data class NetworkTraktRemoveFromCollectionRequestIdsXX(
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromCollectionRequestIdsXXX(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)

data class NetworkTraktRemoveFromCollectionRequestSeasonX(
    val episodes: List<NetworkTraktRemoveFromCollectionRequestEpisodeX>,
    val number: Int
)

data class NetworkTraktRemoveFromCollectionRequestEpisodeX(
    val number: Int
)