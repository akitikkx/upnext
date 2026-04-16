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
import androidx.lifecycle.SavedStateHandle
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.EpisodeDetail
import com.theupnextapp.domain.Result
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
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

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: EpisodeDetailViewModel

    @Mock
    private lateinit var traktRepository: TraktRepository

    @Before
    fun setUp() {
        savedStateHandle =
            SavedStateHandle(
                mapOf(
                    "showTraktId" to 1234,
                    "seasonNumber" to 1,
                    "episodeNumber" to 5,
                ),
            )
        `when`(showDetailRepository.getEpisodeDetails(anyInt(), anyInt(), anyInt())).thenReturn(
            flowOf(Result.Loading(true)),
        )
        `when`(showDetailRepository.getEpisodePeople(anyInt(), anyInt(), anyInt())).thenReturn(
            kotlinx.coroutines.flow.emptyFlow(),
        )
        `when`(traktRepository.traktCheckInEvent).thenReturn(MutableSharedFlow())
        `when`(traktRepository.isAuthorizedOnTrakt()).thenReturn(MutableStateFlow(false))
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

            `when`(showDetailRepository.getEpisodeDetails(anyInt(), anyInt(), anyInt())).thenReturn(
                flowOf(Result.Loading(true), Result.Success(mockEpisode)),
            )

            viewModel = EpisodeDetailViewModel(savedStateHandle, showDetailRepository, traktRepository)
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
            val mockException = Mockito.mock(HttpException::class.java)
            `when`(mockException.message).thenReturn("Test Error")

            `when`(showDetailRepository.getEpisodeDetails(anyInt(), anyInt(), anyInt())).thenReturn(
                flowOf(Result.Loading(true), Result.GenericError(404, null, mockException)),
            )

            viewModel = EpisodeDetailViewModel(savedStateHandle, showDetailRepository, traktRepository)
            advanceUntilIdle()

            val finalState = viewModel.uiState.value
            assertEquals(false, finalState.isLoading)
            assertEquals(null, finalState.episodeDetail)
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

            `when`(showDetailRepository.getEpisodeDetails(anyInt(), anyInt(), anyInt())).thenReturn(
                flowOf(Result.Loading(true), Result.Success(mockEpisode)),
            )

            viewModel = EpisodeDetailViewModel(savedStateHandle, showDetailRepository, traktRepository)

            // Bypass SavedStateHandle's bundle mapping issues with kotlinx.serialization Navigation 2.8 in tests
            val routeField = EpisodeDetailViewModel::class.java.getDeclaredField("route")
            routeField.isAccessible = true
            routeField.set(viewModel, Destinations.EpisodeDetail(1234, 1, 5))

            advanceUntilIdle()

            viewModel.onCheckIn()
            advanceUntilIdle()

            Mockito.verify(traktRepository).checkInToShow(1234, 1, 5)
        }

    @Test
    fun `when repository emits authorized state, uiState updates isAuthorizedOnTrakt`() =
        runTest {
            `when`(showDetailRepository.getEpisodeDetails(anyInt(), anyInt(), anyInt())).thenReturn(
                flowOf(Result.Loading(true)),
            )
            `when`(traktRepository.isAuthorizedOnTrakt()).thenReturn(MutableStateFlow(true))

            viewModel = EpisodeDetailViewModel(savedStateHandle, showDetailRepository, traktRepository)
            advanceUntilIdle()

            val finalState = viewModel.uiState.value
            assertEquals(true, finalState.isAuthorizedOnTrakt)
        }
}
