package com.theupnextapp.network.models.upnext

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkRecommendedShowsResponse(val data: List<NetworkShowData>)