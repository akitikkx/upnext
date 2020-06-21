package com.theupnextapp.network.models.trakt

data class NetworkTraktAddToTraktResponse(
    val added: NetworkTraktAddToTraktResponseAdded,
    val existing: NetworkTraktAddToTraktResponseExisting,
    val not_found: NetworkTraktAddToTraktResponseNotFound,
    val updated: NetworkTraktAddToTraktResponseUpdated
)

data class NetworkTraktAddToTraktResponseAdded(
    val episodes: Int,
    val movies: Int
)

data class NetworkTraktAddToTraktResponseExisting(
    val episodes: Int,
    val movies: Int
)

data class NetworkTraktAddToTraktResponseNotFound(
    val episodes: List<Any>,
    val movies: List<NetworkTraktAddToTraktResponseMovy>,
    val seasons: List<Any>,
    val shows: List<Any>
)

data class NetworkTraktAddToTraktResponseUpdated(
    val episodes: Int,
    val movies: Int
)

data class NetworkTraktAddToTraktResponseMovy(
    val ids: NetworkTraktAddToTraktResponseIds
)

data class NetworkTraktAddToTraktResponseIds(
    val imdb: String
)