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

package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktUserListItem

@Entity(tableName = "favorite_shows")
data class DatabaseFavoriteShows(
    @PrimaryKey
    val id: Int?,
    val title: String?,
    val year: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?,
    var airStamp: String? = null
)

fun List<DatabaseFavoriteShows>.asDomainModel(): List<TraktUserListItem> {
    return map {
        TraktUserListItem(
            id = it.id,
            title = it.title,
            slug = it.slug,
            year = it.year,
            mediumImageUrl = it.mediumImageUrl,
            originalImageUrl = it.originalImageUrl,
            imdbID = it.imdbID,
            tmdbID = it.tmdbID,
            traktID = it.traktID,
            tvdbID = it.tvdbID,
            tvMazeID = it.tvMazeID,
            airStamp = it.airStamp
        )
    }
}

fun DatabaseFavoriteShows.asDomainModel(): TraktUserListItem {
    return TraktUserListItem(
        id = id,
        title = title,
        slug = slug,
        year = year,
        mediumImageUrl = mediumImageUrl,
        originalImageUrl = originalImageUrl,
        imdbID = imdbID,
        tmdbID = tmdbID,
        traktID = traktID,
        tvdbID = tvdbID,
        tvMazeID = tvMazeID,
        airStamp = airStamp
    )
}