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
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
    val webChannel: Any?,
)

data class NetworkShowSeasonsResponseLinks(
    val self: NetworkShowSeasonsResponseSelf?,
)

data class NetworkShowSeasonsResponseImage(
    val medium: String?,
    val original: String?,
)

data class NetworkShowSeasonsResponseNetwork(
    val country: NetworkShowSeasonsResponseCountry?,
    val id: Int?,
    val name: String?,
)

data class NetworkShowSeasonsResponseSelf(
    val href: String?,
)

data class NetworkShowSeasonsResponseCountry(
    val code: String?,
    val name: String?,
    val timezone: String?,
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
            originalImageUrl = it.image?.original,
        )
    }
}
