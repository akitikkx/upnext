package com.theupnextapp.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkNewShowsResponse(val data: List<NetworkShowData>)