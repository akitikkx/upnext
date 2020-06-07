package com.theupnextapp.network.models.tvmaze

import com.theupnextapp.domain.ShowSeason

class NetworkShowSeasonsResponse : ArrayList<NetworkShowSeasonsResponseItem>()

data class NetworkShowSeasonsResponseItem(
    val _links: NetworkShowSeasonsResponseLinks?,
    val endDate: String?,
    val episodeOrder: Int,
    val id: Int?,
    val image: NetworkShowSeasonsResponseImage?,
    val name: String?,
    val network: NetworkShowSeasonsResponseNetwork?,
    val number: Int?,
    val premiereDate: String?,
    val summary: String?,
    val url: String?,
    val webChannel: Any?
)

data class NetworkShowSeasonsResponseLinks(
    val self: NetworkShowSeasonsResponseSelf?
)

data class NetworkShowSeasonsResponseImage(
    val medium: String?,
    val original: String?
)

data class NetworkShowSeasonsResponseNetwork(
    val country: NetworkShowSeasonsResponseCountry?,
    val id: Int?,
    val name: String?
)

data class NetworkShowSeasonsResponseSelf(
    val href: String?
)

data class NetworkShowSeasonsResponseCountry(
    val code: String?,
    val name: String?,
    val timezone: String?
)

fun NetworkShowSeasonsResponse.asDomainModel(): List<ShowSeason> {
    return map {
        ShowSeason(
            id = it.id,
            name = it.name,
            seasonNumber = it.number,
            episodeCount = it.episodeOrder,
            premiereDate = it.premiereDate,
            endDate = it.endDate,
            mediumImageUrl = it.image?.medium,
            originalImageUrl = it.image?.original
        )
    }
}