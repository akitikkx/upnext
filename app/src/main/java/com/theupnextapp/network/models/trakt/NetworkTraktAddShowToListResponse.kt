package com.theupnextapp.network.models.trakt

data class NetworkTraktAddShowToListResponse(
    val added: NetworkTraktAddShowToListResponseAdded,
    val existing: NetworkTraktAddShowToListResponseExisting,
    val not_found: NetworkTraktAddShowToListResponseNotFound
)

data class NetworkTraktAddShowToListResponseAdded(
    val shows: Int
)

data class NetworkTraktAddShowToListResponseExisting(
    val shows: Int
)

data class NetworkTraktAddShowToListResponseNotFound(
    val shows: List<Any>
)