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

import com.theupnextapp.database.DatabaseTraktMostAnticipated

class NetworkTraktMostAnticipatedResponse : ArrayList<NetworkTraktMostAnticipatedResponseItem>()

data class NetworkTraktMostAnticipatedResponseItem(
    val list_count: Int?,
    val show: NetworkTraktMostAnticipatedResponseItemShow
)

data class NetworkTraktMostAnticipatedResponseItemShow(
    val ids: NetworkTraktMostAnticipatedResponseItemIds,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktMostAnticipatedResponseItemIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int,
    val tvdb: Int?,
    var tvMazeID: Int?
)

fun NetworkTraktMostAnticipatedResponseItem.asDatabaseModel(): DatabaseTraktMostAnticipated {
    return DatabaseTraktMostAnticipated(
        id = show.ids.trakt,
        title = show.title,
        year = show.year.toString(),
        medium_image_url = show.mediumImageUrl,
        original_image_url = show.originalImageUrl,
        imdbID = show.ids.imdb,
        slug = show.ids.slug,
        tmdbID = show.ids.tmdb,
        traktID = show.ids.trakt,
        tvdbID = show.ids.tvdb,
        tvMazeID = show.ids.tvMazeID,
        list_count = list_count
    )
}