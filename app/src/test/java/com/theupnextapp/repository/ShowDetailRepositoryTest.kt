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

import com.theupnextapp.domain.EpisodePeople
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.TmdbWatchProviders
import com.theupnextapp.domain.TraktSeason
import com.theupnextapp.network.TmdbService
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.models.tmdb.NetworkTmdbPersonImagesResponse
import com.theupnextapp.network.models.tmdb.NetworkTmdbPersonProfile
import com.theupnextapp.network.models.tmdb.NetworkTmdbWatchProvider
import com.theupnextapp.network.models.tmdb.NetworkTmdbWatchProviderRegion
import com.theupnextapp.network.models.tmdb.NetworkTmdbWatchProvidersResponse
import com.theupnextapp.network.models.trakt.NetworkTraktCast
import com.theupnextapp.network.models.trakt.NetworkTraktEpisodePeopleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktIdLookupResponse
import com.theupnextapp.network.models.trakt.NetworkTraktIdLookupResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktIdLookupResponseItemShow
import com.theupnextapp.network.models.trakt.NetworkTraktIdLookupResponseItemShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktPerson
import com.theupnextapp.network.models.trakt.NetworkTraktPersonIds
import com.theupnextapp.network.models.trakt.NetworkTraktSeasonResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoCountry
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoExternals
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoImage
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoNetwork
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoRating
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoSchedule
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoSelf
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeImage
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeSelf
import com.theupnextapp.repository.fakes.FakeCrashlytics
import com.theupnextapp.repository.fakes.FakeTvMazeService
import com.theupnextapp.repository.fakes.FakeUpnextDao
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

@ExperimentalCoroutinesApi
class ShowDetailRepositoryTest {
    private lateinit var fakeUpnextDao: FakeUpnextDao
    private lateinit var fakeTvMazeService: FakeTvMazeService
    private lateinit var fakeCrashlytics: FakeCrashlytics
    private lateinit var traktService: TraktService
    private lateinit var tmdbService: TmdbService

    private lateinit var showDetailRepository: ShowDetailRepository

    @Before
    fun setUp() {
        fakeUpnextDao = FakeUpnextDao()
        fakeTvMazeService = FakeTvMazeService()
        fakeCrashlytics = FakeCrashlytics()
        fakeCrashlytics.clear()
        traktService = mock()
        tmdbService = mock()

        showDetailRepository =
            ShowDetailRepositoryImpl(
                upnextDao = fakeUpnextDao,
                tvMazeService = fakeTvMazeService,
                traktService = traktService,
                tmdbService = tmdbService,
                crashlytics = fakeCrashlytics,
            )
    }

    @Test
    fun `getShowSummary emits Loading then Success when network call is successful`() =
        runTest {
            val showId = 123
            val fakeNetworkResponse =
                NetworkShowInfoResponse(
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
                    schedule =
                        NetworkShowInfoSchedule(
                            time = "20:00",
                            days = listOf("Monday"),
                        ),
                    rating = NetworkShowInfoRating(average = 8.5),
                    weight = 100,
                    network =
                        NetworkShowInfoNetwork(
                            id = 1,
                            name = "Fake Network",
                            country =
                                NetworkShowInfoCountry(
                                    name = "Fake Country",
                                    code = "FC",
                                    timezone = "Fake/Timezone",
                                ),
                        ),
                    webChannel = Any(),
                    externals =
                        NetworkShowInfoExternals(
                            tvrage = 0,
                            thetvdb = 0,
                            imdb = "tt1234567",
                            themoviedb = null,
                        ),
                    image =
                        NetworkShowInfoImage(
                            medium = "http://fakeimage.com/medium.jpg",
                            original = "http://fakeimage.com/original.jpg",
                        ),
                    summary = "This is a fake show summary.",
                    updated = (System.currentTimeMillis() / 1000L).toInt(),
                    _links =
                        NetworkShowInfoLinks(
                            self =
                                NetworkShowInfoSelf(
                                    href = "http://fakeurl.com/show/$showId/self",
                                ),
                            nextepisode = null,
                            previousepisode = null,
                        ),
                )
            fakeTvMazeService.mockShowInfoResponse = fakeNetworkResponse
            fakeTvMazeService.showSummaryError = null
            fakeTvMazeService.shouldThrowGetShowSummaryError = false

            val results = showDetailRepository.getShowSummary(showId).toList()

            assertTrue(
                "Initial Loading state (true) not found or incorrect",
                results.any { it is Result.Loading && it.status },
            )
            val successResult =
                results.firstOrNull { it is Result.Success } as? Result.Success<ShowDetailSummary>
            assertNotNull(
                "Success result was not found",
                successResult,
            )
            assertEquals(
                "Show ID mismatch",
                showId,
                successResult?.data?.id,
            )
            assertEquals(
                "Show name mismatch",
                "Fake Show Title",
                successResult?.data?.name,
            )
            assertTrue(
                "Final Loading state (false) not found or incorrect",
                results.any { it is Result.Loading && !it.status },
            )
        }

    @Test
    fun `getShowSummary emits Error and logs to Crashlytics when network call fails`() =
        runTest {
            val showId = 456
            val errorMessage = "Fake network error for getShowSummary"
            fakeTvMazeService.showSummaryError = IOException(errorMessage)

            val results = showDetailRepository.getShowSummary(showId).toList()

            assertTrue(
                "Initial Loading state (true) not found or incorrect",
                results.any { it is Result.Loading && it.status },
            )
            val networkErrorResult =
                results.firstOrNull { it is Result.NetworkError } as? Result.NetworkError
            assertNotNull("NetworkError result was not found", networkErrorResult)
            assertTrue(
                "NetworkError's exception type is not IOException",
                networkErrorResult?.exception is IOException,
            )
            assertEquals(errorMessage, networkErrorResult?.exception?.message)
            assertTrue(
                "Final Loading state (false) not found or incorrect",
                results.any { it is Result.Loading && !it.status },
            )
            assertEquals(
                "Crashlytics should have recorded one exception",
                1,
                fakeCrashlytics.getRecordedExceptions().size,
            )
            assertTrue(
                "Recorded exception is not IOException",
                fakeCrashlytics.getRecordedExceptions()[0] is IOException,
            )
        }

    @Test
    fun `getPreviousEpisode emits Loading then Success when network call is successful`() =
        runTest {
            val episodeRef = "http://api.tvmaze.com/episodes/12345"
            val previousEpisodeId = 12345
            val fakeNetworkPreviousEpisodeResponse =
                NetworkShowPreviousEpisodeResponse(
                    id = previousEpisodeId,
                    url = "http://fakeurl.com/episode/$previousEpisodeId",
                    name = "Fake Previous Episode Title",
                    season = 1,
                    number = 1,
                    airdate = "2023-01-01",
                    airtime = "20:00",
                    airstamp = "2023-01-01T20:00:00Z",
                    runtime = 30,
                    image =
                        NetworkShowPreviousEpisodeImage(
                            medium = "http://fakeimage.com/medium.jpg",
                            original = "http://fakeimage.com/original.jpg",
                        ),
                    summary = "This is a fake previous episode summary.",
                    _links =
                        NetworkShowPreviousEpisodeLinks(
                            self =
                                NetworkShowPreviousEpisodeSelf(
                                    href = "http://fakeurl.com/episode/$previousEpisodeId/self",
                                ),
                        ),
                )
            fakeTvMazeService.mockPreviousEpisodeResponse = fakeNetworkPreviousEpisodeResponse
            fakeTvMazeService.previousEpisodeError = null
            fakeTvMazeService.shouldThrowGetPreviousEpisodeError = false

            val results = showDetailRepository.getPreviousEpisode(episodeRef).toList()

            assertTrue(
                "Initial Loading state (true) not found or incorrect",
                results.any { it is Result.Loading && it.status },
            )

            val successResult =
                results.firstOrNull { it is Result.Success } as? Result.Success<ShowPreviousEpisode>
            assertNotNull("Success result was not found", successResult)
            val previousEpisode = successResult?.data
            assertNotNull(
                "ShowPreviousEpisode data is null",
                previousEpisode,
            )
            assertEquals(
                "Episode ID mismatch",
                previousEpisodeId,
                previousEpisode?.previousEpisodeId,
            )
            assertEquals(
                "Episode name mismatch",
                "Fake Previous Episode Title",
                previousEpisode?.previousEpisodeName,
            )
            assertEquals(
                "Episode summary mismatch",
                "This is a fake previous episode summary.",
                previousEpisode?.previousEpisodeSummary,
            )

            assertTrue(
                "Final Loading state (false) not found or incorrect",
                results.any { it is Result.Loading && !it.status },
            )
        }

    @Test
    fun `getPreviousEpisode emits Error and logs to Crashlytics when network call fails`() =
        runTest {
            val episodeRef = "http://api.tvmaze.com/episodes/67890"
            val errorMessage = "Fake network error for getPreviousEpisode"
            fakeTvMazeService.previousEpisodeError = IOException(errorMessage)

            val results = showDetailRepository.getPreviousEpisode(episodeRef).toList()

            assertTrue(
                "Initial Loading state (true) not found or incorrect",
                results.any { it is Result.Loading && it.status },
            )

            val networkErrorResult =
                results.firstOrNull { it is Result.NetworkError } as? Result.NetworkError
            assertNotNull(
                "NetworkError result was not found",
                networkErrorResult,
            )
            assertTrue(
                "NetworkError's exception type is not IOException",
                networkErrorResult?.exception is IOException,
            )
            assertEquals(errorMessage, networkErrorResult?.exception?.message)

            assertTrue(
                "Final Loading state (false) not found or incorrect",
                results.any { it is Result.Loading && !it.status },
            )

            val recordedExceptions = fakeCrashlytics.getRecordedExceptions()
            assertEquals(
                "Crashlytics should have recorded one exception",
                1,
                recordedExceptions.size,
            )
            assertTrue(
                "Recorded exception is not IOException",
                recordedExceptions[0] is IOException,
            )
            assertEquals(errorMessage, recordedExceptions[0].message)
        }

    @Test
    fun `getPreviousEpisode with null episodeRef completes without data or error`() =
        runTest {
            val results = showDetailRepository.getPreviousEpisode(null).toList()

            assertEquals(
                "Should only emit Loading(true) and Loading(false) or be empty, " +
                    "but not Success/Error",
                0,
                results.filter {
                    it is Result.Success<*> || it is Result.Error ||
                        it is Result.NetworkError || it is Result.GenericError
                }.size,
            )
            if (results.isNotEmpty()) {
                assertTrue(
                    "If not empty, first should be Loading(true)",
                    results.first() is Result.Loading &&
                        (
                            results.first() as Result.Loading
                        ).status,
                )
                if (results.size > 1) {
                    assertTrue(
                        "If more than one, last should be Loading(false)",
                        results.last() is Result.Loading &&
                            !(
                                results.last() as Result.Loading
                            ).status,
                    )
                }
            }
            assertEquals(
                "Crashlytics should not have recorded any exception",
                0,
                fakeCrashlytics.getRecordedExceptions().size,
            )
        }

    @Test
    fun `getPreviousEpisode with empty episodeRef completes without data or error`() =
        runTest {
            val results = showDetailRepository.getPreviousEpisode("").toList()

            assertEquals(
                "Should only emit Loading(true) and Loading(false) or " +
                    "be empty, but not Success/Error",
                0,
                results.filter {
                    it is Result.Success<*> || it is Result.Error ||
                        it is Result.NetworkError || it is Result.GenericError
                }.size,
            )

            if (results.isNotEmpty()) {
                assertTrue(
                    "If not empty, first should be Loading(true)",
                    results.first() is Result.Loading &&
                        (
                            results.first() as Result.Loading
                        ).status,
                )
                if (results.size > 1) {
                    assertTrue(
                        "If more than one, last should be Loading(false)",
                        results.last() is Result.Loading &&
                            !(
                                results.last() as Result.Loading
                            ).status,
                    )
                }
            }
            assertEquals(
                "Crashlytics should not have recorded any exception",
                0,
                fakeCrashlytics.getRecordedExceptions().size,
            )
        }

    @Test
    fun `getEpisodePeople correctly merges Trakt and TMDB data using concurrent requests`() =
        runTest {
            val traktId = 123
            val seasonNumber = 1
            val episodeNumber = 1

            val fakeTraktResponse = NetworkTraktEpisodePeopleResponse(
                cast = listOf(
                    NetworkTraktCast(
                        characters = listOf("Character 1"),
                        person = NetworkTraktPerson(
                            name = "Actor 1",
                            ids = NetworkTraktPersonIds(trakt = 1, imdb = "nm1", tmdb = 101, slug = "actor-1", tvrage = null)
                        ),
                        episode_count = null,
                        series_regular = null
                    ),
                    NetworkTraktCast(
                        characters = listOf("Character 2"),
                        person = NetworkTraktPerson(
                            name = "Actor 2",
                            ids = NetworkTraktPersonIds(trakt = 2, imdb = "nm2", tmdb = 102, slug = "actor-2", tvrage = null)
                        ),
                        episode_count = null,
                        series_regular = null
                    )
                ),
                guest_stars = emptyList(),
                crew = null
            )

            whenever(traktService.getEpisodePeopleAsync(traktId.toString(), seasonNumber, episodeNumber))
                .thenReturn(CompletableDeferred(fakeTraktResponse))

            whenever(tmdbService.getPersonImagesAsync(101))
                .thenReturn(CompletableDeferred(NetworkTmdbPersonImagesResponse(
                    id = 101,
                    profiles = listOf(
                        NetworkTmdbPersonProfile(
                            file_path = "/image_101.jpg", width = 200, height = 300, aspect_ratio = 0.6, vote_average = 5.0, vote_count = 1, iso_639_1 = null
                        )
                    )
                )))

            whenever(tmdbService.getPersonImagesAsync(102))
                .thenReturn(CompletableDeferred(NetworkTmdbPersonImagesResponse(
                    id = 102,
                    profiles = listOf(
                        NetworkTmdbPersonProfile(
                            file_path = "/image_102.jpg", width = 200, height = 300, aspect_ratio = 0.6, vote_average = 5.0, vote_count = 1, iso_639_1 = null
                        )
                    )
                )))

            val results = showDetailRepository.getEpisodePeople(traktId, seasonNumber, episodeNumber).toList()

            val successResult = results.firstOrNull { it is Result.Success } as? Result.Success<EpisodePeople>
            assertNotNull("Success result was not found", successResult)

            val cast = successResult?.data?.cast
            assertEquals(2, cast?.size)
            assertEquals("/image_101.jpg", cast?.get(0)?.originalImageUrl)
            assertEquals("/image_102.jpg", cast?.get(1)?.originalImageUrl)
        }

    @Test
    fun `getEpisodePeople handles TMDB API failures gracefully`() =
        runTest {
            val traktId = 123

            val fakeTraktResponse = NetworkTraktEpisodePeopleResponse(
                cast = listOf(
                    NetworkTraktCast(
                        characters = listOf("Character 1"),
                        person = NetworkTraktPerson(
                            name = "Actor 1",
                            ids = NetworkTraktPersonIds(trakt = 1, imdb = "nm1", tmdb = 101, slug = "actor-1", tvrage = null)
                        ),
                        episode_count = null,
                        series_regular = null
                    )
                ),
                guest_stars = emptyList(),
                crew = null
            )

            whenever(traktService.getEpisodePeopleAsync(any(), any(), any()))
                .thenReturn(CompletableDeferred(fakeTraktResponse))

            val fakeDeferred = CompletableDeferred<NetworkTmdbPersonImagesResponse>()
            fakeDeferred.completeExceptionally(IOException("TMDB SSL Handshake Error"))
            whenever(tmdbService.getPersonImagesAsync(101)).thenReturn(fakeDeferred)

            val results = showDetailRepository.getEpisodePeople(traktId, 1, 1).toList()

            val successResult = results.firstOrNull { it is Result.Success } as? Result.Success<EpisodePeople>
            assertNotNull("Success result was not found after error", successResult)

            val cast = successResult?.data?.cast
            assertEquals(1, cast?.size)
            assertEquals("Actor 1", cast?.get(0)?.name)
            // The image should be null because TMDB failed, but it shouldn't crash the repository response flow
            assertEquals(null, cast?.get(0)?.originalImageUrl)
        }

    @Test
    fun `getEpisodePeople with null tmdbId safely returns Trakt model without network fetch`() =
        runTest {
            val traktId = 123

            val fakeTraktResponse = NetworkTraktEpisodePeopleResponse(
                cast = listOf(
                    NetworkTraktCast(
                        characters = listOf("Character 1"),
                        person = NetworkTraktPerson(
                            name = "Actor No TMDB ID",
                            ids = NetworkTraktPersonIds(trakt = 1, imdb = "nm1", tmdb = null, slug = "actor-null", tvrage = null)
                        ),
                        episode_count = null,
                        series_regular = null
                    )
                ),
                guest_stars = emptyList(),
                crew = null
            )

            whenever(traktService.getEpisodePeopleAsync(any(), any(), any()))
                .thenReturn(CompletableDeferred(fakeTraktResponse))

            val results = showDetailRepository.getEpisodePeople(traktId, 1, 1).toList()
            val successResult = results.firstOrNull { it is Result.Success } as? Result.Success<EpisodePeople>

            val cast = successResult?.data?.cast
            assertEquals(1, cast?.size)
            assertEquals("Actor No TMDB ID", cast?.get(0)?.name)
            assertEquals(null, cast?.get(0)?.originalImageUrl)
        }

    @Test
    fun `getTraktShowSeasons emits Loading then Success and caches the response`() =
        runTest {
            val traktId = 123
            val fakeSeasons =
                listOf(
                    NetworkTraktSeasonResponse(
                        number = 1,
                        title = "Season 1",
                        episodeCount = 10,
                        airedEpisodes = 10,
                    ),
                    NetworkTraktSeasonResponse(
                        number = 2,
                        title = "Season 2",
                        episodeCount = 8,
                        airedEpisodes = 8,
                    ),
                )

            whenever(traktService.getShowSeasonsAsync(traktId.toString()))
                .thenReturn(CompletableDeferred(fakeSeasons))

            val results = showDetailRepository.getTraktShowSeasons(traktId).toList()
            assertTrue(results.first() is Result.Loading)
            val successResult = results.last() as Result.Success<List<TraktSeason>>
            assertEquals(2, successResult.data.size)
            assertEquals(1, successResult.data[0].number)
            assertEquals(10, successResult.data[0].episodeCount)

            // Verify second call hits in-memory cache without repeating network request
            val cachedResults = showDetailRepository.getTraktShowSeasons(traktId).toList()
            assertEquals(1, cachedResults.size)
            val cachedSuccess = cachedResults.first() as Result.Success<List<TraktSeason>>
            assertEquals(2, cachedSuccess.data.size)
        }

    @Test
    fun `getTraktShowSeasons emits GenericError and records exception on network failure`() =
        runTest {
            val traktId = 999
            whenever(traktService.getShowSeasonsAsync(traktId.toString()))
                .thenThrow(RuntimeException("Network failure"))

            val results = showDetailRepository.getTraktShowSeasons(traktId).toList()
            val errorResult = results.last()
            assertTrue(errorResult is Result.Error || errorResult is Result.GenericError)
            assertTrue(fakeCrashlytics.getRecordedExceptions().isNotEmpty())
        }

    @Test
    fun `getShowWatchProviders parses all tiers and caches response`() =
        runTest {
            val tmdbId = 1399
            val countryCode = "US"
            val region =
                NetworkTmdbWatchProviderRegion(
                    link = "https://www.justwatch.com/us/tv-show/game-of-thrones",
                    flatrate =
                        listOf(
                            NetworkTmdbWatchProvider(
                                provider_id = 8,
                                provider_name = "Netflix",
                                logo_path = "/netflix.jpg",
                                display_priority = 2,
                            ),
                            NetworkTmdbWatchProvider(
                                provider_id = 9,
                                provider_name = "Amazon Prime Video",
                                logo_path = "/prime.jpg",
                                display_priority = 1,
                            ),
                        ),
                    rent = null,
                    buy =
                        listOf(
                            NetworkTmdbWatchProvider(
                                provider_id = 2,
                                provider_name = "Apple TV",
                                logo_path = "/apple.jpg",
                                display_priority = 3,
                            ),
                        ),
                    free =
                        listOf(
                            NetworkTmdbWatchProvider(
                                provider_id = 73,
                                provider_name = "Tubi TV",
                                logo_path = "/tubi.jpg",
                                display_priority = 4,
                            ),
                        ),
                    ads =
                        listOf(
                            NetworkTmdbWatchProvider(
                                provider_id = 300,
                                provider_name = "Pluto TV",
                                logo_path = "/pluto.jpg",
                                display_priority = 5,
                            ),
                        ),
                )
            val fakeResponse =
                NetworkTmdbWatchProvidersResponse(
                    id = tmdbId,
                    results = mapOf("US" to region),
                )

            whenever(tmdbService.getShowWatchProvidersAsync(tmdbId))
                .thenReturn(CompletableDeferred(fakeResponse))

            val results =
                showDetailRepository.getShowWatchProviders(
                    imdbID = null,
                    tmdbID = tmdbId,
                    countryCode = countryCode,
                ).toList()

            val successResult = results.last() as Result.Success<TmdbWatchProviders>
            val watchProviders = successResult.data
            assertEquals(tmdbId, watchProviders.id)
            assertEquals("https://www.justwatch.com/us/tv-show/game-of-thrones", watchProviders.link)
            assertEquals("US", watchProviders.countryCode)
            assertEquals(5, watchProviders.providers?.size)

            // Verify displayPriority ordering: Amazon (1) before Netflix (2)
            assertEquals("Amazon Prime Video", watchProviders.flatrateProviders[0].name)
            assertEquals("Netflix", watchProviders.flatrateProviders[1].name)
            assertEquals("Stream", watchProviders.flatrateProviders[0].tier)

            // Verify free and ads tiers
            assertEquals(2, watchProviders.freeProviders.size)
            assertEquals("Tubi TV", watchProviders.freeProviders[0].name)
            assertEquals("Free", watchProviders.freeProviders[0].tier)
            assertEquals("Pluto TV", watchProviders.freeProviders[1].name)
            assertEquals("Free with Ads", watchProviders.freeProviders[1].tier)

            // Verify buy tier
            assertEquals(1, watchProviders.buyRentProviders.size)
            assertEquals("Apple TV", watchProviders.buyRentProviders[0].name)
            assertEquals("Buy", watchProviders.buyRentProviders[0].tier)

            // Second call with same ID and country should hit cache without network call
            val cachedResults =
                showDetailRepository.getShowWatchProviders(
                    imdbID = null,
                    tmdbID = tmdbId,
                    countryCode = countryCode,
                ).toList()
            val cachedSuccess = cachedResults.last() as Result.Success<TmdbWatchProviders>
            assertEquals(5, cachedSuccess.data.providers?.size)
        }

    @Test
    fun `getShowWatchProviders resolves tmdbId from imdbId via Trakt when tmdbId is not provided`() =
        runTest {
            val imdbId = "tt0944947"
            val resolvedTmdbId = 1399
            val countryCode = "US"

            val lookupItem =
                NetworkTraktIdLookupResponseItem(
                    show =
                        NetworkTraktIdLookupResponseItemShow(
                            ids =
                                NetworkTraktIdLookupResponseItemShowIds(
                                    trakt = 1,
                                    slug = "got",
                                    tvdb = 1,
                                    imdb = imdbId,
                                    tmdb = resolvedTmdbId,
                                ),
                            title = "Game of Thrones",
                            year = 2011,
                        ),
                    person = null,
                    score = 1000,
                    type = "show",
                )
            val lookupResponse = NetworkTraktIdLookupResponse().apply { add(lookupItem) }

            whenever(traktService.idLookupAsync(idType = "imdb", id = imdbId))
                .thenReturn(CompletableDeferred(lookupResponse))

            val region =
                NetworkTmdbWatchProviderRegion(
                    link = "https://www.justwatch.com/us/tv-show/game-of-thrones",
                    flatrate =
                        listOf(
                            NetworkTmdbWatchProvider(
                                provider_id = 8,
                                provider_name = "Netflix",
                                logo_path = "/netflix.jpg",
                                display_priority = 1,
                            ),
                        ),
                    rent = null,
                    buy = null,
                    free = null,
                    ads = null,
                )
            val fakeResponse =
                NetworkTmdbWatchProvidersResponse(
                    id = resolvedTmdbId,
                    results = mapOf("US" to region),
                )
            whenever(tmdbService.getShowWatchProvidersAsync(resolvedTmdbId))
                .thenReturn(CompletableDeferred(fakeResponse))

            val results =
                showDetailRepository.getShowWatchProviders(
                    imdbID = imdbId,
                    tmdbID = null,
                    countryCode = countryCode,
                ).toList()

            val successResult = results.last() as Result.Success<TmdbWatchProviders>
            assertEquals(resolvedTmdbId, successResult.data.id)
            assertEquals(1, successResult.data.providers?.size)
            assertEquals("Netflix", successResult.data.providers?.get(0)?.name)
        }

    @Test
    fun `getShowWatchProviders returns empty providers when both imdbId and tmdbId are missing`() =
        runTest {
            val results =
                showDetailRepository.getShowWatchProviders(
                    imdbID = null,
                    tmdbID = null,
                    countryCode = "US",
                ).toList()

            val successResult = results.last() as Result.Success<TmdbWatchProviders>
            assertEquals(null, successResult.data.id)
            assertTrue(successResult.data.providers.isNullOrEmpty())
        }
}
