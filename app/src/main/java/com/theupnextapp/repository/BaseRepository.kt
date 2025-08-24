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
import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

abstract class BaseRepository(
    protected val upnextDao: UpnextDao,
    protected val tvMazeService: TvMazeService,
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
    @Deprecated(message = "This will be replaced with a more robust update mechanism in the future.")
    protected fun canProceedWithUpdate(
        tableName: String,
        intervalMinutes: Long,
    ): Boolean {
        val lastUpdateTimeStamp =
            upnextDao.getTableLastUpdateTime(tableName)?.last_updated
                ?: return true

        val elapsedMinutes =
            DateUtils.calculateDifference(
                startTimeMillis = lastUpdateTimeStamp,
                endTimeMillis = System.currentTimeMillis(),
                unit = TimeUnit.MINUTES,
            )

        val canProceed = elapsedMinutes >= intervalMinutes
        Timber.d(
            "canProceedWithUpdate for table '$tableName': lastUpdate=$lastUpdateTimeStamp, elapsedMinutes=$elapsedMinutes, interval=$intervalMinutes, proceed=$canProceed",
        )
        return canProceed
    }

    /**
     * Checks if an update is needed for a given table.
     * An update is needed if:
     * 1. There's no previous update timestamp (initial load).
     * 2. The last update was on a different day than the current day.
     *
     * @param tableName The name of the table.
     * @return true if an update is needed, false otherwise.
     */

    /**
     * Checks if an update is needed for a given table.
     * An update is needed if:
     * 1. There's no previous update timestamp (initial load).
     * 2. The last update was on a different day than the current day (compared using "yyyy-MM-dd" format).
     *
     * @param tableName The name of the table.
     * @return true if an update is needed, false otherwise.
     */
    protected suspend fun isUpdateNeededByDay(tableName: String): Boolean {
        val lastUpdateInfo = upnextDao.getTableLastUpdateTime(tableName)
        val lastUpdateTimeStamp = lastUpdateInfo?.last_updated

        if (lastUpdateTimeStamp == null || lastUpdateTimeStamp == 0L) {
            Timber.d("isUpdateNeededByDay for '$tableName': true (no previous update).")
            return true // No previous update time, so proceed
        }

        // Use DateUtils to format timestamps to "yyyy-MM-dd" strings for comparison
        val lastUpdateDateString = DateUtils.formatTimestampToString(lastUpdateTimeStamp, "yyyy-MM-dd")
        val currentDateString = DateUtils.formatTimestampToString(System.currentTimeMillis(), "yyyy-MM-dd")

        if (lastUpdateDateString == null || currentDateString == null) {
            Timber.w("isUpdateNeededByDay for '$tableName': Could not format dates for comparison. Assuming update is needed to be safe.")
            return true
        }

        val isDifferentDay = lastUpdateDateString != currentDateString

        Timber.d(
            "isUpdateNeededByDay for '$tableName': lastUpdateDateString='$lastUpdateDateString', " +
                "currentDateString='$currentDateString', isDifferentDay=$isDifferentDay.",
        )
        return isDifferentDay
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

                val showInfo = tvMazeService.getShowSummaryAsync(tvMazeID.toString()).await()
                Timber.v("Show summary response for $tvMazeID: $showInfo")

                val nextEpisodeHref = showInfo._links?.nextepisode?.href
                if (nextEpisodeHref.isNullOrBlank()) {
                    Timber.w("No nextepisode link found for TVMaze ID: $tvMazeID")
                    return@withContext null
                }

                val nextEpisodeId =
                    nextEpisodeHref
                        .substringAfterLast('/', "")
                if (nextEpisodeId.isBlank()) {
                    Timber.w(
                        "Could not extract next episode ID from href: " +
                            "$nextEpisodeHref for TVMaze ID: $tvMazeID",
                    )
                    return@withContext null
                }
                Timber.d(
                    "Extracted next episode ID: $nextEpisodeId for " +
                        "TVMaze ID: $tvMazeID",
                )

                fetchNextEpisodeDetails(nextEpisodeId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching next episode for TVMaze ID: $tvMazeID")
            null
        }
    }

    /**
     * Helper function to fetch episode details given an episode ID.
     *
     * @param episodeId The ID of the episode to fetch.
     * @return [NetworkShowNextEpisodeResponse] or `null` if fetching fails or ID is invalid.
     */
    private suspend fun fetchNextEpisodeDetails(episodeId: String?): NetworkShowNextEpisodeResponse? {
        if (episodeId.isNullOrBlank()) {
            Timber.w("fetchNextEpisodeDetails called with null or blank episodeId.")
            return null
        }
        return try {
            Timber.d("Fetching details for episode ID: $episodeId")
            val episodeResponse = tvMazeService.getNextEpisodeAsync(episodeId).await()
            Timber.v("Next episode details response for ID $episodeId: $episodeResponse")
            episodeResponse
        } catch (e: Exception) {
            Timber.e(e, "Error fetching details for episode ID: $episodeId")
            null
        }
    }

    /**
     * Fetches TVMaze ID and image URLs for a show using its IMDb ID.
     *
     * @param imdbId The IMDb ID of the show.
     * @return A Triple containing:
     *         - First: TVMaze ID (Int?)
     *         - Second: Original image URL (String?)
     *         - Third: Medium image URL (String?)
     *         Returns (null, null, null) if the show is not found or an error occurs.
     */
    open suspend fun getImages(imdbId: String?): Triple<Int?, String?, String?> {
        if (imdbId.isNullOrBlank()) {
            Timber.w("getImages called with null or blank imdbId.")
            return Triple(null, null, null)
        }
        return try {
            withContext(Dispatchers.IO) {
                Timber.d("Fetching TVMaze info for IMDb ID: $imdbId")
                val showLookupResponse: NetworkTvMazeShowLookupResponse =
                    tvMazeService.getShowLookupAsync(imdbId).await()

                val show = showLookupResponse

                Timber.v("TVMaze show data for IMDb ID $imdbId: $show")
                Triple(show.id, show.image.original, show.image.medium)
            }
        } catch (e: retrofit2.HttpException) {
            Triple(null, null, null)
        } catch (e: Exception) {
            Triple(null, null, null)
        }
    }

    protected suspend fun logTableUpdateTimestamp(tableName: String) {
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Attempting to insertTableUpdateLog for $tableName")
                upnextDao.insertTableUpdateLog(
                    DatabaseTableUpdate(
                        table_name = tableName,
                        last_updated = System.currentTimeMillis(),
                    ),
                )
                Timber.d("Successfully insertedTableUpdateLog for $tableName at ${System.currentTimeMillis()}")
            } catch (e: Exception) {
                Timber.e(e, "Error inserting table update log for $tableName in DAO")
            }
        }
    }
}
