package com.theupnextapp.network.models.trakt

data class NetworkTraktRemoveShowFromListRequest(
    val shows: List<NetworkTraktRemoveShowFromListRequestShow>
)

data class NetworkTraktRemoveShowFromListRequestShow(
    val ids: NetworkTraktRemoveShowFromListRequestShowIds
)

data class NetworkTraktRemoveShowFromListRequestShowIds(
    val trakt: Int
)