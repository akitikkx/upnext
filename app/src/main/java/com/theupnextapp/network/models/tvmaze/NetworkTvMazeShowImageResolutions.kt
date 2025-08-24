package com.theupnextapp.network.models.tvmaze

data class NetworkTvMazeShowImageResolutions(
    val original: NetworkTvMazeShowImageOriginal,
    val medium: NetworkTvMazeShowImageMedium?, // Making it nullable to be safe
)
