/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 */

package com.theupnextapp.ui.episodeDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.EpisodeDetail
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.WatchedEpisode
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.HttpException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class EpisodeDetailViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Mock
    private lateinit var showDetailRepository: ShowDetailRepository

    @Mock
    private lateinit var traktRepository: TraktRepository

    @Mock
    private lateinit var watchProgressRepository: WatchProgressRepository

    @Mock
    private lateinit var workManager: WorkManager

    private lateinit var route: Destinations.EpisodeDetail
    private lateinit var viewModel: EpisodeDetailViewModel

    @Before
    fun setUp() {
        route =
            Destinations.EpisodeDetail(
                showTraktId = 1234,
                seasonNumber = 1,
                episodeNumber = 5,
            )
        whenever(showDetailRepository.getEpisodeDetails(anyInt(), anyInt(), anyInt())).thenReturn(
            flowOf(Result.Loading(true)),
        )
        whenever(showDetailRepository.getEpisodePeople(anyInt(), anyInt(), anyInt())).thenReturn(
            emptyFlow(),
        )
        whenever(traktRepository.traktCheckInEvent).thenReturn(MutableSharedFlow())
        whenever(traktRepository.isAuthorizedOnTrakt()).thenReturn(MutableStateFlow(false))
        whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))
        whenever(watchProgressRepository.getWatchedEpisodesForShow(anyInt())).thenReturn(emptyFlow())
    }

    private fun createViewModel(customRoute: Destinations.EpisodeDetail = route): EpisodeDetailViewModel {
        return EpisodeDetailViewModel(
            route = customRoute,
            showDetailRepository = showDetailRepository,
            traktRepository = traktRepository,
            watchProgressRepository = watchProgressRepository,
            workManager = workManager,
        )
    }

    @Test
    fun `when viewmodel is initialized, initial episode metadata is seeded synchronously`() {
        viewModel = createViewModel()

        val initialState = viewModel.uiState.value
        assertEquals(true, initialState.isLoading)
        assertEquals(5, initialState.episodeDetail?.number)
        assertEquals(1, initialState.episodeDetail?.season)
        assertNull(initialState.episodeDetail?.title)
    }

    @Test
    fun `when viewmodel is initialized, it emits loading and then episode details`() =
        runTest {
            val mockEpisode =
                EpisodeDetail(
                    title = "Test Episode",
                    overview = "Test Overview",
                    season = 1,
                    number = 5,
                    firstAired = "2026-01-01T00:00:00Z",
                    runtime = 60,
                    rating = 8.5,
                    tvdbId = null,
                    imdbId = null,
                    tmdbId = null,
                    votes = 100,
                )

            whenever(showDetailRepository.getEpisodeDetails(anyInt(), anyInt(), anyInt())).thenReturn(
                flowOf(Result.Loading(true), Result.Success(mockEpisode)),
            )

            viewModel = createViewModel()
            advanceUntilIdle()

            val finalState = viewModel.uiState.value
            assertEquals(false, finalState.isLoading)
            assertEquals("Test Episode", finalState.episodeDetail?.title)
            assertEquals(8.5, finalState.episodeDetail?.rating)
            assertEquals(null, finalState.error)
        }

    @Test
    fun `when repository returns GenericError, uiState exposes error message`() =
        runTest {
            val mockException = mock<HttpException>()
            whenever(mockException.message).thenReturn("Test Error")

            whenever(showDetailRepository.getEpisodeDetails(anyInt(), anyInt(), anyInt())).thenReturn(
                flowOf(Result.Loading(true), Result.GenericError(404, null, mockException)),
            )

            viewModel = createViewModel()
            advanceUntilIdle()

            val finalState = viewModel.uiState.value
            assertEquals(false, finalState.isLoading)
            assertNull(finalState.episodeDetail?.title)
            assertEquals("Test Error", finalState.error)
        }

    @Test
    fun `onCheckIn calls checkInToShow on the repository`() =
        runTest {
            val mockEpisode =
                EpisodeDetail(
                    title = "Test Episode",
                    overview = "Test Overview",
                    season = 1,
                    number = 5,
                    firstAired = "2026-01-01T00:00:00Z",
                    runtime = 60,
                    rating = 8.5,
                    tvdbId = null,
                    imdbId = null,
                    tmdbId = null,
                    votes = 100,
                )

            whenever(showDetailRepository.getEpisodeDetails(anyInt(), anyInt(), anyInt())).thenReturn(
                flowOf(Result.Loading(true), Result.Success(mockEpisode)),
            )

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onCheckIn()
            advanceUntilIdle()

            verify(traktRepository).checkInToShow(1234, 1, 5)
        }

    @Test
    fun `when repository emits authorized state, uiState updates isAuthorizedOnTrakt`() =
        runTest {
            whenever(traktRepository.isAuthorizedOnTrakt()).thenReturn(MutableStateFlow(true))

            viewModel = createViewModel()
            advanceUntilIdle()

            val finalState = viewModel.uiState.value
            assertEquals(true, finalState.isAuthorizedOnTrakt)
        }

    @Test
    fun `when onToggleWatched is called without Trakt auth, repository is not called`() =
        runTest {
            whenever(traktRepository.isAuthorizedOnTrakt()).thenReturn(MutableStateFlow(false))

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onToggleWatched()
            advanceUntilIdle()

            verify(watchProgressRepository, never()).markEpisodeWatched(anyInt(), any(), any(), anyInt(), anyInt())
            verify(watchProgressRepository, never()).markEpisodeUnwatched(anyInt(), anyInt(), anyInt())
            verify(workManager, never()).enqueue(any<WorkRequest>())
        }

    @Test
    fun `when onToggleWatched is called and episode is unwatched, marks episode watched and triggers sync`() =
        runTest {
            whenever(traktRepository.isAuthorizedOnTrakt()).thenReturn(MutableStateFlow(true))
            val mockToken =
                TraktAccessToken(
                    access_token = "valid_token",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 3000000000L,
                )
            whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(mockToken))

            viewModel = createViewModel()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isWatched)

            viewModel.onToggleWatched()

            assertTrue(viewModel.uiState.value.isWatched)
            advanceUntilIdle()

            verify(watchProgressRepository).markEpisodeWatched(
                showTraktId = 1234,
                showTvMazeId = null,
                showImdbId = null,
                seasonNumber = 1,
                episodeNumber = 5,
            )
            verify(workManager).enqueue(any<WorkRequest>())
        }

    @Test
    fun `when onToggleWatched is called and episode is watched, marks episode unwatched and triggers sync`() =
        runTest {
            whenever(traktRepository.isAuthorizedOnTrakt()).thenReturn(MutableStateFlow(true))
            val mockToken =
                TraktAccessToken(
                    access_token = "valid_token",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 3000000000L,
                )
            whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(mockToken))
            val watchedEpisodes =
                listOf(
                    WatchedEpisode(
                        showTraktId = 1234,
                        showTvMazeId = null,
                        showImdbId = null,
                        seasonNumber = 1,
                        episodeNumber = 5,
                        watchedAt = 1000L,
                        isSynced = true,
                    ),
                )
            whenever(watchProgressRepository.getWatchedEpisodesForShow(1234)).thenReturn(flowOf(watchedEpisodes))

            viewModel = createViewModel()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isWatched)

            viewModel.onToggleWatched()

            assertFalse(viewModel.uiState.value.isWatched)
            advanceUntilIdle()

            verify(watchProgressRepository).markEpisodeUnwatched(
                showTraktId = 1234,
                seasonNumber = 1,
                episodeNumber = 5,
            )
            verify(workManager).enqueue(any<WorkRequest>())
        }

    @Test
    fun `when episodeNumber is 1, canNavigatePrevious is false and getPreviousEpisodeRoute returns null`() =
        runTest {
            val routeEpisode1 =
                Destinations.EpisodeDetail(
                    showTraktId = 1234,
                    seasonNumber = 1,
                    episodeNumber = 1,
                )

            val vm = createViewModel(routeEpisode1)

            assertFalse(vm.canNavigatePrevious)
            assertNull(vm.getPreviousEpisodeRoute())
        }

    @Test
    fun `when episodeNumber is greater than 1, traversal routes navigate to previous and next episodes`() =
        runTest {
            viewModel = createViewModel()

            // route has episodeNumber = 5, seasonNumber = 1, showTraktId = 1234
            assertTrue(viewModel.canNavigatePrevious)

            val prevRoute = viewModel.getPreviousEpisodeRoute()
            assertNotNull(prevRoute)
            assertEquals(4, prevRoute?.episodeNumber)
            assertEquals(1, prevRoute?.seasonNumber)
            assertEquals(1234, prevRoute?.showTraktId)
            assertNull(prevRoute?.episodeImageUrl)

            val nextRoute = viewModel.getNextEpisodeRoute()
            assertEquals(6, nextRoute.episodeNumber)
            assertEquals(1, nextRoute.seasonNumber)
            assertEquals(1234, nextRoute.showTraktId)
            assertNull(nextRoute.episodeImageUrl)
        }
}
