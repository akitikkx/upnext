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

import com.theupnextapp.domain.ShowPreviousEpisode

data class NetworkShowPreviousEpisodeResponse constructor(
    val _links: NetworkShowPreviousEpisodeLinks?,
    val airdate: String?,
    val airstamp: String?,
    val airtime: String?,
    val id: Int,
    val image: NetworkShowPreviousEpisodeImage?,
    val name: String?,
    val number: Int,
    val runtime: Int?,
    val season: Int?,
    val summary: String?,
    val url: String?
)

data class NetworkShowPreviousEpisodeLinks(
    val self: NetworkShowPreviousEpisodeSelf
)

data class NetworkShowPreviousEpisodeSelf(
    val href: String
)

data class NetworkShowPreviousEpisodeImage(
    val medium: String,
    val original: String
)

fun NetworkShowPreviousEpisodeResponse.asDomainModel(): ShowPreviousEpisode {
    return ShowPreviousEpisode(
        previousEpisodeId = id,
        previousEpisodeUrl = url,
        previousEpisodeSummary = summary,
        previousEpisodeSeason = season.toString(),
        previousEpisodeRuntime = runtime.toString(),
        previousEpisodeNumber = number.toString(),
        previousEpisodeName = name,
        previousEpisodeMediumImageUrl = image?.medium,
        previousEpisodeOriginalImageUrl = image?.original,
        previousEpisodeAirtime = airtime,
        previousEpisodeAirstamp = airstamp,
        previousEpisodeAirdate = airdate
    )
}