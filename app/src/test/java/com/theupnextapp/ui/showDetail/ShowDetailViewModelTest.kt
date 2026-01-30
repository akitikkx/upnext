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
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.domain.TraktCast
import com.theupnextapp.network.models.trakt.NetworkTraktPersonResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonShowCreditsResponse
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.AddFavoriteShowWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class ShowDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Mock
    lateinit var showDetailRepository: ShowDetailRepository

    @Mock
    lateinit var workManager: WorkManager

    @Mock
    lateinit var traktRepository: TraktRepository

    @Mock
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    @Mock
    lateinit var traktAuthManager: TraktAuthManager

    private lateinit var viewModel: ShowDetailViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(traktAuthManager.traktAuthState).thenReturn(MutableStateFlow(TraktAuthState.LoggedIn))
        // Stub the repository flow to return an empty flow by default, but mutable for tests
        // But for constructor it's fine.
        
        // Mock default behavior for other flows to prevent NPE in init
        `when`(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))
        `when`(traktRepository.favoriteShow).thenReturn(MutableStateFlow(null))
        `when`(traktRepository.traktShowRating).thenReturn(MutableStateFlow(null))
        `when`(traktRepository.traktShowStats).thenReturn(MutableStateFlow(null))
        
        viewModel = ShowDetailViewModel(
            showDetailRepository,
            workManager,
            traktRepository,
            firebaseCrashlytics,
            traktAuthManager
        )
    }

    private fun createTraktCast(traktId: Int, name: String): TraktCast {
        return TraktCast(
            character = "Test Character",
            name = name,
            originalImageUrl = null,
            mediumImageUrl = null,
            traktId = traktId,
            imdbId = null,
            slug = null
        )
    }

    @Test
    fun `onShowCastItemClicked uses Trakt ID directly`() = runTest {
        // Given
        val traktId = 456
        val actorName = "Test Actor"
        val traktCast = createTraktCast(traktId, actorName)

        `when`(
            traktRepository.getTraktPersonSummary(traktId.toString())
        ).thenReturn(
            Result.success(
                NetworkTraktPersonResponse(name = "Test Actor", ids = null, biography = null, birthday = null, death = null, birthplace = null, homepage = null, gender = null, known_for_department = null, social_ids = null)
            )
        )
        `when`(
            traktRepository.getTraktPersonShowCredits(traktId.toString())
        ).thenReturn(Result.success(NetworkTraktPersonShowCreditsResponse(cast = emptyList())))

        // When
        viewModel.onShowCastItemClicked(traktCast)

        // Then
        // Should NOT call lookups
        verify(traktRepository, never()).getTraktPersonIdLookup(anyString())
        verify(traktRepository, never()).getTraktPersonIdSearch(anyString())

        // Should call summary and credits directly
        verify(traktRepository, timeout(3000)).getTraktPersonSummary(traktId.toString())
        verify(traktRepository, timeout(3000)).getTraktPersonShowCredits(traktId.toString())
    }

    @Test
    fun `similarShows_success`() = runTest {
        // Given
        val imdbId = "tt12345"
        val showDetailArg = com.theupnextapp.domain.ShowDetailArg(
            showId = "123",
            showTitle = "Test Show",
            showImageUrl = null,
            showBackgroundUrl = null,
            imdbID = imdbId,
            isAuthorizedOnTrakt = false,
            showTraktId = 1
        )
        val showSummary = com.theupnextapp.domain.ShowDetailSummary(
            id = 123,
            imdbID = imdbId,
            name = "Test Show",
            mediumImageUrl = null,
            originalImageUrl = null,
            summary = "Summary",
            genres = null,
            time = null,
            previousEpisodeHref = null,
            nextEpisodeHref = null,
            status = null,
            airDays = null,
            averageRating = null,
            language = null,
            nextEpisodeLinkedId = null,
            previousEpisodeLinkedId = null
        )

        `when`(
            showDetailRepository.getShowSummary(123)
        ).thenReturn(kotlinx.coroutines.flow.flowOf(com.theupnextapp.domain.Result.Success(showSummary)))
        `when`(traktRepository.getRelatedShows(imdbId)).thenReturn(
            Result.success(
                listOf(
                    com.theupnextapp.domain.TraktRelatedShows(
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
                    )
                )
            )
        )

        // When
        viewModel.selectedShow(showDetailArg)

        // Then
        // Verify state update (checking eventually or after delay)
        verify(traktRepository, timeout(3000)).getRelatedShows(imdbId)
    }

    @Test
    fun `onAddRemoveFavoriteClick queues work when logged in`() = runTest {
        // Given
        val imdbId = "tt12345"
        val token = "test_token"
        
        // Mock a valid token in the repository
        `when`(traktRepository.traktAccessToken).thenReturn(
            MutableStateFlow(TraktAccessToken(
                access_token = token,
                token_type = "bearer",
                expires_in = 1234,
                refresh_token = "refresh",
                scope = "public",
                created_at = 123
            ))
        )
        // Ensure favoriteShow is NOT set (so we are Adding)
        `when`(traktRepository.getFavoriteShowFlow(imdbId)).thenReturn(kotlinx.coroutines.flow.flowOf(null))

        // Re-init viewModel to pick up new mocks if needed, or just rely on flow collection
        // Since we are testing a function call that collects, we can assume it will collect the new flow value.
        // But we need to make sure the viewmodel is initialized with the mocks.
        // The flows are collected in init block for favorite status, but the function under test `onAddRemoveFavoriteClick`
        // collects `traktRepository.traktAccessToken` ON DEMAND.
        
        // Setup UI State with valid show execution
        val showDetailArg = com.theupnextapp.domain.ShowDetailArg(
            showId = "123",
            showTitle = "Test Show",
            showImageUrl = null,
            showBackgroundUrl = null,
            imdbID = imdbId,
            isAuthorizedOnTrakt = true,
            showTraktId = 1
        )
        val showSummary = com.theupnextapp.domain.ShowDetailSummary(
            id = 123,
            imdbID = imdbId,
            name = "Test Show",
            mediumImageUrl = null,
            originalImageUrl = null,
            summary = "Summary",
            genres = null,
            time = null,
            previousEpisodeHref = null,
            nextEpisodeHref = null,
            status = null,
            airDays = null,
            averageRating = null,
            language = null,
            nextEpisodeLinkedId = null,
            previousEpisodeLinkedId = null
        )
        `when`(showDetailRepository.getShowSummary(123)).thenReturn(kotlinx.coroutines.flow.flowOf(com.theupnextapp.domain.Result.Success(showSummary)))
        
        viewModel.selectedShow(showDetailArg)
        
        // Wait for summary to load so imdbId is in state
        verify(traktRepository, timeout(3000)).getRelatedShows(anyString()) // Wait for side effect of loading
        
        // When
        viewModel.onAddRemoveFavoriteClick()
        
        // Then
        verify(workManager).enqueue(any<OneTimeWorkRequest>())
    }
}
