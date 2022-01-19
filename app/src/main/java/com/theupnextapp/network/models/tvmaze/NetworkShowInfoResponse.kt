package com.theupnextapp.network.models.tvmaze

import com.theupnextapp.domain.ShowDetailSummary

data class NetworkShowInfoResponse constructor(
    val id: Int,
    val image: NetworkShowInfoImage?,
    val externals: NetworkShowInfoExternals,
    val genres: List<String>,
    val language: String?,
    val _links: NetworkShowInfoLinks?,
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
    val imdb: String?,
    val thetvdb: Int?,
    val tvrage: Int?
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

fun NetworkShowInfoResponse.asDomainModel(): ShowDetailSummary {
    return ShowDetailSummary(
        id = id,
        imdbID = externals.imdb,
        name = name,
        summary = summary,
        status = status,
        mediumImageUrl = image?.medium,
        originalImageUrl = image?.original,
        genres = genres.joinToString(),
        language = language,
        averageRating = rating?.average.toString(),
        airDays = schedule?.days?.joinToString(),
        time = schedule?.time,
        previousEpisodeHref = _links?.previousepisode?.href,
        nextEpisodeHref = _links?.nextepisode?.href,
        nextEpisodeLinkedId = _links?.nextepisode?.href?.substring(
            _links.nextepisode.href.lastIndexOf("/") + 1,
            _links.nextepisode.href.length
        )?.replace("/", "")?.let {
            Integer.parseInt(
                it
            )
        },
        previousEpisodeLinkedId = _links?.previousepisode?.href?.substring(
            _links.previousepisode.href.lastIndexOf("/") + 1,
            _links.previousepisode.href.length
        )?.replace("/", "")?.let {
            Integer.parseInt(
                it
            )
        }
    )
}