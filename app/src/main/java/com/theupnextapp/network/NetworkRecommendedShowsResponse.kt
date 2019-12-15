package com.theupnextapp.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkRecommendedShowsResponse(val data: List<NetworkDatum>)