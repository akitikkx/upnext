/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.network.models.tvmaze

import com.theupnextapp.domain.ShowCast

class NetworkShowCastResponse : ArrayList<NetworkShowCastResponseItem>()

data class NetworkShowCastResponseItem(
    val character: NetworkShowCastCharacter?,
    val person: NetworkShowCastPerson?,
    val self: Boolean?,
    val voice: Boolean?,
)

data class NetworkShowCastCharacter(
    val _links: NetworkShowCastLinks?,
    val id: Int?,
    val image: NetworkShowCastImage?,
    val name: String?,
    val url: String?,
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
    val url: String?,
)

data class NetworkShowCastLinks(
    val self: NetworkShowCastSelf?,
)

data class NetworkShowCastSelf(
    val href: String?,
)

data class NetworkShowCastLinksX(
    val self: NetworkShowCastSelfX?,
)

data class NetworkShowCastCountry(
    val code: String?,
    val name: String?,
    val timezone: String?,
)

data class NetworkShowCastImage(
    val medium: String?,
    val original: String?,
)

data class NetworkShowCastSelfX(
    val href: String?,
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
            voice = it.voice,
        )
    }
}
