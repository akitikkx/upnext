/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
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

package com.theupnextapp.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.trakt.NetworkTraktIdLookupResponse
import com.theupnextapp.network.models.trakt.NetworkTraktIdLookupResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktShowRatingResponse
import com.theupnextapp.network.models.trakt.NetworkTraktShowStatsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktIdLookupResponseItemShow
import com.theupnextapp.network.models.trakt.NetworkTraktIdLookupResponseItemShowIds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TraktRecommendationsDataSourceTest {
    private val traktService: TraktService = mock()
    private val traktDao: TraktDao = mock()
    private val upnextDao: UpnextDao = mock()
    private val tvMazeService: TvMazeService = mock()
    private val firebaseCrashlytics: FirebaseCrashlytics = mock()

    private lateinit var dataSource: TraktRecommendationsDataSource

    @Before
    fun setup() {
        dataSource =
            TraktRecommendationsDataSource(
                traktDao,
                traktService,
                upnextDao,
                tvMazeService,
                firebaseCrashlytics,
            )
    }

    @Test
    fun getTraktShowRating_success() =
        runBlocking {
            val mockResponse =
                NetworkTraktShowRatingResponse(
                    rating = 8.5,
                    votes = 1000,
                    distribution = hashMapOf("10" to 500, "1" to 10),
                )
            whenever(traktService.getShowRatingsAsync(any())).thenReturn(
                CompletableDeferred(mockResponse),
            )

            val result = dataSource.getTraktShowRating("tt1234567")

            assertTrue(result.isSuccess)
            assertEquals(8.5, result.getOrNull()?.rating)
            assertEquals(1000, result.getOrNull()?.votes)
        }

    @Test
    fun getTraktShowStats_success() =
        runBlocking {
            val mockResponse =
                NetworkTraktShowStatsResponse(
                    watchers = 500,
                    plays = 2000,
                    collectors = 300,
                    collected_episodes = 5000,
                    comments = 50,
                    lists = 10,
                    votes = 1000,
                )
            whenever(traktService.getShowStatsAsync(any())).thenReturn(
                CompletableDeferred(mockResponse),
            )

            val result = dataSource.getTraktShowStats("tt1234567")

            assertTrue(result.isSuccess)
            assertEquals(500, result.getOrNull()?.watchers)
            assertEquals(2000, result.getOrNull()?.plays)
        }

    @Test
    fun getTraktIdFromImdbId_success() =
        runBlocking {
             val mockIds = NetworkTraktIdLookupResponseItemShowIds(trakt = 12345, slug = "slug", imdb = "tt1234567", tmdb = 2)
             val mockShow = NetworkTraktIdLookupResponseItemShow(title = "Show Title", year = 2024, ids = mockIds)
             val mockResponseItem = NetworkTraktIdLookupResponseItem(type = "show", score = 1.0, show = mockShow)
             val mockResponse = NetworkTraktIdLookupResponse()
             mockResponse.add(mockResponseItem)

            whenever(traktService.idLookupAsync(eq("imdb"), eq("tt1234567"))).thenReturn(
                CompletableDeferred(mockResponse),
            )

            val result = dataSource.getTraktIdFromImdbId("tt1234567")

            assertTrue(result.isSuccess)
            assertEquals(12345, result.getOrNull())
        }
}
