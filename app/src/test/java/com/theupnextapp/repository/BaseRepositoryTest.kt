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

import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupExternals
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupImage
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupLinks
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupNetwork
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupRating
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupSchedule
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupWebChannel
import com.theupnextapp.repository.fakes.FakeUpnextDao
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class BaseRepositoryTest {

    private lateinit var fakeUpnextDao: FakeUpnextDao

    @Mock
    private lateinit var mockTvMazeService: TvMazeService

    private lateinit var repository: ConcreteTestRepository

    private val dummyLinks: NetworkTvMazeShowLookupLinks = mock()
    private val dummyExternals: NetworkTvMazeShowLookupExternals = mock()
    private val dummyNetwork: NetworkTvMazeShowLookupNetwork = mock()
    private val dummyRating: NetworkTvMazeShowLookupRating = mock()
    private val dummySchedule: NetworkTvMazeShowLookupSchedule = mock()
    private val dummyWebChannel: NetworkTvMazeShowLookupWebChannel = mock()

    @Before
    fun setUp() {
        fakeUpnextDao = FakeUpnextDao()
        repository = ConcreteTestRepository(fakeUpnextDao, mockTvMazeService)
    }

    private val testTableName = "test_shows"
    private val shortIntervalMinutes = 30L

    @Test
    fun `canProceedWithUpdate should return true when no last update time exists`() {
        // Act
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)

        assertTrue(
            "Should proceed if no last update time is recorded.", canProceed
        )
    }

    @Test
    fun `canProceedWithUpdate should return true when update interval has passed`() {
        // Arrange
        val currentTime = System.currentTimeMillis()
        // Make sure the interval has more than passed
        val lastUpdateTimeMillis =
            currentTime - ((shortIntervalMinutes + 5) * 60 * 1000) // Added a bit more buffer
        fakeUpnextDao.addTableUpdate(
            DatabaseTableUpdate(
                table_name = testTableName,
                last_updated = lastUpdateTimeMillis
            )
        )

        // Act
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)

        // Assert
        assertTrue("Should proceed as the interval has passed.", canProceed)
    }

    @Test
    fun `canProceedWithUpdate should return false when update interval has not passed`() {
        // Arrange
        val currentTime = System.currentTimeMillis()
        // Last update was very recent, less than the interval
        val lastUpdateTimeMillis = currentTime - ((shortIntervalMinutes - 5) * 60 * 1000)
        fakeUpnextDao.addTableUpdate(
            DatabaseTableUpdate(
                table_name = testTableName,
                last_updated = lastUpdateTimeMillis
            )
        )

        // Act
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)

        // Assert
        Assert.assertFalse(
            "Should not proceed as the interval has not passed.",
            canProceed
        )
    }

    // Helper to create mock responses.
    // Since 'image', 'image.medium', and 'image.original' are non-nullable,
    // they must always be provided for a successful response mock.
    private fun createMockShowResponse(
        id: Int,
        name: String,
        mediumImageUrl: String,
        originalImageUrl: String,
        // Provide defaults or mocks for other non-nullable complex types
        _links: NetworkTvMazeShowLookupLinks? = dummyLinks, // Nullable fields can remain as is
        averageRuntime: Int? = 0,
        dvdCountry: Any? = null,
        externals: NetworkTvMazeShowLookupExternals? = dummyExternals,
        genres: List<String>? = emptyList(),
        language: String = "English", // Non-nullable
        network: NetworkTvMazeShowLookupNetwork = dummyNetwork, // Non-nullable
        officialSite: String = "", // Non-nullable
        premiered: String = "", // Non-nullable
        rating: NetworkTvMazeShowLookupRating = dummyRating, // Non-nullable
        runtime: Int = 0, // Non-nullable
        schedule: NetworkTvMazeShowLookupSchedule = dummySchedule, // Non-nullable
        status: String = "", // Non-nullable
        summary: String = "", // Non-nullable
        type: String = "", // Non-nullable
        updated: Int = 0, // Non-nullable
        url: String = "", // Non-nullable
        webChannel: NetworkTvMazeShowLookupWebChannel = dummyWebChannel, // Non-nullable
        weight: Int = 0 // Non-nullable
    ): NetworkTvMazeShowLookupResponse {
        return NetworkTvMazeShowLookupResponse(
            _links = _links,
            averageRuntime = averageRuntime,
            dvdCountry = dvdCountry,
            externals = externals,
            genres = genres,
            id = id,
            image = NetworkTvMazeShowLookupImage(
                medium = mediumImageUrl,
                original = originalImageUrl
            ),
            language = language,
            name = name,
            network = network,
            officialSite = officialSite,
            premiered = premiered,
            rating = rating,
            runtime = runtime,
            schedule = schedule,
            status = status,
            summary = summary,
            type = type,
            updated = updated,
            url = url,
            webChannel = webChannel,
            weight = weight
        )
    }

    @Test
    fun `getImages with valid imdbId returns image data`() = runTest {
        // Arrange
        val imdbId = "tt1234567"
        val expectedTvMazeId = 123
        val expectedOriginalUrl = "http://example.com/original.jpg"
        val expectedMediumUrl = "http://example.com/medium.jpg"

        val mockResponse = createMockShowResponse(
            id = expectedTvMazeId,
            name = "Test Show",
            mediumImageUrl = expectedMediumUrl,
            originalImageUrl = expectedOriginalUrl
        )

        whenever(mockTvMazeService.getShowLookupAsync(imdbId))
            .thenReturn(CompletableDeferred(mockResponse))

        // Act
        val result = repository.getImages(imdbId)

        // Assert
        assertEquals(expectedTvMazeId, result.first)
        assertEquals(expectedOriginalUrl, result.second)
        assertEquals(expectedMediumUrl, result.third)
        // Optionally, assert that they are not null if that adds value to the test's intent
        assertNotNull(result.first)
        assertNotNull(result.second)
        assertNotNull(result.third)
    }

    @Test
    fun `getImages with null imdbId returns null triple`() = runTest {
        // Act
        val result = repository.getImages(null)
        // Assert
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages with blank imdbId returns null triple`() = runTest {
        // Act
        val result = repository.getImages("   ")
        // Assert
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages when service throws 404 HttpException returns null triple`() = runTest {
        // Arrange
        val imdbId = "ttNotFound"
        val responseBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        val mockHttpException =
            HttpException(
                Response.error<Any>(
                    404,
                    responseBody
                )
            )

        whenever(mockTvMazeService.getShowLookupAsync(any()))
            .doSuspendableAnswer { throw mockHttpException }

        // Act
        val result = repository.getImages(imdbId)

        // Assert
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages when service throws other HttpException returns null triple`() = runTest {
        // Arrange
        val imdbId = "ttError"
        val responseBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        val mockHttpException = HttpException(Response.error<Any>(500, responseBody))

        whenever(mockTvMazeService.getShowLookupAsync(any()))
            .doSuspendableAnswer { throw mockHttpException }

        // Act
        val result = repository.getImages(imdbId)

        // Assert
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages when service throws generic Exception returns null triple`() = runTest {
        // Arrange
        val imdbId = "ttGenericError"
        val genericException = RuntimeException("Network issue")

        whenever(mockTvMazeService.getShowLookupAsync(any()))
            .doSuspendableAnswer { throw genericException }

        // Act
        val result = repository.getImages(imdbId)

        // Assert
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

}