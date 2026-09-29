/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
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
import androidx.room.Index
import androidx.room.PrimaryKey
import com.theupnextapp.domain.WatchHistoryItem

@Entity(
    tableName = "watch_history",
    indices = [
        Index(value = ["watchedAtEpochMillis"]),
        Index(value = ["showTraktId"]),
        Index(value = ["watchedAt"]),
    ],
)
data class DatabaseWatchHistory(
    @PrimaryKey
    val historyId: Long,
    val watchedAt: String,
    val watchedAtEpochMillis: Long,
    val showTraktId: Int,
    val episodeTraktId: Int?,
    val showTvmazeId: Int?,
    val showImdbId: String?,
    val showTitle: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeTitle: String,
    val episodeImageUrl: String?,
    val showPosterUrl: String?,
)

fun DatabaseWatchHistory.asDomainModel(): WatchHistoryItem {
    return WatchHistoryItem(
        historyId = historyId,
        watchedAt = watchedAt,
        watchedAtEpochMillis = watchedAtEpochMillis,
        showTraktId = showTraktId,
        episodeTraktId = episodeTraktId,
        showTvmazeId = showTvmazeId,
        showImdbId = showImdbId,
        showTitle = showTitle,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        episodeTitle = episodeTitle,
        episodeImageUrl = episodeImageUrl,
        showPosterUrl = showPosterUrl,
    )
}

fun List<DatabaseWatchHistory>.asDomainModel(): List<WatchHistoryItem> {
    return map { it.asDomainModel() }
}
