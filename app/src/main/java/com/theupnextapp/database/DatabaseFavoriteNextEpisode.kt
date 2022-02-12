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
import com.theupnextapp.domain.FavoriteNextEpisode

@Entity(tableName = "favorite_next_episodes")
data class DatabaseFavoriteNextEpisode(
    @PrimaryKey
    val tvMazeID: Int?,
    val number: Int?,
    val season: Int?,
    val title: String?,
    val airStamp: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdb: String?
)

fun List<DatabaseFavoriteNextEpisode>.asDomainModel(): List<FavoriteNextEpisode> {
    return map {
        FavoriteNextEpisode(
            tvMazeID = it.tvMazeID,
            number = it.number,
            season = it.season,
            title = it.title,
            airStamp = it.airStamp,
            mediumImageUrl = it.mediumImageUrl,
            originalImageUrl = it.originalImageUrl,
            imdb = it.imdb
        )
    }
}