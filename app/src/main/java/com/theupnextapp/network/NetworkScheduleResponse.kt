package com.theupnextapp.network

data class Links(
    val self: NetworkShowEpisodeSelf
)

data class Self(
    val href: String
)

data class NetworkScheduleShow(
    val _links: NetworkScheduleLinksX?,
    val externals: NetworkScheduleExternals?,
    val genres: List<String>?,
    val id: Int,
    val image: NetworkScheduleImage?,
    val language: String?,
    val name: String?,
    val network: NetworkScheduleNetwork?,
    val officialSite: String?,
    val premiered: String?,
    val rating: NetworkScheduleRating?,
    val runtime: Int?,
    val schedule: NetworkScheduleSchedule?,
    val status: String?,
    val summary: String?,
    val type: String?,
    val updated: Int?,
    val url: String?,
    val webChannel: Any?,
    val weight: Int?
)

data class NetworkScheduleLinksX(
    val nextepisode: NetworkScheduleNextEpisode,
    val previousepisode: NetworkSchedulePreviousEpisode,
    val self: NetworkScheduleNetworkSelfX
)

data class NetworkScheduleNextEpisode(
    val href: String
)

data class NetworkSchedulePreviousEpisode(
    val href: String
)

data class NetworkScheduleNetworkSelfX(
    val href: String
)

data class NetworkScheduleExternals(
    val imdb: String,
    val thetvdb: Int,
    val tvrage: Any
)

data class NetworkScheduleImage(
    val medium: String?,
    val original: String?
)

data class NetworkScheduleNetwork(
    val country: NetworkScheduleCountry,
    val id: Int,
    val name: String
)

data class NetworkScheduleCountry(
    val code: String,
    val name: String,
    val timezone: String
)

data class NetworkScheduleRating(
    val average: Any
)

data class NetworkScheduleSchedule(
    val days: List<String>,
    val time: String
)