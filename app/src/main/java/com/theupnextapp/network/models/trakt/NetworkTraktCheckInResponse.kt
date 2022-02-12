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

package com.theupnextapp.network.models.trakt

import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.domain.TraktCheckInStatus

data class NetworkTraktCheckInResponse(
    val episode: NetworkTraktCheckInResponseEpisode?,
    val id: Long?,
    val sharing: NetworkTraktCheckInResponseSharing?,
    val show: NetworkTraktCheckInResponseShow?,
    val watched_at: String?
)

data class NetworkTraktCheckInResponseEpisode(
    val ids: NetworkTraktCheckInResponseEpisodeIds?,
    val number: Int?,
    val season: Int?,
    val title: String?
)

data class NetworkTraktCheckInResponseEpisodeIds(
    val imdb: Any?,
    val tmdb: Any?,
    val trakt: Int?,
    val tvdb: Int?
)

data class NetworkTraktCheckInResponseShow(
    val ids: NetworkTraktCheckInResponseShowIds?,
    val title: String?,
    val year: Int?
)

data class NetworkTraktCheckInResponseShowIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?
)

data class NetworkTraktCheckInResponseSharing(
    val tumblr: Boolean?,
    val twitter: Boolean?
)

fun NetworkTraktCheckInResponse.asDomainModel(): TraktCheckInStatus {
    return TraktCheckInStatus(
        season = episode?.season,
        episode = episode?.number,
        checkInTime = watched_at?.let { DateUtils.getDisplayDateFromDateStamp(it).toString() },
        message = null
    )
}