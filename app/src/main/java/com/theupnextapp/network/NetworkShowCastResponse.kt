package com.theupnextapp.network

import com.theupnextapp.domain.ShowCast

class NetworkShowCastResponse : ArrayList<NetworkShowCastResponseItem>()

data class NetworkShowCastResponseItem(
    val character: NetworkShowCastCharacter?,
    val person: NetworkShowCastPerson?,
    val self: Boolean?,
    val voice: Boolean?
)

data class NetworkShowCastCharacter(
    val _links: NetworkShowCastLinks?,
    val id: Int?,
    val image: NetworkShowCastImage?,
    val name: String?,
    val url: String?
)

data class NetworkShowCastPerson(
    val _links: NetworkShowCastLinksX?,
    val birthday: String?,
    val country: NetworkShowCastCountry?,
    val deathday: String?,
    val gender: String?,
    val id: Int?,
    val image: NetworkShowCastImage?,
    val name: String?,
    val url: String?
)

data class NetworkShowCastLinks(
    val self: NetworkShowCastSelf?
)

data class NetworkShowCastSelf(
    val href: String?
)

data class NetworkShowCastLinksX(
    val self: NetworkShowCastSelfX?
)

data class NetworkShowCastCountry(
    val code: String?,
    val name: String?,
    val timezone: String?
)

data class NetworkShowCastImage(
    val medium: String?,
    val original: String?
)

data class NetworkShowCastSelfX(
    val href: String?
)

fun NetworkShowCastResponse.asDomainModel(): List<ShowCast> {
    return map {
        ShowCast(
            id = it.person?.id,
            name = it.person?.name,
            country = it.person?.country?.name,
            birthday = it.person?.birthday,
            deathday = it.person?.deathday,
            gender = it.person?.gender,
            originalImageUrl = it.person?.image?.original,
            mediumImageUrl = it.person?.image?.medium,
            characterId = it.character?.id,
            characterUrl = it.character?.url,
            characterName = it.character?.name,
            characterOriginalImageUrl = it.character?.image?.original,
            characterMediumImageUrl = it.character?.image?.medium,
            self = it.self,
            voice = it.voice
        )
    }
}