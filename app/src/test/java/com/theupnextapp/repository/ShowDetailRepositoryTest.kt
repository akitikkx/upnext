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

import com.theupnextapp.fake.FakeFirebaseCrashlytics
import com.theupnextapp.fake.FakeTvMazeService
import com.theupnextapp.fake.FakeUpnextDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoImage
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoRating
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoSchedule
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoSelf
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoExternals
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoNetwork
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoCountry
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeImage
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeSelf
import java.io.IOException

@ExperimentalCoroutinesApi
class ShowDetailRepositoryTest {

    private lateinit var fakeUpnextDao: FakeUpnextDao
    private lateinit var fakeTvMazeService: FakeTvMazeService
    private lateinit var fakeFirebaseCrashlytics: FakeFirebaseCrashlytics

    private lateinit var showDetailRepository: ShowDetailRepository

    @Before
    fun setUp() {
        fakeUpnextDao = FakeUpnextDao()
        fakeTvMazeService = FakeTvMazeService()
        fakeFirebaseCrashlytics = FakeFirebaseCrashlytics()
        fakeFirebaseCrashlytics.clear()

        showDetailRepository = ShowDetailRepository(
            upnextDao = fakeUpnextDao,
            tvMazeService = fakeTvMazeService,
            crashlytics = fakeFirebaseCrashlytics
        )
    }

    @Test
    fun `getShowSummary emits Loading then Success when network call is successful`() = runTest {
        val showId = 123
        val fakeNetworkResponse = NetworkShowInfoResponse(
            id = showId,
            url = "http://fakeurl.com/show/$showId",
            name = "Fake Show Title",
            type = "Scripted",
            language = "English",
            genres = listOf("Drama", "Sci-Fi"),
            status = "Running",
            runtime = 60,
            premiered = "2023-01-01",
            officialSite = "http://fakeofficial.com",
            schedule = NetworkShowInfoSchedule(time = "20:00", days = listOf("Monday")),
            rating = NetworkShowInfoRating(average = 8.5),
            weight = 100,
            network = NetworkShowInfoNetwork(
                id = 1,
                name = "Fake Network",
                country = NetworkShowInfoCountry(name = "Fake Country", code = "FC", timezone = "Fake/Timezone")
            ),
            webChannel = Any(),
            externals = NetworkShowInfoExternals(tvrage = 0, thetvdb = 0, imdb = "tt1234567"),
            image = NetworkShowInfoImage(medium = "http://fakeimage.com/medium.jpg", original = "http://fakeimage.com/original.jpg"),
            summary = "This is a fake show summary.",
            updated = (System.currentTimeMillis() / 1000L).toInt(),
            _links = NetworkShowInfoLinks(self = NetworkShowInfoSelf(href = "http://fakeurl.com/show/$showId/self"), nextepisode = null, previousepisode = null)
        )
        fakeTvMazeService.mockShowInfoResponse = fakeNetworkResponse
        fakeTvMazeService.shouldThrowGetShowSummaryError = false

        val results = showDetailRepository.getShowSummary(showId).toList()

        assertTrue("Initial Loading state (true) not found or incorrect", results.any { it is Result.Loading && it.status })
        val successResult = results.firstOrNull { it is Result.Success } as? Result.Success<ShowDetailSummary>
        assertNotNull("Success result was not found", successResult)
        assertEquals("Show ID mismatch", showId, successResult?.data?.id)
        assertEquals("Show name mismatch", "Fake Show Title", successResult?.data?.name)
        assertTrue("Final Loading state (false) not found or incorrect", results.any { it is Result.Loading && !it.status })
    }

    @Test
    fun `getShowSummary emits Error and logs to Crashlytics when network call fails`() = runTest {
        val showId = 456
        fakeTvMazeService.shouldThrowGetShowSummaryError = true

        val results = showDetailRepository.getShowSummary(showId).toList()

        assertTrue("Initial Loading state (true) not found or incorrect", results.any { it is Result.Loading && it.status })
        val networkErrorResult = results.firstOrNull { it is Result.NetworkError } as? Result.NetworkError
        assertNotNull("NetworkError result was not found", networkErrorResult)
        assertTrue("NetworkError's exception type is not IOException", networkErrorResult?.exception is IOException)
        assertEquals("Fake network error for getShowSummary", networkErrorResult?.exception?.message)
        assertTrue("Final Loading state (false) not found or incorrect", results.any { it is Result.Loading && !it.status })
        assertEquals("Crashlytics should have recorded one exception", 1, fakeFirebaseCrashlytics.getRecordedExceptions().size)
        assertTrue("Recorded exception is not IOException", fakeFirebaseCrashlytics.getRecordedExceptions()[0] is IOException)
    }

    @Test
    fun `getPreviousEpisode emits Loading then Success when network call is successful`() = runTest {
        // GIVEN: Configure FakeTvMazeService for success
        val episodeRef = "http://api.tvmaze.com/episodes/12345"
        val previousEpisodeId = 12345
        val fakeNetworkPreviousEpisodeResponse = NetworkShowPreviousEpisodeResponse(
            id = previousEpisodeId,
            url = "http://fakeurl.com/episode/$previousEpisodeId",
            name = "Fake Previous Episode Title",
            season = 1,
            number = 1,
            airdate = "2023-01-01",
            airtime = "20:00",
            airstamp = "2023-01-01T20:00:00Z",
            runtime = 30,
            image = NetworkShowPreviousEpisodeImage(medium = "http://fakeimage.com/medium.jpg", original = "http://fakeimage.com/original.jpg"),
            summary = "This is a fake previous episode summary.",
            _links = NetworkShowPreviousEpisodeLinks(self = NetworkShowPreviousEpisodeSelf(href = "http://fakeurl.com/episode/$previousEpisodeId/self"))
        )
        fakeTvMazeService.mockPreviousEpisodeResponse = fakeNetworkPreviousEpisodeResponse
        fakeTvMazeService.shouldThrowGetPreviousEpisodeError = false

        // WHEN: Call the repository method and collect emissions
        val results = showDetailRepository.getPreviousEpisode(episodeRef).toList()

        // THEN: Assert the flow emits the correct sequence of states
        assertTrue("Initial Loading state (true) not found or incorrect", results.any { it is Result.Loading && it.status })

        val successResult = results.firstOrNull { it is Result.Success } as? Result.Success<ShowPreviousEpisode>
        assertNotNull("Success result was not found", successResult)
        val previousEpisode = successResult?.data
        assertNotNull("ShowPreviousEpisode data is null", previousEpisode)
        assertEquals("Episode ID mismatch", previousEpisodeId, previousEpisode?.previousEpisodeId)
        assertEquals("Episode name mismatch", "Fake Previous Episode Title", previousEpisode?.previousEpisodeName)
        assertEquals("Episode summary mismatch", "This is a fake previous episode summary.", previousEpisode?.previousEpisodeSummary)

        assertTrue("Final Loading state (false) not found or incorrect", results.any { it is Result.Loading && !it.status })
    }

    @Test
    fun `getPreviousEpisode emits Error and logs to Crashlytics when network call fails`() = runTest {
        // GIVEN: Configure FakeTvMazeService to throw an exception
        val episodeRef = "http://api.tvmaze.com/episodes/67890"
        fakeTvMazeService.shouldThrowGetPreviousEpisodeError = true

        // WHEN: Call the repository method and collect emissions
        val results = showDetailRepository.getPreviousEpisode(episodeRef).toList()

        // THEN: Assert the flow emits the correct sequence of states and logs to crashlytics
        assertTrue("Initial Loading state (true) not found or incorrect", results.any { it is Result.Loading && it.status })

        val networkErrorResult = results.firstOrNull { it is Result.NetworkError } as? Result.NetworkError
        assertNotNull("NetworkError result was not found", networkErrorResult)
        assertTrue("NetworkError's exception type is not IOException", networkErrorResult?.exception is IOException)
        assertEquals("Fake network error for getPreviousEpisode", networkErrorResult?.exception?.message)

        assertTrue("Final Loading state (false) not found or incorrect", results.any { it is Result.Loading && !it.status })

        // Verify Crashlytics was called
        val recordedExceptions = fakeFirebaseCrashlytics.getRecordedExceptions()
        assertEquals("Crashlytics should have recorded one exception", 1, recordedExceptions.size)
        assertTrue("Recorded exception is not IOException", recordedExceptions[0] is IOException)
        assertEquals("Fake network error for getPreviousEpisode", recordedExceptions[0].message)
    }

    @Test
    fun `getPreviousEpisode with null episodeRef completes without data or error`() = runTest {
        // WHEN: Call the repository method with null episodeRef
        val results = showDetailRepository.getPreviousEpisode(null).toList()

        // THEN: Assert the flow completes, possibly emitting only Loading states or nothing
        assertEquals("Should only emit Loading(true) and Loading(false) or be empty, but not Success/Error",
            0, results.filter { it is Result.Success<*> || it is Result.Error || it is Result.NetworkError || it is Result.GenericError }.size) // Updated to include all error/success types
        // It might be empty if the condition is checked before emitting Loading(true)
        // Or it might emit Loading(true) then Loading(false)
        if (results.isNotEmpty()) {
            assertTrue("If not empty, first should be Loading(true)", results.first() is Result.Loading && (results.first() as Result.Loading).status)
            if (results.size > 1) {
                 assertTrue("If more than one, last should be Loading(false)", results.last() is Result.Loading && !(results.last() as Result.Loading).status)
            }
        }
        assertEquals("Crashlytics should not have recorded any exception", 0, fakeFirebaseCrashlytics.getRecordedExceptions().size)
    }

    @Test
    fun `getPreviousEpisode with empty episodeRef completes without data or error`() = runTest {
        // WHEN: Call the repository method with empty episodeRef
        val results = showDetailRepository.getPreviousEpisode("").toList()

        // THEN: Assert the flow completes, possibly emitting only Loading states or nothing
        assertEquals("Should only emit Loading(true) and Loading(false) or be empty, but not Success/Error",
            0, results.filter { it is Result.Success<*> || it is Result.Error || it is Result.NetworkError || it is Result.GenericError }.size)  // Updated to include all error/success types

        if (results.isNotEmpty()) {
            assertTrue("If not empty, first should be Loading(true)", results.first() is Result.Loading && (results.first() as Result.Loading).status)
             if (results.size > 1) {
                 assertTrue("If more than one, last should be Loading(false)", results.last() is Result.Loading && !(results.last() as Result.Loading).status)
            }
        }
        assertEquals("Crashlytics should not have recorded any exception", 0, fakeFirebaseCrashlytics.getRecordedExceptions().size)
    }
}
