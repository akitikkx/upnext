package com.theupnextapp.ui.watchHistory

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedEpisode
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowInfo
import com.theupnextapp.repository.fakes.FakeDashboardRepository
import com.theupnextapp.repository.fakes.FakeTraktRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class WatchHistoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var traktRepository: FakeTraktRepository
    private lateinit var dashboardRepository: FakeDashboardRepository

    @Mock
    private lateinit var traktAuthManager: TraktAuthManager

    @Mock
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val traktAuthStateFlow = MutableStateFlow<TraktAuthState>(TraktAuthState.LoggedIn)

    private val sampleHistoryItem1 =
        NetworkTraktHistoryResponse(
            id = 1L,
            watchedAt = "2026-09-15T20:30:00.000Z",
            action = "watch",
            type = "episode",
            show =
                NetworkTraktWatchedShowInfo(
                    title = "Severance",
                    year = 2022,
                    ids = NetworkTraktWatchedShowIds(trakt = 100, slug = "severance", tvdb = 1, imdb = "tt100", tmdb = 1),
                ),
            episode =
                NetworkTraktWatchedEpisode(
                    season = 1,
                    number = 1,
                    title = "Good News About Hell",
                    plays = 1,
                    lastWatchedAt = "2026-09-15T20:30:00.000Z",
                ),
        )

    private val sampleHistoryItem2 =
        NetworkTraktHistoryResponse(
            id = 2L,
            watchedAt = "2026-08-10T19:00:00.000Z",
            action = "watch",
            type = "episode",
            show =
                NetworkTraktWatchedShowInfo(
                    title = "Slow Horses",
                    year = 2022,
                    ids = NetworkTraktWatchedShowIds(trakt = 200, slug = "slow-horses", tvdb = 2, imdb = "tt200", tmdb = 2),
                ),
            episode =
                NetworkTraktWatchedEpisode(
                    season = 1,
                    number = 1,
                    title = "Failure's Contagious",
                    plays = 1,
                    lastWatchedAt = "2026-08-10T19:00:00.000Z",
                ),
        )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        traktRepository = FakeTraktRepository()
        dashboardRepository = FakeDashboardRepository()

        whenever(traktAuthManager.traktAuthState).thenReturn(traktAuthStateFlow)
        traktRepository.setAccessToken(
            TraktAccessToken(
                access_token = "valid_token",
                token_type = "Bearer",
                expires_in = 7200,
                refresh_token = "refresh_token",
                scope = "public",
                created_at = 1000,
            ),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): WatchHistoryViewModel {
        return WatchHistoryViewModel(
            traktRepository = traktRepository,
            dashboardRepository = dashboardRepository,
            traktAuthManager = traktAuthManager,
            firebaseAnalytics = firebaseAnalytics,
        )
    }

    @Test
    fun `initial state loads first page and groups items by Month and Year`() = runTest {
        traktRepository.recentHistoryResult = Result.success(listOf(sampleHistoryItem1, sampleHistoryItem2))

        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertTrue(state.groupedItems.containsKey("September 2026"))
        assertTrue(state.groupedItems.containsKey("August 2026"))
        assertEquals("Severance", state.groupedItems["September 2026"]?.first()?.showTitle)
        assertEquals("Slow Horses", state.groupedItems["August 2026"]?.first()?.showTitle)
        verify(firebaseAnalytics).logEvent(eq("watch_history_page_loaded"), any())
    }

    @Test
    fun `loadNextPage appends subsequent page items to existing diary`() = runTest {
        // First page returns 30 items to avoid triggering endOfListReached
        val firstPageList = List(30) { index ->
            NetworkTraktHistoryResponse(
                id = index.toLong(),
                watchedAt = "2026-09-15T20:30:00.000Z",
                action = "watch",
                type = "episode",
                show = NetworkTraktWatchedShowInfo(
                    title = "Show $index",
                    year = 2022,
                    ids = NetworkTraktWatchedShowIds(trakt = index, slug = "show-$index", tvdb = index, imdb = "tt$index", tmdb = index),
                ),
                episode = NetworkTraktWatchedEpisode(
                    season = 1,
                    number = 1,
                    title = "Ep 1",
                    plays = 1,
                    lastWatchedAt = "2026-09-15T20:30:00.000Z",
                ),
            )
        }
        traktRepository.recentHistoryResult = Result.success(firstPageList)

        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        assertEquals(30, viewModel.uiState.value.items.size)

        // Load next page
        val secondPageList = listOf(sampleHistoryItem2)
        traktRepository.recentHistoryResult = Result.success(secondPageList)

        viewModel.loadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(31, state.items.size)
        assertTrue(state.endOfListReached)
    }

    @Test
    fun `searchQuery filters history items by show title in real time`() = runTest {
        traktRepository.recentHistoryResult = Result.success(listOf(sampleHistoryItem1, sampleHistoryItem2))

        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("sever")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Severance", state.items.first().showTitle)
        verify(firebaseAnalytics).logEvent(eq("watch_history_search"), any())
    }

    @Test
    fun `searchQuery cleared restores full chronological history list`() = runTest {
        traktRepository.recentHistoryResult = Result.success(listOf(sampleHistoryItem1, sampleHistoryItem2))

        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("sever")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.items.size)

        viewModel.onSearchQueryChange("")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.items.size)
    }

    @Test
    fun `unauthorized user does not load history and sets state to unauthorized`() = runTest {
        traktAuthStateFlow.value = TraktAuthState.LoggedOut
        traktRepository.setAccessToken(null)

        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthorized)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun `API error sets error message and retry reloads history`() = runTest {
        traktRepository.recentHistoryResult = Result.failure(Exception("Network Timeout"))

        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertNotNull(errorState.errorMessage)
        assertEquals("Network Timeout", errorState.errorMessage)

        // Retry succeeds
        traktRepository.recentHistoryResult = Result.success(listOf(sampleHistoryItem1))
        viewModel.loadFirstPage()
        advanceUntilIdle()

        val successState = viewModel.uiState.value
        assertEquals(null, successState.errorMessage)
        assertEquals(1, successState.items.size)
    }
}
