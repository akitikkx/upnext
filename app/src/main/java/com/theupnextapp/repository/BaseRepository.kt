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

import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository(
    private val upnextDao: UpnextDao,
    private val tvMazeService: TvMazeService
) {

    /**
     * Determine whether this particular update request may proceed
     * based on the last update timestamp
     */
    protected fun canProceedWithUpdate(tableName: String, intervalMinutes: Long): Boolean {
        var canProceedWithUpdate = false

        val lastUpdateTime =
            upnextDao.getTableLastUpdateTime(tableName)

        val diffInMinutes = lastUpdateTime?.last_updated?.let {
            DateUtils.dateDifference(
                endTime = it,
                type = DateUtils.MINUTES
            )
        }
        if (diffInMinutes != null) {
            if (diffInMinutes > intervalMinutes) {
                canProceedWithUpdate = true
            }
        }

        if (diffInMinutes == null) {
            canProceedWithUpdate = true
        }

        return canProceedWithUpdate
    }

    suspend fun getNextEpisode(tvMazeID: Int): NetworkShowNextEpisodeResponse? {
        return try {
            withContext(Dispatchers.IO) {
                val showInfo =
                    tvMazeService.getShowSummaryAsync(tvMazeID.toString()).await()

                val nextEpisodeLink = showInfo._links?.nextepisode?.href?.substring(
                    showInfo._links.nextepisode.href.lastIndexOf("/") + 1,
                    showInfo._links.nextepisode.href.length
                )?.replace("/", "")

                getNextEpisodeInfo(nextEpisodeLink)
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getNextEpisodeInfo(nextEpisodeLink: String?): NetworkShowNextEpisodeResponse? {
        var showNextEpisode: NetworkShowNextEpisodeResponse? = null
        if (!nextEpisodeLink.isNullOrEmpty()) {
            showNextEpisode = tvMazeService.getNextEpisodeAsync(
                nextEpisodeLink.replace("/", "")
            ).await()
        }
        return showNextEpisode
    }
}
