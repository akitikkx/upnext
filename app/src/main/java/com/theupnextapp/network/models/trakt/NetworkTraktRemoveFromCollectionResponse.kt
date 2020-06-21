package com.theupnextapp.network.models.trakt

data class NetworkTraktRemoveFromCollectionResponse(
    val deleted: NetworkTraktRemoveFromCollectionResponseDeleted,
    val not_found: NetworkTraktRemoveFromCollectionResponseNotFound
)

data class NetworkTraktRemoveFromCollectionResponseDeleted(
    val episodes: Int,
    val movies: Int
)

data class NetworkTraktRemoveFromCollectionResponseNotFound(
    val episodes: List<Any>,
    val movies: List<NetworkTraktRemoveFromCollectionResponseMovy>,
    val seasons: List<Any>,
    val shows: List<Any>
)

data class NetworkTraktRemoveFromCollectionResponseMovy(
    val ids: NetworkTraktRemoveFromCollectionResponseIds
)

data class NetworkTraktRemoveFromCollectionResponseIds(
    val imdb: String
)