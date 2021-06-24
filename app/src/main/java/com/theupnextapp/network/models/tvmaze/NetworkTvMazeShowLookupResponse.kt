package com.theupnextapp.network.models.tvmaze

data class NetworkTvMazeShowLookupResponse(
    val _links: NetworkTvMazeShowLookupLinks?,
    val averageRuntime: Int?,
    val dvdCountry: Any?,
    val externals: NetworkTvMazeShowLookupExternals?,
    val genres: List<String>?,
    val id: Int,
    val image: NetworkTvMazeShowLookupImage,
    val language: String,
    val name: String,
    val network: NetworkTvMazeShowLookupNetwork,
    val officialSite: String,
    val premiered: String,
    val rating: NetworkTvMazeShowLookupRating,
    val runtime: Int,
    val schedule: NetworkTvMazeShowLookupSchedule,
    val status: String,
    val summary: String,
    val type: String,
    val updated: Int,
    val url: String,
    val webChannel: NetworkTvMazeShowLookupWebChannel,
    val weight: Int
)