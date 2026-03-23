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

@ExperimentalCoroutinesApi
class ShowDetailViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    val workManager: WorkManager = mock()
    val firebaseCrashlytics: FirebaseCrashlytics = mock()
    val traktAuthManager: TraktAuthManager = mock()

    private lateinit var showDetailRepository: FakeShowDetailRepository
    private lateinit var traktRepository: FakeTraktRepository

    private lateinit var viewModel: ShowDetailViewModel

    @Before
    fun setup() {
        showDetailRepository = FakeShowDetailRepository()
        traktRepository = FakeTraktRepository()

        whenever(traktAuthManager.traktAuthState).thenReturn(MutableStateFlow(TraktAuthState.LoggedIn))

        viewModel =
            ShowDetailViewModel(
                showDetailRepository,
                workManager,
                traktRepository,
                firebaseCrashlytics,
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
            traktRepository.relatedShowsResult = kotlin.Result.success(testRelatedShows)

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
            assertEquals("IMDb ID should match", imdbId, inputData.getString(com.theupnextapp.work.AddToWatchlistWorker.ARG_IMDB_ID))
            assertEquals("Trakt ID should match", 1, inputData.getInt(com.theupnextapp.work.AddToWatchlistWorker.ARG_TRAKT_ID, -1))
            assertEquals("Token should match", token, inputData.getString(com.theupnextapp.work.AddToWatchlistWorker.ARG_TOKEN))
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
            assertEquals("Show ID is missing.", state.generalErrorMessage)
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
}
