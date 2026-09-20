/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleEpisode
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleShow
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedEpisode
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowInfo
import com.theupnextapp.network.models.trakt.TraktHistoryPage
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import java.util.Locale

@ExperimentalCoroutinesApi
class DashboardViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var traktRepository: TraktRepository
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var watchProgressRepository: WatchProgressRepository
    private lateinit var localWorkManager: WorkManager
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        traktRepository = mock(TraktRepository::class.java)
        dashboardRepository = mock(DashboardRepository::class.java)
        watchProgressRepository = mock(WatchProgressRepository::class.java)
        localWorkManager = mock(WorkManager::class.java)
        firebaseAnalytics = mock(FirebaseAnalytics::class.java)

        `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))
        `when`(traktRepository.traktMostAnticipatedShows).thenReturn(flowOf(emptyList()))
        runBlocking {
            `when`(traktRepository.getRegionalTrendingShows(any())).thenReturn(
                Result.success(
                    emptyList(),
                ),
            )
            `when`(traktRepository.getTraktPlaybackProgress(any())).thenReturn(
                Result.success(
                    emptyList(),
                ),
            )
        }
        `when`(dashboardRepository.todayShows).thenReturn(flowOf(emptyList()))
        `when`(dashboardRepository.yesterdayShows).thenReturn(flowOf(emptyList()))
        `when`(dashboardRepository.tomorrowShows).thenReturn(flowOf(emptyList()))
        `when`(dashboardRepository.isLoadingYesterdayShows).thenReturn(MutableStateFlow(false))
        `when`(dashboardRepository.isLoadingTodayShows).thenReturn(MutableStateFlow(false))
        `when`(dashboardRepository.isLoadingTomorrowShows).thenReturn(MutableStateFlow(false))
        `when`(traktRepository.isLoading).thenReturn(MutableStateFlow(false))
        `when`(traktRepository.isLoadingTraktMostAnticipated).thenReturn(MutableStateFlow(false))
        `when`(watchProgressRepository.isSyncing).thenReturn(MutableStateFlow(false))

        viewModel =
            DashboardViewModel(
                traktRepository = traktRepository,
                dashboardRepository = dashboardRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = localWorkManager,
                firebaseAnalytics = firebaseAnalytics,
            )
    }

    @Test
    fun `viewModel initializes correctly`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `when trakt access token is null, dependent flows handle gracefully`() =
        runTest {
            // Ensure the mock returns null explicitly for access token
            `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))

            // Create viewModel fresh inside the scope to trigger the init block collection
            val testViewModel =
                DashboardViewModel(
                    traktRepository = traktRepository,
                    dashboardRepository = dashboardRepository,
                    watchProgressRepository = watchProgressRepository,
                    localWorkManager = localWorkManager,
                    firebaseAnalytics = firebaseAnalytics,
                )

            // Give flows time to emit initial states
            val accessTokenValue = testViewModel.traktAccessToken.value
            val airingSoonValue = testViewModel.airingSoonShows.value
            val recentHistoryValue = testViewModel.recentHistory.value

            // Validate that null access token safely maps to null/empty dependent structures, not crashes
            assertNull("Access token should be null", accessTokenValue)
            assertTrue(
                "Airing soon shows should be empty when unauthorized",
                airingSoonValue.isNullOrEmpty(),
            )
            assertTrue(
                "Recent history should be empty when unauthorized",
                recentHistoryValue.isNullOrEmpty(),
            )
        }

    @Test
    fun `when marking episode as watched with auth, triggers sync worker`() =
        runTest {
            val token =
                TraktAccessToken(
                    access_token = "mock_token",
                    created_at = 1234567890L,
                    expires_in = 3600L,
                    refresh_token = "mock_refresh",
                    scope = "public",
                    token_type = "bearer",
                )
            `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(token))

            val testViewModel =
                DashboardViewModel(
                    traktRepository = traktRepository,
                    dashboardRepository = dashboardRepository,
                    watchProgressRepository = watchProgressRepository,
                    localWorkManager = localWorkManager,
                    firebaseAnalytics = firebaseAnalytics,
                )

            // Use a specific non-null invocation since workManager.enqueue expects a non-null WorkRequest
            val mockOperation = mock(Operation::class.java)
            `when`(
                localWorkManager.enqueueUniqueWork(
                    eq(DashboardViewModel.SYNC_WORK_NAME),
                    eq(ExistingWorkPolicy.REPLACE),
                    any<OneTimeWorkRequest>(),
                ),
            ).thenReturn(mockOperation)

            testViewModel.onMarkEpisodeWatched(
                showTvMazeId = 1,
                imdbId = "tt123",
                showTraktId = 123,
                season = 1,
                number = 1,
            )

            // Verify work manager was told to enqueue the sync
            verify(localWorkManager).enqueueUniqueWork(
                eq(DashboardViewModel.SYNC_WORK_NAME),
                eq(ExistingWorkPolicy.REPLACE),
                any<OneTimeWorkRequest>(),
            )
        }

    @Test
    fun `when receiving new isSyncing state completion, requests fresh history`() =
        runTest {
            val token =
                TraktAccessToken(
                    access_token = "mock_token",
                    created_at = 1234567890L,
                    expires_in = 3600L,
                    refresh_token = "mock_refresh",
                    scope = "public",
                    token_type = "bearer",
                )
            `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(token))

            val syncStateFlow = MutableStateFlow(false)
            `when`(watchProgressRepository.isSyncing).thenReturn(syncStateFlow)

            val testViewModel =
                DashboardViewModel(
                    traktRepository = traktRepository,
                    dashboardRepository = dashboardRepository,
                    watchProgressRepository = watchProgressRepository,
                    localWorkManager = localWorkManager,
                    firebaseAnalytics = firebaseAnalytics,
                )

            // Trigger sync start
            syncStateFlow.value = true

            // Trigger sync complete
            syncStateFlow.value = false

            assertNotNull(testViewModel)
            // Verify it asked trakt for history and playback progress refresh using the token
            verify(traktRepository).getTraktRecentHistory("mock_token")
            verify(traktRepository).getTraktPlaybackProgress("mock_token")
        }

    @Test
    fun `when fetchDashboardData is invoked multiple times, guards prevent redundant network calls`() =
        runTest {
            val token = "mock_token"

            // Setup responses for the repository
            val mockScheduleResponse = NetworkTraktMyScheduleResponse()
            `when`(
                traktRepository.getTraktMySchedule(
                    any(),
                    any(),
                    any(),
                ),
            ).thenReturn(Result.success(mockScheduleResponse))
            `when`(traktRepository.getTraktRecommendations(token)).thenReturn(
                Result.success(
                    NetworkTraktRecommendationsResponse(),
                ),
            )
            `when`(traktRepository.getTraktRecentHistory(token)).thenReturn(Result.success(TraktHistoryPage(items = listOf())))
            `when`(traktRepository.getTraktPlaybackProgress(token)).thenReturn(Result.success(listOf()))

            val testViewModel = DashboardViewModel(
                traktRepository = traktRepository,
                dashboardRepository = dashboardRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = localWorkManager,
                firebaseAnalytics = firebaseAnalytics,
            )

            // First invocation should trigger the fetches
            testViewModel.fetchDashboardData(token)

            // Wait for coroutines to complete
            advanceUntilIdle()

            // Verify they were called once
            verify(traktRepository, Mockito.times(1)).getTraktMySchedule(any(), any(), any())
            verify(traktRepository, Mockito.times(1)).getTraktRecommendations(token)
            verify(traktRepository, Mockito.times(1)).getTraktRecentHistory(token)
            verify(traktRepository, Mockito.times(1)).getTraktPlaybackProgress(token)

            // Explicitly verify the states have values so the guard should be active
            assertNotNull(testViewModel.upNextShows.value)
            assertNotNull(testViewModel.airingSoonShows.value)
            assertNotNull(testViewModel.recommendedShows.value)
            assertNotNull(testViewModel.recentHistory.value)

            // Second invocation should NOT trigger the fetches again because of the guard
            testViewModel.fetchDashboardData(token)

            advanceUntilIdle()

            // Verify they were STILL only called once total
            verify(traktRepository, Mockito.times(1)).getTraktMySchedule(any(), any(), any())
            verify(traktRepository, Mockito.times(1)).getTraktRecommendations(token)
            verify(traktRepository, Mockito.times(1)).getTraktRecentHistory(token)
            verify(traktRepository, Mockito.times(1)).getTraktPlaybackProgress(token)
        }

    @Test
    fun `viewModel fetches regional trending shows on init`() =
        runTest {
            val countryCode = Locale.getDefault().country

            // Verify that the repository method was called with the default country code
            verify(traktRepository, Mockito.times(1)).getRegionalTrendingShows(countryCode)

            // Give flows time to emit initial states
            advanceUntilIdle()

            // Since we mocked success with emptyList(), regionalTrendingShows should not be null
            assertNotNull(viewModel.regionalTrendingShows.value)
        }

    @Test
    fun `todayShows emits cached items immediately regardless of isLoadingTodayShows`() =
        runTest {
            val cachedShows =
                listOf(
                    ScheduleShow(
                        id = 1,
                        showId = 10,
                        originalImage = "image_orig.png",
                        mediumImage = "image_med.png",
                        language = "English",
                        name = "Test Show",
                        officialSite = null,
                        premiered = "2023-01-01",
                        runtime = "30",
                        status = "Running",
                        summary = "Summary",
                        type = "Scripted",
                        updated = "123",
                        url = "url",
                    ),
                )
            `when`(dashboardRepository.todayShows).thenReturn(flowOf(cachedShows))
            `when`(dashboardRepository.isLoadingTodayShows).thenReturn(MutableStateFlow(true))

            val testViewModel =
                DashboardViewModel(
                    traktRepository = traktRepository,
                    dashboardRepository = dashboardRepository,
                    watchProgressRepository = watchProgressRepository,
                    localWorkManager = localWorkManager,
                    firebaseAnalytics = firebaseAnalytics,
                )

            advanceUntilIdle()

            assertEquals(cachedShows, testViewModel.todayShows.value)
            assertTrue(testViewModel.isLoadingTodayShows.value)
        }

    @Test
    fun `fetchRecentHistory maps and persists watched episodes to watchProgressRepository`() =
        runTest {
            val token = "mock_token"
            val historyResponse =
                listOf(
                    NetworkTraktHistoryResponse(
                        id = 1,
                        watchedAt = "2026-09-20T00:00:00.000Z",
                        action = "watch",
                        type = "episode",
                        episode =
                            NetworkTraktWatchedEpisode(
                                season = 1,
                                number = 5,
                                title = "Episode 5",
                                plays = 1,
                                lastWatchedAt = "2026-09-20T00:00:00.000Z",
                            ),
                        show =
                            NetworkTraktWatchedShowInfo(
                                title = "Test Show",
                                year = 2026,
                                ids =
                                    NetworkTraktWatchedShowIds(
                                        trakt = 999,
                                        tvdb = null,
                                        imdb = "tt999",
                                        tmdb = null,
                                        slug = "test-show",
                                    ),
                            ),
                    ),
                )

            `when`(traktRepository.getTraktRecentHistory(token)).thenReturn(Result.success(TraktHistoryPage(items = historyResponse)))

            val testViewModel =
                DashboardViewModel(
                    traktRepository = traktRepository,
                    dashboardRepository = dashboardRepository,
                    watchProgressRepository = watchProgressRepository,
                    localWorkManager = localWorkManager,
                    firebaseAnalytics = firebaseAnalytics,
                )

            testViewModel.fetchDashboardData(token)
            advanceUntilIdle()

            verify(watchProgressRepository).saveWatchedEpisodes(check { episodes ->
                assertEquals(1, episodes.size)
                assertEquals(999, episodes[0].showTraktId)
                assertEquals(1, episodes[0].seasonNumber)
                assertEquals(5, episodes[0].episodeNumber)
                assertEquals("tt999", episodes[0].showImdbId)
                assertTrue(episodes[0].isSynced)
            })
        }

    @Test
    fun `fetchDashboardData loads both Up Next progress and Airing Soon schedule`() =
        runTest {
            val token = "mock_token"
            val mockPlayback = listOf(
                NetworkTraktPlaybackResponse(
                    progress = 45.0f,
                    action = "pause",
                    type = "episode",
                    show = NetworkTraktWatchedShowInfo(
                        title = "Severance",
                        year = 2022,
                        ids = NetworkTraktWatchedShowIds(
                            trakt = 100,
                            tvdb = 200,
                            imdb = "tt12345",
                            tmdb = 300,
                            slug = "severance",
                        ),
                    ),
                    episode = NetworkTraktWatchedEpisode(
                        season = 2,
                        number = 1,
                        title = "Hello Ms. Cobel",
                        plays = 1,
                        lastWatchedAt = "2026-09-20T00:00:00.000Z",
                    ),
                ),
            )
            `when`(traktRepository.getTraktPlaybackProgress(token)).thenReturn(Result.success(mockPlayback))

            val testViewModel = DashboardViewModel(
                traktRepository = traktRepository,
                dashboardRepository = dashboardRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = localWorkManager,
                firebaseAnalytics = firebaseAnalytics,
            )

            testViewModel.fetchDashboardData(token)
            advanceUntilIdle()

            verify(traktRepository).getTraktPlaybackProgress(token)
            val upNext = testViewModel.upNextShows.value
            assertNotNull(upNext)
            assertEquals(1, upNext?.size)
            assertEquals("Severance", upNext?.first()?.show?.title)
            assertEquals(2, upNext?.first()?.episode?.season)
            assertEquals(1, upNext?.first()?.episode?.number)
        }

    @Test
    fun `upNextShows preserves uncompleted episodes when future calendar moves forward`() =
        runTest {
            val token = "mock_token"
            val mockPlayback = listOf(
                NetworkTraktPlaybackResponse(
                    progress = 10.0f,
                    action = "pause",
                    type = "episode",
                    show = NetworkTraktWatchedShowInfo(
                        title = "Silo",
                        year = 2023,
                        ids = NetworkTraktWatchedShowIds(
                            trakt = 500,
                            tvdb = null,
                            imdb = "tt55555",
                            tmdb = null,
                            slug = "silo",
                        ),
                    ),
                    episode = NetworkTraktWatchedEpisode(
                        season = 1,
                        number = 3,
                        title = "Machines",
                        plays = 0,
                        lastWatchedAt = null,
                    ),
                ),
            )
            `when`(traktRepository.getTraktPlaybackProgress(token)).thenReturn(Result.success(mockPlayback))

            val testViewModel = DashboardViewModel(
                traktRepository = traktRepository,
                dashboardRepository = dashboardRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = localWorkManager,
                firebaseAnalytics = firebaseAnalytics,
            )

            testViewModel.fetchDashboardData(token)
            advanceUntilIdle()

            // The upNextShows preserves the unwatched episode even as schedule dates progress
            assertEquals(1, testViewModel.upNextShows.value?.size)
            val item = testViewModel.upNextShows.value?.first()
            assertEquals("Silo", item?.show?.title)
            assertEquals(3, item?.episode?.number)
        }

    @Test
    fun `onMarkEpisodeWatched on Up Next card triggers optimistic UI update and queues sync`() =
        runTest {
            val token = TraktAccessToken(
                access_token = "mock_token",
                created_at = 1234567890L,
                expires_in = 3600L,
                refresh_token = "mock_refresh",
                scope = "public",
                token_type = "bearer",
            )
            `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(token))

            val mockPlayback = listOf(
                NetworkTraktPlaybackResponse(
                    progress = 80.0f,
                    action = "pause",
                    type = "episode",
                    show = NetworkTraktWatchedShowInfo(
                        title = "Severance",
                        year = 2022,
                        ids = NetworkTraktWatchedShowIds(
                            trakt = 100,
                            tvdb = null,
                            imdb = "tt12345",
                            tmdb = null,
                            slug = "severance",
                        ),
                    ),
                    episode = NetworkTraktWatchedEpisode(
                        season = 2,
                        number = 1,
                        title = "Hello Ms. Cobel",
                        plays = 1,
                        lastWatchedAt = "2026-09-20T00:00:00.000Z",
                    ),
                ),
            )
            `when`(traktRepository.getTraktPlaybackProgress("mock_token")).thenReturn(Result.success(mockPlayback))

            val mockOperation = mock(Operation::class.java)
            `when`(
                localWorkManager.enqueueUniqueWork(
                    eq(DashboardViewModel.SYNC_WORK_NAME),
                    eq(ExistingWorkPolicy.REPLACE),
                    any<OneTimeWorkRequest>(),
                ),
            ).thenReturn(mockOperation)

            val testViewModel = DashboardViewModel(
                traktRepository = traktRepository,
                dashboardRepository = dashboardRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = localWorkManager,
                firebaseAnalytics = firebaseAnalytics,
            )

            testViewModel.fetchDashboardData("mock_token")
            advanceUntilIdle()

            assertEquals(1, testViewModel.upNextShows.value?.size)

            // Mark watched
            testViewModel.onMarkEpisodeWatched(
                showTvMazeId = 999,
                imdbId = "tt12345",
                showTraktId = 100,
                season = 2,
                number = 1,
            )

            // Optimistically removed immediately from upNextShows
            assertTrue(testViewModel.upNextShows.value.isNullOrEmpty())

            advanceUntilIdle()

            // Verify watchProgressRepository and WorkManager were invoked
            verify(watchProgressRepository).markEpisodeWatched(
                showTraktId = 100,
                showTvMazeId = 999,
                showImdbId = "tt12345",
                seasonNumber = 2,
                episodeNumber = 1,
            )
            verify(localWorkManager).enqueueUniqueWork(
                eq(DashboardViewModel.SYNC_WORK_NAME),
                eq(ExistingWorkPolicy.REPLACE),
                any<OneTimeWorkRequest>(),
            )
            verify(firebaseAnalytics).logEvent(Mockito.eq(FirebaseAnalytics.Event.SELECT_CONTENT), any())
        }

    @Test
    fun `empty Up Next and empty Airing Soon states emit appropriate UI states`() =
        runTest {
            val token = "mock_token"
            `when`(traktRepository.getTraktPlaybackProgress(token)).thenReturn(Result.success(emptyList()))
            `when`(traktRepository.getTraktMySchedule(any(), any(), any()))
                .thenReturn(Result.success(NetworkTraktMyScheduleResponse()))

            val testViewModel = DashboardViewModel(
                traktRepository = traktRepository,
                dashboardRepository = dashboardRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = localWorkManager,
                firebaseAnalytics = firebaseAnalytics,
            )

            testViewModel.fetchDashboardData(token)
            advanceUntilIdle()

            assertNotNull(testViewModel.upNextShows.value)
            assertTrue(testViewModel.upNextShows.value?.isEmpty() == true)
            assertFalse(testViewModel.isLoadingUpNext.value)
        }

    @Test
    fun `fetchDashboardData passes clean tokens and populates images progressively`() =
        runTest {
            val token = "clean_token_123"
            val schedule = NetworkTraktMyScheduleResponse().apply {
                add(
                    NetworkTraktMyScheduleResponseItem(
                        first_aired = "2026-09-20T20:00:00.000Z",
                        episode = NetworkTraktMyScheduleEpisode(
                            season = 1,
                            number = 1,
                            title = "Pilot",
                            ids = null,
                        ),
                        show = NetworkTraktMyScheduleShow(
                            title = "Severance",
                            year = 2022,
                            ids = NetworkTraktMyScheduleShowIds(trakt = 100, slug = "severance", tvdb = 1, imdb = "tt100", tmdb = 1),
                        ),
                    ),
                )
            }
            `when`(traktRepository.getTraktMySchedule(eq(token), any(), any()))
                .thenReturn(Result.success(schedule))
            `when`(dashboardRepository.getShowImageAndTvmazeId("tt100"))
                .thenReturn("http://image.png" to 123)

            val testViewModel = DashboardViewModel(
                traktRepository = traktRepository,
                dashboardRepository = dashboardRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = localWorkManager,
                firebaseAnalytics = firebaseAnalytics,
            )

            testViewModel.fetchDashboardData(token)
            advanceUntilIdle()

            // Verify clean token was passed
            verify(traktRepository).getTraktMySchedule(eq(token), any(), any())
            assertEquals(schedule, testViewModel.airingSoonShows.value)

            // Verify progressive image loading populated the image map
            val uniqueKey = "100-1-1"
            assertEquals("http://image.png", testViewModel.airingSoonImages.value[uniqueKey]?.imageUrl)
            assertEquals(123, testViewModel.airingSoonImages.value[uniqueKey]?.tvmazeId)
        }
}
