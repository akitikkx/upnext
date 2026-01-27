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

data class NetworkShowEpisodeLinks(
    val self: NetworkShowNextEpisodeSelf,
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
    val weight: Int?,
)

data class NetworkScheduleLinksX(
    val nextepisode: NetworkScheduleNextEpisode,
    val previousepisode: NetworkSchedulePreviousEpisode,
    val self: NetworkScheduleNetworkSelfX,
)

data class NetworkScheduleNextEpisode(
    val href: String,
)

data class NetworkSchedulePreviousEpisode(
    val href: String,
)

data class NetworkScheduleNetworkSelfX(
    val href: String,
)

data class NetworkScheduleExternals(
    val imdb: String,
    val thetvdb: Int,
    val tvrage: Any,
)

data class NetworkScheduleImage(
    var medium: String?,
    var original: String?,
)

data class NetworkScheduleNetwork(
    val country: NetworkScheduleCountry,
    val id: Int,
    val name: String,
)

data class NetworkScheduleCountry(
    val code: String,
    val name: String,
    val timezone: String,
)

data class NetworkScheduleRating(
    val average: Any,
)

data class NetworkScheduleSchedule(
    val days: List<String>,
    val time: String,
)
