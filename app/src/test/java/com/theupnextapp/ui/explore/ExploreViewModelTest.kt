/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
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

package com.theupnextapp.ui.explore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.repository.fakes.FakeTraktRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ExploreViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: ExploreViewModel
    private lateinit var fakeRepository: FakeTraktRepository
    private val firebaseAnalytics: FirebaseAnalytics = org.mockito.Mockito.mock(FirebaseAnalytics::class.java)

    @Before
    fun setup() {
        fakeRepository = FakeTraktRepository()
        viewModel = ExploreViewModel(fakeRepository, firebaseAnalytics)
    }

    @Test
    fun `trendingShows returns data from repository`() =
        runTest {
            // Given
            val shows =
                listOf(
                    TraktTrendingShows(
                        id = 1,
                        title = "Breaking Bad",
                        year = "2008",
                        mediumImageUrl = "url",
                        originalImageUrl = "url",
                        imdbID = "tt0903747",
                        slug = "breaking-bad",
                        tmdbID = 1396,
                        traktID = 1388,
                        tvdbID = 81189,
                        tvMazeID = 169,
                    ),
                )
            fakeRepository.setTrendingShows(shows)

            // Then
            assertEquals(shows, viewModel.trendingShows.first())
        }

    @Test
    fun `popularShows returns data from repository`() =
        runTest {
            // Given
            val shows =
                listOf(
                    TraktPopularShows(
                        id = 2,
                        title = "Game of Thrones",
                        year = "2011",
                        mediumImageUrl = "url",
                        originalImageUrl = "url",
                        imdbID = "tt0944947",
                        slug = "game-of-thrones",
                        tmdbID = 1399,
                        traktID = 1390,
                        tvdbID = 121361,
                        tvMazeID = 82,
                    ),
                )
            fakeRepository.setPopularShows(shows)

            // Then
            assertEquals(shows, viewModel.popularShows.first())
        }

    @Test
    fun `mostAnticipatedShows returns data from repository`() =
        runTest {
            // Given
            val shows =
                listOf(
                    TraktMostAnticipated(
                        id = 3,
                        title = "House of the Dragon",
                        year = "2022",
                        mediumImageUrl = "url",
                        originalImageUrl = "url",
                        imdbID = "tt11198330",
                        slug = "house-of-the-dragon",
                        tmdbID = 94997,
                        traktID = 155590,
                        tvdbID = 371572,
                        tvMazeID = 44778,
                    ),
                )
            fakeRepository.setMostAnticipatedShows(shows)

            // Then
            assertEquals(shows, viewModel.mostAnticipatedShows.first())
        }

    @Test
    fun `isLoading is true when trending is loading`() =
        runTest {
            // Given
            fakeRepository.setLoadingTrending(true)
            fakeRepository.setLoadingPopular(false)
            fakeRepository.setLoadingMostAnticipated(false)

            // Then
            assertTrue(viewModel.isLoading.first())
        }

    @Test
    fun `isLoading is false when nothing is loading`() =
        runTest {
            // Given
            fakeRepository.setLoadingTrending(false)
            fakeRepository.setLoadingPopular(false)
            fakeRepository.setLoadingMostAnticipated(false)

            // Then
            assertFalse(viewModel.isLoading.first())
        }

    @Test
    fun `trendingShowsEmpty is true when trending list is empty`() =
        runTest {
            // Default state is empty
            assertTrue(viewModel.trendingShowsEmpty.first())
        }

    @Test
    fun `trendingShowsEmpty is false when trending list has data`() =
        runTest {
            // Given
            fakeRepository.setTrendingShows(
                listOf(
                    TraktTrendingShows(
                        id = 1,
                        title = "Test",
                        year = "2024",
                        mediumImageUrl = null,
                        originalImageUrl = null,
                        imdbID = null,
                        slug = null,
                        tmdbID = null,
                        traktID = null,
                        tvdbID = null,
                        tvMazeID = null,
                    ),
                ),
            )

            // Then
            assertFalse(viewModel.trendingShowsEmpty.first())
        }
}
