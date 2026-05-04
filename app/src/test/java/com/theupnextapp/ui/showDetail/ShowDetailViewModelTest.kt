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

package com.theupnextapp.ui.showDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.domain.TraktRelatedShows
import com.theupnextapp.repository.fakes.FakeShowDetailRepository
import com.theupnextapp.repository.fakes.FakeTraktRepository
import com.theupnextapp.work.AddToWatchlistWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.Result as StdResult

@ExperimentalCoroutinesApi
class ShowDetailViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    val workManager: WorkManager = mock()
    val firebaseCrashlytics: FirebaseCrashlytics = mock()
    val traktAuthManager: TraktAuthManager = mock()
    val firebaseAnalytics: com.google.firebase.analytics.FirebaseAnalytics = mock()
    val providerManager: com.theupnextapp.repository.ProviderManager = mock()
    val simklRepository: com.theupnextapp.repository.SimklRepository = mock()
    val simklAuthManager: com.theupnextapp.repository.SimklAuthManager = mock()

    private lateinit var showDetailRepository: FakeShowDetailRepository
    private lateinit var traktRepository: FakeTraktRepository

    private lateinit var viewModel: ShowDetailViewModel

    @Before
    fun setup() {
        showDetailRepository = FakeShowDetailRepository()
        traktRepository = FakeTraktRepository()

        whenever(traktAuthManager.traktAuthState).thenReturn(MutableStateFlow(TraktAuthState.LoggedIn))
        whenever(providerManager.activeProvider).thenReturn(MutableStateFlow(com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT))

        viewModel =
            ShowDetailViewModel(
                showDetailRepository,
                workManager,
                traktRepository,
                simklRepository,
                providerManager,
                simklAuthManager,
                firebaseCrashlytics,
                firebaseAnalytics,
                traktAuthManager,
            )
    }

    @Test
    fun `similarShows_success updates ui state`() =
        runTest {
            // Given
            val imdbId = "tt12345"
            val showDetailArg =
                ShowDetailArg(
                    showId = "123",
                    showTitle = "Test Show",
                    showImageUrl = null,
                    showBackgroundUrl = null,
                    imdbID = imdbId,
                    isAuthorizedOnTrakt = false,
                    showTraktId = 1,
                )

            val testRelatedShows =
                listOf(
                    TraktRelatedShows(
                        title = "Related Show",
                        year = "2024",
                        traktID = 2,
                        slug = "slug",
                        imdbID = "tt2",
                        originalImageUrl = null,
                        mediumImageUrl = null,
                        tvMazeID = null,
                        tmdbID = null,
                        tvdbID = null,
                        id = 2,
                    ),
                )
            traktRepository.relatedShowsResult = StdResult.success(testRelatedShows)

            // When
            viewModel.selectedShow(showDetailArg)

            // Let coroutines settle
            kotlinx.coroutines.delay(100)

            // Then
            val state = viewModel.uiState.value
            assertNotNull("Similar shows should not be null", state.similarShows)
            assertEquals("Should have exactly 1 similar show", 1, state.similarShows?.size)
            assertEquals("Related Show", state.similarShows?.firstOrNull()?.title)
        }

    @Test
    fun `selectedShow fetches certification and updates ui state`() =
        runTest {
            // Given
            val imdbId = "tt12345"
            val showDetailArg =
                ShowDetailArg(
                    showId = "123",
                    showTitle = "Test Show",
                    showImageUrl = null,
                    showBackgroundUrl = null,
                    imdbID = imdbId,
                    isAuthorizedOnTrakt = false,
                    showTraktId = 1,
                )

            val testCertification = "TV-MA"
            traktRepository.certificationResult = StdResult.success(testCertification)

            // When
            viewModel.selectedShow(showDetailArg)

            // Let coroutines settle
            kotlinx.coroutines.delay(100)

            // Then
            val state = viewModel.uiState.value
            assertEquals("Certification should be updated in ui state", testCertification, state.certification)
        }

    @Test
    fun `onAddRemoveFavoriteClick queues work when logged in`() =
        runTest {
            // Given
            val imdbId = "tt12345"
            val token = "test_token"

            traktRepository.setAccessToken(
                TraktAccessToken(
                    access_token = token,
                    token_type = "bearer",
                    expires_in = 1234,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 123,
                ),
            )

            val showDetailArg =
                ShowDetailArg(
                    showId = "123",
                    showTitle = "Test Show",
                    showImageUrl = null,
                    showBackgroundUrl = null,
                    imdbID = imdbId,
                    isAuthorizedOnTrakt = true,
                    showTraktId = 1,
                )

            showDetailRepository.showSummaryResult =
                Result.Success(
                    ShowDetailSummary(
                        id = 123,
                        imdbID = imdbId,
                        name = "Test Show",
                        averageRating = null,
                        mediumImageUrl = null,
                        originalImageUrl = null,
                        summary = "Summary",
                        genres = null,
                        time = null,
                        previousEpisodeHref = null,
                        nextEpisodeHref = null,
                        status = null,
                        airDays = null,
                        language = null,
                        nextEpisodeLinkedId = null,
                        previousEpisodeLinkedId = null,
                        tmdbID = 123,
                        network = "ABC",
                        premiered = "2024-01-01",
                    ),
                )

            viewModel.selectedShow(showDetailArg)

            kotlinx.coroutines.delay(100)

            viewModel.onAddRemoveWatchlistClick()

            kotlinx.coroutines.delay(100)

            val requestCaptor = org.mockito.kotlin.argumentCaptor<OneTimeWorkRequest>()
            verify(workManager, timeout(3000)).enqueue(requestCaptor.capture())

            val enqueuedWork = requestCaptor.firstValue
            val inputData = enqueuedWork.workSpec.input

            assertNotNull("Work input data should not be null", inputData)
            assertEquals("IMDb ID should match", imdbId, inputData.getString(AddToWatchlistWorker.ARG_IMDB_ID))
            assertEquals("Trakt ID should match", 1, inputData.getInt(AddToWatchlistWorker.ARG_TRAKT_ID, -1))
            assertEquals("Token should match", token, inputData.getString(AddToWatchlistWorker.ARG_TOKEN))
            assertEquals("TVMaze ID should match", 123, inputData.getInt(AddToWatchlistWorker.ARG_TVMAZE_ID, -1))
            assertEquals("TMDB ID should match", 123, inputData.getInt(AddToWatchlistWorker.ARG_TMDB_ID, -1))
            assertEquals("Year should match", "2024", inputData.getString(AddToWatchlistWorker.ARG_YEAR))
            assertEquals("Network should match", "ABC", inputData.getString(AddToWatchlistWorker.ARG_NETWORK))
        }

    @Test
    fun `selectedShow with null sets generalErrorMessage`() =
        runTest {
            // When
            viewModel.selectedShow(null)

            kotlinx.coroutines.delay(100)

            // Then
            val state = viewModel.uiState.value
            assertEquals("No show information provided.", state.generalErrorMessage)
        }

    @Test
    fun `selectedShow with missing showId and imdbID shows error`() =
        runTest {
            // Given — showId and imdbID are both null
            val showDetailArg =
                ShowDetailArg(
                    showId = null,
                    showTitle = "Test Show",
                    showImageUrl = null,
                    showBackgroundUrl = null,
                    imdbID = null,
                    isAuthorizedOnTrakt = false,
                    showTraktId = null,
                )

            // When
            viewModel.selectedShow(showDetailArg)

            kotlinx.coroutines.delay(100)

            // Then
            val state = viewModel.uiState.value
            assertEquals("This show is missing a required TVMaze or IMDB ID.", state.generalErrorMessage)
        }

    @Test
    fun `selectedShow with string 'null' showId and valid imdbID executes fallback lookup`() =
        runTest {
            // Given - showId is the literal string "null" (as passed by Compose nav sometimes)
            val imdbId = "tt12345"
            val showDetailArg =
                ShowDetailArg(
                    showId = "null",
                    showTitle = "Test Show",
                    showImageUrl = null,
                    showBackgroundUrl = null,
                    imdbID = imdbId,
                    isAuthorizedOnTrakt = false,
                    showTraktId = null,
                )

            val mappedShow = mock<com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse>()
            whenever(mappedShow.id).thenReturn(999)
            showDetailRepository.showLookupResult = Result.Success(mappedShow)

            // Setup the final fetch via that ID
            showDetailRepository.showSummaryResult = Result.Success(
                ShowDetailSummary(
                    id = 999,
                    imdbID = imdbId,
                    name = "Test Show via IMDB",
                    averageRating = null,
                    mediumImageUrl = null,
                    originalImageUrl = null,
                    summary = "Resolved via IMDB fallback",
                    genres = null,
                    time = null,
                    previousEpisodeHref = null,
                    nextEpisodeHref = null,
                    status = null,
                    airDays = null,
                    language = null,
                    nextEpisodeLinkedId = null,
                    previousEpisodeLinkedId = null,
                    tmdbID = null,
                    network = null,
                    premiered = null,
                )
            )

            // When
            viewModel.selectedShow(showDetailArg)

            kotlinx.coroutines.delay(200)

            // Then
            val state = viewModel.uiState.value
            assertNotNull("State summary should not be null after fallback", state.showSummary)
            assertEquals("Test Show via IMDB", state.showSummary?.name)
        }

    @Test
    fun `onAddRemoveFavoriteClick with null token shows login message`() =
        runTest {
            // Given — no access token set (default null)
            val showDetailArg =
                ShowDetailArg(
                    showId = "123",
                    showTitle = "Test Show",
                    showImageUrl = null,
                    showBackgroundUrl = null,
                    imdbID = "tt12345",
                    isAuthorizedOnTrakt = false,
                    showTraktId = 1,
                )

            showDetailRepository.showSummaryResult =
                Result.Success(
                    ShowDetailSummary(
                        id = 123,
                        imdbID = "tt12345",
                        name = "Test Show",
                        averageRating = null,
                        mediumImageUrl = null,
                        originalImageUrl = null,
                        summary = "Summary",
                        genres = null,
                        time = null,
                        previousEpisodeHref = null,
                        nextEpisodeHref = null,
                        status = null,
                        airDays = null,
                        language = null,
                        nextEpisodeLinkedId = null,
                        previousEpisodeLinkedId = null,
                        tmdbID = 123,
                        network = null,
                        premiered = null,
                    ),
                )

            viewModel.selectedShow(showDetailArg)
            kotlinx.coroutines.delay(200)

            // When
            viewModel.onAddRemoveWatchlistClick()

            // Then — wait reactively for the state to be set via Dispatchers.IO
            val state =
                kotlinx.coroutines.withTimeout(3000) {
                    viewModel.uiState.first { it.generalErrorMessage != null }
                }
            assertEquals("Please log in to Trakt to manage watchlists.", state.generalErrorMessage)
        }

    @Test
    fun `onSeasonsClick sets navigateToSeasons to true`() =
        runTest {
            // When
            viewModel.onSeasonsClick()

            // Then
            assertTrue(viewModel.navigateToSeasons.value)
        }

    @Test
    fun `onSeasonsNavigationComplete resets navigateToSeasons to false`() =
        runTest {
            // Given
            viewModel.onSeasonsClick()
            assertTrue(viewModel.navigateToSeasons.value)

            // When
            viewModel.onSeasonsNavigationComplete()

            // Then
            assertFalse(viewModel.navigateToSeasons.value)
        }

    @Test
    fun `selectedShow with identical show does not trigger network calls repeatedly on rotation`() =
        runTest {
            // Given
            val showDetailArg =
                ShowDetailArg(
                    showId = "123",
                    showTitle = "Test Show",
                    showImageUrl = null,
                    showBackgroundUrl = null,
                    imdbID = "tt12345",
                    isAuthorizedOnTrakt = false,
                    showTraktId = 1,
                )

            showDetailRepository.showSummaryResult =
                Result.Success(
                    ShowDetailSummary(
                        id = 123,
                        imdbID = "tt12345",
                        name = "Test Show",
                        averageRating = null,
                        mediumImageUrl = null,
                        originalImageUrl = null,
                        summary = "Summary",
                        genres = null,
                        time = null,
                        previousEpisodeHref = null,
                        nextEpisodeHref = null,
                        status = null,
                        airDays = null,
                        language = null,
                        nextEpisodeLinkedId = null,
                        previousEpisodeLinkedId = null,
                        tmdbID = 123,
                        network = null,
                        premiered = null,
                    ),
                )

            // When - First load
            viewModel.selectedShow(showDetailArg)
            kotlinx.coroutines.delay(100)

            // Then - Check state exists
            val initialState = viewModel.uiState.value
            assertNotNull("Initial state summary should not be null", initialState.showSummary)
            assertEquals("Test Show", initialState.showSummary?.name)

            // When - Simulate rotation by calling selectedShow again with same args
            viewModel.selectedShow(showDetailArg)

            // Then - State should not be wiped out (isLoadingSummary false and summary intact)
            val rotatedState = viewModel.uiState.value
            assertFalse("Simulated rotation should skip loading and preserve state", rotatedState.isLoadingSummary)
            assertNotNull("Rotated state summary should be preserved", rotatedState.showSummary)
            assertEquals("Test Show", rotatedState.showSummary?.name)
        }
}
