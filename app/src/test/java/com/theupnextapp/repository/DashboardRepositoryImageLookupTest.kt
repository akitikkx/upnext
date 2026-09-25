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

package com.theupnextapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeImage
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountry
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountryX
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupExternals
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupImage
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupLinks
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupNetwork
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupPreviousepisode
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupRating
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupSchedule
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupSelf
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupWebChannel
import com.theupnextapp.repository.fakes.FakeCrashlytics
import com.theupnextapp.repository.fakes.FakeTvMazeDao
import com.theupnextapp.repository.fakes.FakeTvMazeService
import com.theupnextapp.repository.fakes.FakeUpnextDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardRepositoryImageLookupTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var fakeUpnextDao: FakeUpnextDao
    private lateinit var fakeTvMazeDao: FakeTvMazeDao
    private lateinit var fakeTvMazeService: FakeTvMazeService
    private lateinit var fakeCrashlytics: FakeCrashlytics

    private lateinit var repository: DashboardRepositoryImpl

    @Before
    fun setup() {
        fakeUpnextDao = FakeUpnextDao()
        fakeTvMazeDao = FakeTvMazeDao()
        fakeTvMazeService = FakeTvMazeService()
        fakeCrashlytics = FakeCrashlytics()

        fakeUpnextDao.clearAll()
        fakeTvMazeDao.clearAllData()
        fakeTvMazeService.reset()
        fakeCrashlytics.clear()

        repository =
            DashboardRepositoryImpl(
                upnextDao = fakeUpnextDao,
                tvMazeDao = fakeTvMazeDao,
                tvMazeService = fakeTvMazeService,
                firebaseCrashlytics = fakeCrashlytics,
            )
    }

    private fun createMockShowLookup(
        id: Int,
        original: String = "http://example.com/show_orig.jpg",
        medium: String = "http://example.com/show_med.jpg",
    ): NetworkTvMazeShowLookupResponse {
        return NetworkTvMazeShowLookupResponse(
            _links = NetworkTvMazeShowLookupLinks(
                self = NetworkTvMazeShowLookupSelf("http://example.com/self"),
                previousepisode = NetworkTvMazeShowLookupPreviousepisode("http://example.com/prev"),
            ),
            averageRuntime = 60,
            dvdCountry = null,
            externals = NetworkTvMazeShowLookupExternals("tt100", 1, 1),
            genres = listOf("Drama"),
            id = id,
            image = NetworkTvMazeShowLookupImage(medium = medium, original = original),
            language = "English",
            name = "Test Show $id",
            network = NetworkTvMazeShowLookupNetwork(
                id = 1,
                name = "Network",
                country = NetworkTvMazeShowLookupCountry("US", "US", "America/New_York"),
            ),
            officialSite = "",
            premiered = "2024-01-01",
            rating = NetworkTvMazeShowLookupRating(8.5),
            runtime = 60,
            schedule = NetworkTvMazeShowLookupSchedule(emptyList(), "21:00"),
            status = "Running",
            summary = "Summary",
            type = "Scripted",
            updated = 12345,
            url = "http://example.com/show",
            webChannel = NetworkTvMazeShowLookupWebChannel(
                id = 1,
                name = "Web",
                country = NetworkTvMazeShowLookupCountryX("US", "US", "America/New_York"),
            ),
            weight = 50,
        )
    }

    private fun createMockEpisodeResponse(
        id: Int,
        original: String = "http://example.com/ep_orig.jpg",
        medium: String = "http://example.com/ep_med.jpg",
    ): NetworkShowNextEpisodeResponse {
        return NetworkShowNextEpisodeResponse(
            _links = null,
            airdate = "2024-01-01",
            airstamp = "",
            airtime = "21:00",
            id = id,
            image = NetworkShowNextEpisodeImage(medium = medium, original = original),
            mediumShowImageUrl = null,
            originalShowImageUrl = null,
            tvMazeID = null,
            imdb = null,
            name = "Episode $id",
            number = 1,
            runtime = 60,
            season = 1,
            summary = "Episode summary",
            url = "http://example.com/ep",
        )
    }

    @Test
    fun `getEpisodeImageAndTvmazeId reuses cached tvmazeId from getShowImageAndTvmazeId`() = runTest {
        val imdbId = "tt99999"
        val tvmazeId = 42
        fakeTvMazeService.mockShowLookupResponse = createMockShowLookup(id = tvmazeId)
        fakeTvMazeService.mockNextEpisodeResponse = createMockEpisodeResponse(id = 101)

        // First call getShowImageAndTvmazeId to warm the cache
        val showResult = repository.getShowImageAndTvmazeId(imdbId)
        assertEquals(tvmazeId, showResult.second)

        // Clear show lookup mock to ensure getEpisodeImageAndTvmazeId DOES NOT invoke getShowLookupAsync
        fakeTvMazeService.mockShowLookupResponse = null

        val episodeResult = repository.getEpisodeImageAndTvmazeId(imdbId, 1, 1)
        assertEquals("http://example.com/ep_orig.jpg", episodeResult.first)
        assertEquals(tvmazeId, episodeResult.second)
    }

    @Test
    fun `getEpisodeImageAndTvmazeId handles network exception gracefully without crashing`() = runTest {
        val imdbId = "ttError"
        fakeTvMazeService.showLookupError = IOException("TVMaze Service Unavailable")

        val result = repository.getEpisodeImageAndTvmazeId(imdbId, 1, 1)
        assertNull(result.first)
        assertNull(result.second)
        assertEquals(1, fakeCrashlytics.getRecordedExceptions().size)
    }
}
