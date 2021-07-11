package com.theupnextapp.network.models.trakt

data class NetworkTraktRemoveShowFromListResponse(
    val deleted: NetworkTraktRemoveShowFromListResponseDeleted,
    val not_found: NetworkTraktRemoveShowFromListResponseNotFound
)

data class NetworkTraktRemoveShowFromListResponseDeleted(
    val shows: Int
)

data class NetworkTraktRemoveShowFromListResponseNotFound(
    val shows: List<Any>
)