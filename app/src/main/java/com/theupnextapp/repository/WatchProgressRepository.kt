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

package com.theupnextapp.repository

import com.theupnextapp.domain.ShowWatchProgress
import com.theupnextapp.domain.WatchedEpisode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WatchProgressRepository {
    val isSyncing: StateFlow<Boolean>
    val syncError: StateFlow<String?>

    suspend fun markEpisodeWatched(
        showTraktId: Int,
        showTvMazeId: Int?,
        showImdbId: String?,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Result<Unit>

    suspend fun markEpisodeUnwatched(
        showTraktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Result<Unit>

    fun getWatchedEpisodesForShow(showTraktId: Int): Flow<List<WatchedEpisode>>

    suspend fun isEpisodeWatched(
        showTraktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Boolean

    suspend fun getShowWatchProgress(
        showTraktId: Int,
        totalEpisodes: Int,
    ): ShowWatchProgress

    suspend fun syncWithTrakt(token: String): Result<Unit>

    suspend fun refreshWatchedFromTrakt(
        token: String,
        showTraktId: Int? = null,
    ): Result<Unit>
}
