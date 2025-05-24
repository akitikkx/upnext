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
import timber.log.Timber

abstract class BaseRepository(
    protected val upnextDao: UpnextDao, // Changed to protected for potential access in subclasses
    protected val tvMazeService: TvMazeService // Changed to protected
) {

    /**
     * Determines whether a data update operation can proceed based on the last update timestamp
     * for a given table and a specified interval.
     *
     * @param tableName The name of the table to check the last update time for.
     * @param intervalMinutes The minimum time in minutes that must have passed since the last update.
     * @return `true` if the update can proceed (either enough time has passed or no previous update recorded),
     *         `false` otherwise.
     */
    protected fun canProceedWithUpdate(tableName: String, intervalMinutes: Long): Boolean {
        val lastUpdateTimeStamp = upnextDao.getTableLastUpdateTime(tableName)?.last_updated
            ?: return true // No previous update time, so proceed

        val diffInMinutes = DateUtils.dateDifference(
            endTime = lastUpdateTimeStamp,
            type = DateUtils.MINUTES
        )

        val canProceed = diffInMinutes > intervalMinutes
        Timber.d("canProceedWithUpdate for table '$tableName': lastUpdate=$lastUpdateTimeStamp, diffMinutes=$diffInMinutes, interval=$intervalMinutes, proceed=$canProceed")
        return canProceed
    }

    /**
     * Fetches information about the next episode for a given TV show from the TVMaze API.
     *
     * @param tvMazeID The TVMaze ID of the show.
     * @return [NetworkShowNextEpisodeResponse] containing the next episode's details,
     *         or `null` if an error occurs or no next episode information is found.
     */
    suspend fun getNextEpisode(tvMazeID: Int): NetworkShowNextEpisodeResponse? {
        return try {
            withContext(Dispatchers.IO) { // Ensure network call is on IO dispatcher
                Timber.d("Fetching next episode for TVMaze ID: $tvMazeID")

                // Step 1: Get the show summary to find the link to the next episode
                val showInfo = tvMazeService.getShowSummaryAsync(tvMazeID.toString()).await()
                Timber.v("Show summary response for $tvMazeID: $showInfo")

                // Step 2: Extract the next episode ID from the _links
                val nextEpisodeHref = showInfo._links?.nextepisode?.href
                if (nextEpisodeHref.isNullOrBlank()) {
                    Timber.w("No nextepisode link found for TVMaze ID: $tvMazeID")
                    return@withContext null
                }

                // Robustly extract the ID from the href
                // URL (e.g., "https://api.tvmaze.com/episodes/12345")
                val nextEpisodeId = nextEpisodeHref
                    .substringAfterLast('/', "")
                if (nextEpisodeId.isBlank()) {
                    Timber.w(
                        "Could not extract next episode ID from href: " +
                                "$nextEpisodeHref for TVMaze ID: $tvMazeID"
                    )
                    return@withContext null
                }
                Timber.d(
                    "Extracted next episode ID: $nextEpisodeId for " +
                            "TVMaze ID: $tvMazeID"
                )


                // Step 3: Fetch the actual next episode details using the extracted ID
                fetchNextEpisodeDetails(nextEpisodeId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching next episode for TVMaze ID: $tvMazeID")
            null // Return null on any exception during the process
        }
    }

    /**
     * Helper function to fetch episode details given an episode ID.
     *
     * @param episodeId The ID of the episode to fetch.
     * @return [NetworkShowNextEpisodeResponse] or `null` if fetching fails or ID is invalid.
     */
    private suspend fun fetchNextEpisodeDetails(episodeId: String?):
            NetworkShowNextEpisodeResponse? {
        if (episodeId.isNullOrBlank()) {
            Timber.w("fetchNextEpisodeDetails called with null or blank episodeId.")
            return null
        }
        return try {
            Timber.d("Fetching details for episode ID: $episodeId")
            // The API endpoint seems to be just "/episodes/{id}", so no need to replace "/"
            // if it's just the ID
            val episodeResponse = tvMazeService.getNextEpisodeAsync(episodeId).await()
            Timber.v("Next episode details response for ID $episodeId: $episodeResponse")
            episodeResponse
        } catch (e: Exception) {
            Timber.e(e, "Error fetching details for episode ID: $episodeId")
            null
        }
    }
}
