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

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class NetworkTraktSyncHistoryResponse(
    val added: NetworkTraktSyncHistoryResponseAdded?,
    val deleted: NetworkTraktSyncHistoryResponseDeleted?,
    @SerializedName("not_found")
    val notFound: NetworkTraktSyncHistoryResponseNotFound?,
)

data class NetworkTraktSyncHistoryResponseAdded(
    val episodes: Int?,
)

data class NetworkTraktSyncHistoryResponseDeleted(
    val episodes: Int?,
)

data class NetworkTraktSyncHistoryResponseNotFound(
    val episodes: List<Any>?,
)

@Keep
data class NetworkTraktWatchedShowsResponse(
    val plays: Int?,
    @SerializedName("last_watched_at")
    val lastWatchedAt: String?,
    @SerializedName("last_updated_at")
    val lastUpdatedAt: String?,
    @SerializedName("reset_at")
    val resetAt: String?,
    val show: NetworkTraktWatchedShowInfo?,
    val seasons: List<NetworkTraktWatchedSeason>?,
)

@Keep
data class NetworkTraktWatchedShowInfo(
    val title: String?,
    val year: Int?,
    val ids: NetworkTraktWatchedShowIds?,
)

@Keep
data class NetworkTraktWatchedShowIds(
    val trakt: Int?,
    val slug: String?,
    val tvdb: Int?,
    val imdb: String?,
    val tmdb: Int?,
)

@Keep
data class NetworkTraktWatchedSeason(
    val number: Int?,
    val episodes: List<NetworkTraktWatchedEpisode>?,
)

@Keep
data class NetworkTraktWatchedEpisode(
    val season: Int?,
    val number: Int?,
    val title: String?,
    val plays: Int?,
    @SerializedName("last_watched_at")
    val lastWatchedAt: String?,
)
