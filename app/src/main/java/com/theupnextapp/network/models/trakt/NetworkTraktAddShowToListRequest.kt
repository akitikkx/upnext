package com.theupnextapp.network.models.trakt

data class NetworkTraktAddShowToListRequest(
    val shows: List<NetworkTraktAddShowToListRequestShow>
)

data class NetworkTraktAddShowToListRequestShow(
    val ids: NetworkTraktAddShowToListRequestShowIds
)

data class NetworkTraktAddShowToListRequestShowIds(
    val trakt: Int
)