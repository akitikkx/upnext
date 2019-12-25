package com.theupnextapp.network

data class NetworkShowInfoResponse constructor(
    val _links: NetworkShowInfoLinks?,
    val externals: NetworkShowInfoExternals,
    val genres: List<String>,
    val id: Int,
    val image: NetworkShowInfoImage?,
    val language: String?,
    val name: String?,
    val network: NetworkShowInfoNetwork?,
    val officialSite: String?,
    val premiered: String?,
    val rating: NetworkShowInfoRating?,
    val runtime: Int,
    val schedule: NetworkShowInfoSchedule?,
    val status: String?,
    val summary: String?,
    val type: String?,
    val updated: Int,
    val url: String?,
    val webChannel: Any,
    val weight: Int
)

data class NetworkShowInfoLinks(
    val nextepisode: NetworkShowInfoNextEpisode?,
    val previousepisode: NetworkShowInfoPreviousEpsiode?,
    val self: NetworkShowInfoSelf?
)

data class NetworkShowInfoPreviousEpsiode(
    val href: String
)

data class NetworkShowInfoNextEpisode(
    val href: String
)

data class NetworkShowInfoSelf(
    val href: String
)

data class NetworkShowInfoExternals(
    val imdb: String,
    val thetvdb: Int,
    val tvrage: Int
)

data class NetworkShowInfoImage(
    val medium: String,
    val original: String
)

data class NetworkShowInfoNetwork(
    val country: NetworkShowInfoCountry,
    val id: Int,
    val name: String
)

data class NetworkShowInfoCountry(
    val code: String,
    val name: String,
    val timezone: String
)

data class NetworkShowInfoRating(
    val average: Double
)

data class NetworkShowInfoSchedule(
    val days: List<String>,
    val time: String
)