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
import com.theupnextapp.network.models.trakt.NetworkTraktShowPeopleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktCast
import com.theupnextapp.network.models.trakt.NetworkTraktPerson
import com.theupnextapp.network.models.trakt.NetworkTraktPersonIds
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
             val mockIds = NetworkTraktIdLookupResponseItemShowIds(trakt = 12345, slug = "slug", imdb = "tt1234567", tmdb = 2, tvdb = 1)
             val mockShow = NetworkTraktIdLookupResponseItemShow(title = "Show Title", year = 2024, ids = mockIds)
             val mockResponseItem = NetworkTraktIdLookupResponseItem(type = "show", score = 1.0, show = mockShow, person = null)
             val mockResponse = NetworkTraktIdLookupResponse()
             mockResponse.add(mockResponseItem)

            whenever(traktService.idLookupAsync(eq("imdb"), eq("tt1234567"), eq("show"))).thenReturn(
                CompletableDeferred(mockResponse),
            )

            val result = dataSource.getTraktIdFromImdbId("tt1234567")

            assertTrue(result.isSuccess)
            assertEquals(12345, result.getOrNull())
        }

    @Test
    fun getShowCast_success() =
        runBlocking {
            val mockPersonIds = NetworkTraktPersonIds(trakt = 1, slug = "slug", imdb = "nm123", tmdb = 10, tvrage = 2)
            val mockPerson = NetworkTraktPerson(name = "Actor Name", ids = mockPersonIds)
            val mockCastMember = NetworkTraktCast(characters = listOf("Character Name"), person = mockPerson, episode_count = 10, series_regular = true)
            val mockResponse = NetworkTraktShowPeopleResponse(cast = listOf(mockCastMember), crew = null)

            whenever(traktService.getShowPeopleAsync(any())).thenReturn(
                CompletableDeferred(mockResponse)
            )

            // Mock TvMaze calls
            val mockTvMazeLookupResponse = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse(
                id = 12345,
                image = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupImage(medium = "", original = ""),
                _links = null,
                externals = null,
                name = "Show Title",
                premiered = "2024-01-01",
                status = "Running",
                summary = "Summary",
                updated = 123456789,
                url = "http://example.com",
                averageRuntime = 60,
                dvdCountry = null,
                genres = emptyList(),
                language = "English",
                network = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupNetwork(
                    id = 1,
                    name = "Network",
                    country = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountry(code = "US", name = "United States", timezone = "America/New_York")
                ),
                officialSite = "http://example.com",
                rating = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupRating(average = 8.0),
                runtime = 60,
                schedule = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupSchedule(days = emptyList(), time = "20:00"),
                type = "Scripted",
                webChannel = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupWebChannel(
                    id = 1,
                    name = "WebChannel",
                    country = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountryX(code = "US", name = "United States", timezone = "America/New_York")
                ),
                weight = 100
            )
            whenever(tvMazeService.getShowLookupAsync(any())).thenReturn(
                CompletableDeferred(mockTvMazeLookupResponse)
            )

            val mockTvMazeImage = com.theupnextapp.network.models.tvmaze.NetworkShowCastImage(
                medium = "medium_url",
                original = "original_url"
            )
            val mockTvMazePerson = com.theupnextapp.network.models.tvmaze.NetworkShowCastPerson(
                name = "Actor Name",
                image = mockTvMazeImage,
                id = 1,
                _links = null,
                birthday = null,
                country = null,
                deathday = null,
                gender = null,
                url = null
            )
            val mockTvMazeCastItem = com.theupnextapp.network.models.tvmaze.NetworkShowCastResponseItem(
                person = mockTvMazePerson,
                character = null,
                self = false,
                voice = false
            )
            val mockTvMazeCastResponse = com.theupnextapp.network.models.tvmaze.NetworkShowCastResponse()
            mockTvMazeCastResponse.add(mockTvMazeCastItem)

            whenever(tvMazeService.getShowCastAsync(any())).thenReturn(
                CompletableDeferred(mockTvMazeCastResponse)
            )

            val result = dataSource.getShowCast("tt1234567")

            assertTrue(result.isSuccess)
            val castList = result.getOrNull()
            assertTrue(castList?.isNotEmpty() == true)
            assertEquals("Actor Name", castList?.first()?.name)
            assertEquals("Character Name", castList?.first()?.character)
            assertEquals(1, castList?.first()?.traktId)
            assertEquals("original_url", castList?.first()?.originalImageUrl) // Verify image enrichment
        }
}
