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
import com.theupnextapp.network.TvMazeRateLimiter
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import com.theupnextapp.network.TmdbService
import timber.log.Timber
import java.util.concurrent.TimeUnit

abstract class BaseRepository(
    protected val upnextDao: UpnextDao,
    protected val tvMazeService: TvMazeService,
    protected val tmdbService: TmdbService,
) {
    /**
     * Modern synchronization wrapper for database tables.
     * Evaluates the interval, updates the loading state, performs the sync,
     * and logs the database table update upon success.
     */
    protected suspend fun performDatabaseSync(
        tableName: String,
        intervalMinutes: Long,
        onLoadingChange: (Boolean) -> Unit,
        performSync: suspend () -> Unit
    ) {
        val lastUpdateInfo = upnextDao.getTableLastUpdateTime(tableName)
        val lastUpdateTimeStamp = lastUpdateInfo?.last_updated

        val canProceed = if (lastUpdateTimeStamp == null || lastUpdateTimeStamp == 0L) {
            true
        } else {
            val elapsedMinutes = DateUtils.calculateDifference(
                startTimeMillis = lastUpdateTimeStamp,
                endTimeMillis = System.currentTimeMillis(),
                unit = TimeUnit.MINUTES,
            )
            elapsedMinutes >= intervalMinutes
        }

        if (canProceed) {
            onLoadingChange(true)
            try {
                performSync()
                upnextDao.deleteRecentTableUpdate(tableName)
                upnextDao.insertTableUpdateLog(
                    DatabaseTableUpdate(
                        table_name = tableName,
                        last_updated = System.currentTimeMillis(),
                    ),
                )
            } catch (e: Exception) {
                Timber.e(e, "Error performing database sync for table $tableName")
                throw e
            } finally {
                onLoadingChange(false)
            }
        } else {
            onLoadingChange(false)
        }
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
            Timber.w(
                "isUpdateNeededByDay for '$tableName': Could not format dates for comparison. Assuming update is needed to be safe."
            )
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

                val showInfo = TvMazeRateLimiter.acquire { tvMazeService.getShowSummaryAsync(tvMazeID.toString()).await() }
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
            val episodeResponse = TvMazeRateLimiter.acquire { tvMazeService.getNextEpisodeAsync(episodeId).await() }
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
                    TvMazeRateLimiter.acquire { tvMazeService.getShowLookupAsync(imdbId).await() }

                val show = showLookupResponse

                Timber.v("TVMaze show data for IMDb ID $imdbId: $show")
                Triple(show.id, show.image.original, show.image.medium)
            }
        } catch (e: HttpException) {
            Triple(null, null, null)
        } catch (e: Exception) {
            Triple(null, null, null)
        }
    }

    /**
     * Fetches original and medium image URLs for a show using its TMDB ID.
     * TVMaze ID will be returned as null since TMDB doesn't guarantee it.
     *
     * @param tmdbId The TMDB ID of the show.
     * @return A Triple containing:
     *         - First: null (for TVMaze ID)
     *         - Second: Original image URL (String?)
     *         - Third: Medium image URL (String?)
     */
    open suspend fun getTmdbImages(tmdbId: Int?): Triple<Int?, String?, String?> {
        if (tmdbId == null) {
            Timber.w("getTmdbImages called with null tmdbId.")
            return Triple(null, null, null)
        }
        return try {
            withContext(Dispatchers.IO) {
                Timber.d("Fetching TMDB info for TMDB ID: $tmdbId")
                val response = tmdbService.getShowDetailsAsync(tmdbId).await()
                
                val originalImageUrl = response.poster_path?.let { "https://image.tmdb.org/t/p/original$it" }
                val mediumImageUrl = response.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
                
                Timber.v("TMDB show image data for TMDB ID $tmdbId: original=$originalImageUrl, medium=$mediumImageUrl")
                Triple(null, originalImageUrl, mediumImageUrl)
            }
        } catch (e: HttpException) {
            Timber.e(e, "HTTP Exception fetching TMDB images for TMDB ID: $tmdbId")
            Triple(null, null, null)
        } catch (e: Exception) {
            Timber.e(e, "Exception fetching TMDB images for TMDB ID: $tmdbId")
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
