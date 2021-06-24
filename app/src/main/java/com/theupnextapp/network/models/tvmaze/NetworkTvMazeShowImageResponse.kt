package com.theupnextapp.network.models.tvmaze

class NetworkTvMazeShowImageResponse : ArrayList<NetworkTvMazeShowImageResponseItem>()

data class NetworkTvMazeShowImageResponseItem(
    val id: Int,
    val main: Boolean,
    val resolutions: NetworkTvMazeShowImageResolutions,
    val type: String
)