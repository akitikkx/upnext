/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoCountry
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoExternals
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoImage
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoNetwork
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoRating
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoSchedule
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeSelf
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
import com.theupnextapp.repository.fakes.FakeTvMazeService
import com.theupnextapp.repository.fakes.FakeUpnextDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class BaseRepositoryTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeUpnextDao: FakeUpnextDao
    private lateinit var fakeTvMazeService: FakeTvMazeService
    private lateinit var repository: ConcreteTestRepository

    // Dummy data for NetworkTvMazeShowLookupResponse
    private val dummyLookupSelfLink =
        NetworkTvMazeShowLookupSelf(href = "http://api.tvmaze.com/shows/1/self")
    private val dummyLookupPreviousEpisodeLink =
        NetworkTvMazeShowLookupPreviousepisode(href = "http://api.tvmaze.com/episodes/1")
    private val dummyLookupExternals =
        NetworkTvMazeShowLookupExternals(tvrage = 123, thetvdb = 456, imdb = "tt1234567")
    private val dummyLookupCountry =
        NetworkTvMazeShowLookupCountry(name = "US", code = "US", timezone = "America/New_York")
    private val dummyLookupCountryX =
        NetworkTvMazeShowLookupCountryX(name = "US", code = "US", timezone = "America/New_York")
    private val dummyLookupWebChannel =
        NetworkTvMazeShowLookupWebChannel(
            id = 1,
            name = "Fake Web Channel",
            country = dummyLookupCountryX,
        )
    private val dummyLookupNetwork =
        NetworkTvMazeShowLookupNetwork(id = 1, name = "Fake Network", country = dummyLookupCountry)
    private val dummyLookupRating = NetworkTvMazeShowLookupRating(average = 8.5)
    private val dummyLookupSchedule =
        NetworkTvMazeShowLookupSchedule(time = "22:00", days = listOf("Monday"))

    // Dummy data for NetworkShowInfoResponse
    private val dummyShowInfoCountry =
        NetworkShowInfoCountry(name = "US", code = "US", timezone = "America/New_York")
    private val dummyShowInfoNetwork =
        NetworkShowInfoNetwork(id = 1, name = "Fake Network", country = dummyShowInfoCountry)
    private val dummyShowInfoExternals =
        NetworkShowInfoExternals(tvrage = 123, thetvdb = 456, imdb = "tt1234567")
    private val dummyShowInfoImage =
        NetworkShowInfoImage(medium = "http://medium.jpg", original = "http://original.jpg")
    private val dummyShowInfoRating = NetworkShowInfoRating(average = 8.0)
    private val dummyShowInfoSchedule =
        NetworkShowInfoSchedule(time = "20:00", days = listOf("Sunday"))

    // Dummy data for NetworkShowNextEpisodeResponse
    private val dummyNextEpisodeSelf =
        NetworkShowNextEpisodeSelf(href = "http://api.tvmaze.com/episodes/2/self")

    @Before
    fun setUp() {
        fakeUpnextDao = FakeUpnextDao()
        fakeTvMazeService = FakeTvMazeService()
        repository = ConcreteTestRepository(fakeUpnextDao, fakeTvMazeService)
    }

    private val testTableName = "test_shows"
    private val shortIntervalMinutes = 30L

    @Test
    @Suppress("DEPRECATION")
    fun `canProceedWithUpdate should return true when no last update time exists`() {
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)
        assertTrue("Should proceed if no last update time is recorded.", canProceed)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `canProceedWithUpdate should return true when update interval has passed`() {
        val currentTime = System.currentTimeMillis()
        val lastUpdateTimeMillis =
            currentTime - ((shortIntervalMinutes + 5) * 60 * 1000)
        fakeUpnextDao.addTableUpdate(
            DatabaseTableUpdate(
                table_name = testTableName,
                last_updated = lastUpdateTimeMillis,
            ),
        )
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)
        assertTrue("Should proceed as the interval has passed.", canProceed)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `canProceedWithUpdate should return false when update interval has not passed`() {
        val currentTime = System.currentTimeMillis()
        val lastUpdateTimeMillis = currentTime - ((shortIntervalMinutes - 5) * 60 * 1000)
        fakeUpnextDao.addTableUpdate(
            DatabaseTableUpdate(
                table_name = testTableName,
                last_updated = lastUpdateTimeMillis,
            ),
        )
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)
        assertFalse("Should not proceed as the interval has not passed.", canProceed)
    }

    private fun createMockShowLookupResponse(
        id: Int,
        name: String,
        mediumImageUrl: String,
        originalImageUrl: String,
    ): NetworkTvMazeShowLookupResponse {
        return NetworkTvMazeShowLookupResponse(
            _links =
                NetworkTvMazeShowLookupLinks(
                    self = dummyLookupSelfLink,
                    previousepisode = dummyLookupPreviousEpisodeLink,
                ),
            averageRuntime = 60,
            dvdCountry = null,
            externals = dummyLookupExternals,
            genres = listOf("Drama"),
            id = id,
            image =
                NetworkTvMazeShowLookupImage(
                    medium = mediumImageUrl,
                    original = originalImageUrl,
                ),
            language = "English",
            name = name,
            network = dummyLookupNetwork,
            officialSite = "http://official.site",
            premiered = "2022-01-01",
            rating = dummyLookupRating,
            runtime = 60,
            schedule = dummyLookupSchedule,
            status = "Running",
            summary = "Summary for lookup",
            type = "Scripted",
            updated = 123456789,
            url = "http://show.url",
            webChannel = dummyLookupWebChannel,
            weight = 90,
        )
    }

    @Test
    fun `getImages with valid imdbId returns image data`() =
        runTest {
            val imdbId = "tt1234567"
            val expectedTvMazeId = 123
            val expectedOriginalUrl = "http://example.com/original.jpg"
            val expectedMediumUrl = "http://example.com/medium.jpg"
            val mockResponse =
                createMockShowLookupResponse(
                    id = expectedTvMazeId,
                    name = "Test Show",
                    mediumImageUrl = expectedMediumUrl,
                    originalImageUrl = expectedOriginalUrl,
                )
            fakeTvMazeService.mockShowLookupResponse = mockResponse

            val result = repository.getImages(imdbId)

            assertEquals(expectedTvMazeId, result.first)
            assertEquals(expectedOriginalUrl, result.second)
            assertEquals(expectedMediumUrl, result.third)
        }

    @Test
    fun `getImages with null imdbId returns null triple`() =
        runTest {
            val result = repository.getImages(null)
            assertNull(result.first)
            assertNull(result.second)
            assertNull(result.third)
        }

    @Test
    fun `getImages with blank imdbId returns null triple`() =
        runTest {
            val result = repository.getImages("   ")
            assertNull(result.first)
            assertNull(result.second)
            assertNull(result.third)
        }

    @Test
    fun `getImages when service throws 404 HttpException returns null triple`() =
        runTest {
            val imdbId = "ttNotFound"
            val responseBody = "".toResponseBody("application/json".toMediaTypeOrNull())
            fakeTvMazeService.showLookupError =
                HttpException(Response.error<Any>(404, responseBody))

            val result = repository.getImages(imdbId)
            assertNull(result.first)
            assertNull(result.second)
            assertNull(result.third)
        }

    @Test
    fun `getImages when service throws other HttpException returns null triple`() =
        runTest {
            val imdbId = "ttError"
            val responseBody = "".toResponseBody("application/json".toMediaTypeOrNull())
            fakeTvMazeService.showLookupError =
                HttpException(Response.error<Any>(500, responseBody))

            val result = repository.getImages(imdbId)
            assertNull(result.first)
            assertNull(result.second)
            assertNull(result.third)
        }
}
