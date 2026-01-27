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

import com.theupnextapp.domain.ShowSeasonEpisode

class NetworkTvMazeEpisodesResponse : ArrayList<NetworkTvMazeEpisodesResponseItem>()

data class NetworkTvMazeEpisodesResponseItem(
    val _links: NetworkTvMazeEpisodesResponseLinks?,
    val airdate: String?,
    val airstamp: String?,
    val airtime: String?,
    val id: Int?,
    val image: NetworkTvMazeEpisodesResponseImage?,
    val name: String?,
    val number: Int?,
    val runtime: Int?,
    val season: Int?,
    val summary: String?,
    val type: String?,
    val url: String?,
)

data class NetworkTvMazeEpisodesResponseLinks(
    val self: NetworkTvMazeEpisodesResponseSelf?,
)

data class NetworkTvMazeEpisodesResponseImage(
    val medium: String?,
    val original: String?,
)

data class NetworkTvMazeEpisodesResponseSelf(
    val href: String,
)

fun List<NetworkTvMazeEpisodesResponseItem>.asDomainModel(): List<ShowSeasonEpisode> {
    return map {
        ShowSeasonEpisode(
            id = it.id,
            name = it.name,
            season = it.season,
            number = it.number,
            runtime = it.runtime,
            originalImageUrl = it.image?.original,
            mediumImageUrl = it.image?.medium,
            summary = it.summary,
            type = it.type,
            airdate = it.airdate,
            airstamp = it.airstamp,
            airtime = it.airtime,
            imdbID = null,
        )
    }
}
