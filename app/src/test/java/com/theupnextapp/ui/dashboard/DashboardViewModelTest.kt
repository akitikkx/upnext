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
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponse
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.ProviderManager
import com.theupnextapp.repository.SimklAuthManager
import com.theupnextapp.repository.SimklRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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

@ExperimentalCoroutinesApi
class DashboardViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var traktRepository: TraktRepository
    private lateinit var simklRepository: SimklRepository
    private lateinit var simklAuthManager: SimklAuthManager
    private lateinit var providerManager: ProviderManager
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var watchProgressRepository: WatchProgressRepository
    private lateinit var localWorkManager: WorkManager
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        traktRepository = mock(TraktRepository::class.java)
        simklRepository = mock(SimklRepository::class.java)
        simklAuthManager = mock(SimklAuthManager::class.java)
        providerManager = mock(ProviderManager::class.java)
        dashboardRepository = mock(DashboardRepository::class.java)
        watchProgressRepository = mock(WatchProgressRepository::class.java)
        localWorkManager = mock(WorkManager::class.java)
        firebaseAnalytics = mock(FirebaseAnalytics::class.java)

        `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))
        `when`(simklAuthManager.simklAccessToken).thenReturn(MutableStateFlow(null))
        `when`(providerManager.activeProvider).thenReturn(MutableStateFlow("trakt"))
        `when`(traktRepository.traktMostAnticipatedShows).thenReturn(flowOf(emptyList()))
        `when`(simklRepository.premieresShows).thenReturn(MutableStateFlow(emptyList()))
        `when`(simklRepository.isLoadingPremieres).thenReturn(MutableStateFlow(false))
        kotlinx.coroutines.runBlocking {
            `when`(traktRepository.getRegionalTrendingShows(any())).thenReturn(
                Result.success(
                    emptyList(),
                ),
            )
        }
        `when`(dashboardRepository.todayShows).thenReturn(flowOf(emptyList()))
        `when`(watchProgressRepository.isSyncing).thenReturn(MutableStateFlow(false))

        viewModel =
            DashboardViewModel(
                traktRepository = traktRepository,
                simklRepository = simklRepository,
                simklAuthManager = simklAuthManager,
                providerManager = providerManager,
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
                    simklRepository = simklRepository,
                    simklAuthManager = simklAuthManager,
                    providerManager = providerManager,
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
                    simklRepository = simklRepository,
                    simklAuthManager = simklAuthManager,
                    providerManager = providerManager,
                    dashboardRepository = dashboardRepository,
                    watchProgressRepository = watchProgressRepository,
                    localWorkManager = localWorkManager,
                    firebaseAnalytics = firebaseAnalytics,
                )

            // Use a specific non-null invocation since workManager.enqueue expects a non-null WorkRequest
            // Because WorkManager enqueue returns an Operation, we must mock it so it doesn't crash on execution via lazy eval
            val mockOperation = mock(Operation::class.java)
            `when`(localWorkManager.enqueue(any<WorkRequest>())).thenReturn(mockOperation)

            testViewModel.onMarkEpisodeWatched(
                showTvMazeId = 1,
                imdbId = "tt123",
                showTraktId = 123,
                season = 1,
                number = 1,
            )

            // Verify work manager was told to enqueue the sync
            verify(localWorkManager).enqueue(any<WorkRequest>())
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
                    simklRepository = simklRepository,
                    simklAuthManager = simklAuthManager,
                    providerManager = providerManager,
                    dashboardRepository = dashboardRepository,
                    watchProgressRepository = watchProgressRepository,
                    localWorkManager = localWorkManager,
                    firebaseAnalytics = firebaseAnalytics,
                )

            // Trigger sync start
            syncStateFlow.value = true

            // Trigger sync complete
            syncStateFlow.value = false

            // Verify it asked trakt for history refresh using the token
            verify(traktRepository).getTraktRecentHistory("mock_token")
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
            `when`(traktRepository.getTraktRecentHistory(token)).thenReturn(Result.success(listOf()))

            val testViewModel = DashboardViewModel(
                traktRepository = traktRepository,
                simklRepository = simklRepository,
                simklAuthManager = simklAuthManager,
                providerManager = providerManager,
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

            // Explicitly verify the states have values so the guard should be active
            assertNotNull(testViewModel.airingSoonShows.value)
            assertNotNull(testViewModel.recommendedShows.value)

            // Second invocation should NOT trigger the fetches again because of the guard
            testViewModel.fetchDashboardData(token)

            advanceUntilIdle()

            // Verify they were STILL only called once total
            verify(traktRepository, Mockito.times(1)).getTraktMySchedule(any(), any(), any())
            verify(traktRepository, Mockito.times(1)).getTraktRecommendations(token)
        }

    @Test
    fun `when fetchDashboardHistoryData is invoked multiple times, guards prevent redundant network calls`() =
        runTest {
            val token = "mock_token"

            // Setup responses for the repository
            `when`(traktRepository.getTraktRecentHistory(token)).thenReturn(Result.success(listOf()))

            val testViewModel = DashboardViewModel(
                traktRepository = traktRepository,
                simklRepository = simklRepository,
                simklAuthManager = simklAuthManager,
                providerManager = providerManager,
                dashboardRepository = dashboardRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = localWorkManager,
                firebaseAnalytics = firebaseAnalytics,
            )

            // Override provider and token specifically for this internal launch
            `when`(providerManager.activeProvider).thenReturn(MutableStateFlow("trakt"))
            `when`(traktRepository.traktAccessToken).thenReturn(
                MutableStateFlow(
                    TraktAccessToken(
                        access_token = token,
                        created_at = 1,
                        expires_in = 1,
                        refresh_token = "",
                        scope = "",
                        token_type = ""
                    )
                )
            )

            // First invocation should trigger the fetches
            testViewModel.fetchDashboardHistoryData()

            // Wait for coroutines to complete
            advanceUntilIdle()

            verify(traktRepository, Mockito.times(1)).getTraktRecentHistory(token)

            // Explicitly verify the states have values so the guard should be active
            assertNotNull(testViewModel.recentHistory.value)

            // Second invocation should NOT trigger the fetches again because of the guard
            testViewModel.fetchDashboardHistoryData()

            advanceUntilIdle()

            // Verify they were STILL only called once total
            verify(traktRepository, Mockito.times(1)).getTraktRecentHistory(token)
        }

    @Test
    fun `viewModel fetches regional trending shows on init`() =
        runTest {
            val countryCode = java.util.Locale.getDefault().country

            // Verify that the repository method was called with the default country code
            verify(traktRepository, Mockito.times(1)).getRegionalTrendingShows(countryCode)

            // Give flows time to emit initial states
            advanceUntilIdle()

            // Since we mocked success with emptyList(), regionalTrendingShows should not be null
            assertNotNull(viewModel.regionalTrendingShows.value)
        }

    @Test
    fun `isAuthorizedOnProvider reflects true when active provider is Trakt and Trakt is authenticated`() = runTest {
        `when`(providerManager.activeProvider).thenReturn(MutableStateFlow(com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT))
        val token = TraktAccessToken(
            access_token = "trakt_token",
            created_at = 1,
            expires_in = 1,
            refresh_token = "",
            scope = "",
            token_type = ""
        )
        `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(token))
        `when`(simklAuthManager.simklAccessToken).thenReturn(MutableStateFlow(null))

        val testViewModel = DashboardViewModel(
            traktRepository = traktRepository, simklRepository = simklRepository, simklAuthManager = simklAuthManager,
            providerManager = providerManager, dashboardRepository = dashboardRepository, watchProgressRepository = watchProgressRepository,
            localWorkManager = localWorkManager, firebaseAnalytics = firebaseAnalytics
        )

        advanceUntilIdle()
        assertTrue(testViewModel.isAuthorizedOnProvider.first())
    }

    @Test
    fun `isAuthorizedOnProvider reflects false when active provider is Trakt and Trakt is NOT authenticated`() = runTest {
        `when`(providerManager.activeProvider).thenReturn(MutableStateFlow(com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT))
        `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))
        val simklToken = com.theupnextapp.domain.SimklAccessToken(accessToken = "simkl_token", tokenType = "Bearer", scope = "public")
        `when`(simklAuthManager.simklAccessToken).thenReturn(MutableStateFlow(simklToken))

        val testViewModel = DashboardViewModel(
            traktRepository = traktRepository, simklRepository = simklRepository, simklAuthManager = simklAuthManager,
            providerManager = providerManager, dashboardRepository = dashboardRepository, watchProgressRepository = watchProgressRepository,
            localWorkManager = localWorkManager, firebaseAnalytics = firebaseAnalytics
        )

        advanceUntilIdle()
        assertTrue(!testViewModel.isAuthorizedOnProvider.first())
    }

    @Test
    fun `isAuthorizedOnProvider reflects true when active provider is SIMKL and SIMKL is authenticated`() = runTest {
        `when`(providerManager.activeProvider).thenReturn(MutableStateFlow(com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL))
        `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))
        val simklToken = com.theupnextapp.domain.SimklAccessToken(accessToken = "simkl_token", tokenType = "Bearer", scope = "public")
        `when`(simklAuthManager.simklAccessToken).thenReturn(MutableStateFlow(simklToken))

        val testViewModel = DashboardViewModel(
            traktRepository = traktRepository, simklRepository = simklRepository, simklAuthManager = simklAuthManager,
            providerManager = providerManager, dashboardRepository = dashboardRepository, watchProgressRepository = watchProgressRepository,
            localWorkManager = localWorkManager, firebaseAnalytics = firebaseAnalytics
        )

        advanceUntilIdle()
        assertTrue(testViewModel.isAuthorizedOnProvider.first())
    }

    @Test
    fun `when simkl access token is present and active provider is SIMKL, fetches simkl dashboard`() = runTest {
        val simklToken = com.theupnextapp.domain.SimklAccessToken(accessToken = "simkl_token", tokenType = "Bearer", scope = "public")
        `when`(simklAuthManager.simklAccessToken).thenReturn(MutableStateFlow(simklToken))
        `when`(providerManager.activeProvider).thenReturn(MutableStateFlow(com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL))

        val testViewModel = DashboardViewModel(
            traktRepository = traktRepository, simklRepository = simklRepository, simklAuthManager = simklAuthManager,
            providerManager = providerManager, dashboardRepository = dashboardRepository, watchProgressRepository = watchProgressRepository,
            localWorkManager = localWorkManager, firebaseAnalytics = firebaseAnalytics
        )

        testViewModel.fetchSimklDashboardData()
        advanceUntilIdle()

        verify(simklRepository, Mockito.times(1)).refreshPremieres()
    }
}
