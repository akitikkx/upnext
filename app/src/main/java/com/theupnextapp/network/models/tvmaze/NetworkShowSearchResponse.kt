package com.theupnextapp.network.models.tvmaze

import com.theupnextapp.domain.ShowSearch

data class NetworkShowSearchResponse(
    val score: Double,
    val show: NetworkShowSearchResponseShow
)

data class NetworkShowSearchResponseShow(
    val _links: NetworkShowSearchReponseLinks,
    val externals: Externals,
    val genres: List<String?>,
    val id: Int,
    val image: NetworkShowSearchResponseImage?,
    val language: String?,
    val name: String?,
    val network: Network,
    val officialSite: String?,
    val premiered: String?,
    val rating: Rating,
    val runtime: Int,
    val schedule: Schedule,
    val status: String?,
    val summary: String?,
    val type: String?,
    val updated: Int,
    val url: String?,
    val webChannel: Any,
    val weight: Int
)

data class NetworkShowSearchReponseLinks(
    val previousepisode: NetworkShowSearchResponsePreviousepisode,
    val self: NetworkShowSearchResponseSelf
)

data class NetworkShowSearchResponsePreviousepisode(
    val href: String?
)

data class NetworkShowSearchResponseSelf(
    val href: String?
)

data class Externals(
    val imdb: String,
    val thetvdb: Int,
    val tvrage: Any
)

data class NetworkShowSearchResponseImage(
    val medium: String?,
    val original: String?
)

data class Network(
    val country: NetworkShowCastCountry?,
    val id: Int,
    val name: String?
)

data class Country(
    val code: String,
    val name: String,
    val timezone: String
)

data class Rating(
    val average: Double
)

data class Schedule(
    val days: List<String>,
    val time: String
)

fun List<NetworkShowSearchResponse>.asDomainModel(): List<ShowSearch> {
    return map {
        ShowSearch(
            genres = it.show.genres.joinToString(),
            id = it.show.id,
            name = it.show.name,
            mediumImageUrl = it.show.image?.medium,
            originalImageUrl = it.show.image?.original,
            language = it.show.language,
            premiered = it.show.premiered,
            runtime = it.show.runtime.toString(),
            status = it.show.status,
            summary = it.show.summary,
            type = it.show.type,
            updated = it.show.updated.toString()
        )
    }
}