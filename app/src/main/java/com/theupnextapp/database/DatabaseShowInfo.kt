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

@Entity(tableName = "shows_info")
data class DatabaseShowInfo constructor(
    @PrimaryKey
    var id: Int,
    var imdbID: String?,
    val name: String?,
    val summary: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val genres: String?,
    val language: String?,
    val averageRating: String?,
    val airDays: String?,
    val time: String?,
    val status: String?,
    val nextEpisodeLinkedId: Int?,
    val previousEpisodeLinkedId: Int?,
    val nextEpisodeId: Int?,
    val nextEpisodeAirdate: String?,
    val nextEpisodeAirstamp: String?,
    val nextEpisodeAirtime: String?,
    val nextEpisodeMediumImageUrl: String?,
    val nextEpisodeOriginalImageUrl: String?,
    val nextEpisodeName: String?,
    val nextEpisodeNumber: String?,
    val nextEpisodeRuntime: String?,
    val nextEpisodeSeason: String?,
    val nextEpisodeSummary: String?,
    val nextEpisodeUrl: String?,
    val previousEpisodeId: Int?,
    val previousEpisodeAirdate: String?,
    val previousEpisodeAirstamp: String?,
    val previousEpisodeAirtime: String?,
    val previousEpisodeMediumImageUrl: String?,
    val previousEpisodeOriginalImageUrl: String?,
    val previousEpisodeName: String?,
    val previousEpisodeNumber: String?,
    val previousEpisodeRuntime: String?,
    val previousEpisodeSeason: String?,
    val previousEpisodeSummary: String?,
    val previousEpisodeUrl: String?,
)
