package com.theupnextapp.network

import java.util.ArrayList

data class NetworkRecommendedShows(
    var code: Int?,
    val status: Int?,
    val message: Any?,
    val data: List<NetworkShowData> = ArrayList()
)