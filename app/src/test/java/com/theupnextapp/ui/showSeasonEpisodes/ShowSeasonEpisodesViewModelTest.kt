package com.theupnextapp.ui.showSeasonEpisodes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ShowSeasonEpisodesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val traktRepository: TraktRepository = mock()

    private lateinit var viewModel: ShowSeasonEpisodesViewModel

    private val showDetailRepository: ShowDetailRepository = mock()
    private val watchProgressRepository: WatchProgressRepository = mock()
    private val workManager: WorkManager = mock()
    private val traktAuthManager: TraktAuthManager = mock()

    @Before
    fun setup() {
        // Default to not authorized/valid unless specified in test
        whenever(traktRepository.traktAccessToken).thenReturn(flowOf(null))
        whenever(traktRepository.favoriteShow).thenReturn(MutableStateFlow(null))
        whenever(traktAuthManager.traktAuthState).thenReturn(MutableStateFlow(TraktAuthState.LoggedIn))
    }

    @Test
    fun `onToggleWatched calls markEpisodeWatched when authorized and episode is not watched`() = runTest {
        // Given
        val accessToken = com.theupnextapp.domain.TraktAccessToken(
            access_token = "token",
            token_type = "bearer",
            expires_in = 3600,
            refresh_token = "refresh",
            scope = "public",
            created_at = 3000000000L // Year 2065
        )
        whenever(traktRepository.traktAccessToken).thenReturn(flowOf(accessToken))

        createViewModel()

        val showTraktId = 123
        val seasonNum = 1
        val episodeNum = 1

        // Mock data loading
        whenever(
            showDetailRepository.getShowSeasonEpisodes(1, seasonNum)
        ).thenReturn(flowOf(com.theupnextapp.domain.Result.Success(emptyList())))
        whenever(watchProgressRepository.getWatchedEpisodesForShow(showTraktId)).thenReturn(flowOf(emptyList()))

        // Initialize VM state
        val args = com.theupnextapp.domain.ShowSeasonEpisodesArg(
            showId = 1,
            showTraktId = showTraktId,
            seasonNumber = seasonNum,
            imdbID = "tt123",
            isAuthorizedOnTrakt = true
        )
        viewModel.selectedSeason(args)

        // Important: Collect the flow so stateIn works
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isAuthorizedOnTrakt.collect {}
        }

        val episode = ShowSeasonEpisode(
            id = 1,
            number = episodeNum,
            season = seasonNum,
            name = "Pilot",
            isWatched = false, // Not watched
            originalImageUrl = null,
            mediumImageUrl = null,
            summary = null,
            airstamp = null,
            runtime = null,
            type = null,
            airdate = null,
            airtime = null,
            imdbID = null
        )

        // When
        viewModel.onToggleWatched(episode)

        // Then
        verify(watchProgressRepository).markEpisodeWatched(
            showTraktId = showTraktId,
            showTvMazeId = 1,
            showImdbId = "tt123",
            seasonNumber = seasonNum,
            episodeNumber = episodeNum
        )
    }

    @Test
    fun `onToggleWatched calls markEpisodeUnwatched when authorized and episode is watched`() = runTest {
        // Given
        val accessToken = com.theupnextapp.domain.TraktAccessToken(
            access_token = "token",
            token_type = "bearer",
            expires_in = 3600,
            refresh_token = "refresh",
            scope = "public",
            created_at = 3000000000L // Year 2065
        )
        whenever(traktRepository.traktAccessToken).thenReturn(flowOf(accessToken))

        viewModel = ShowSeasonEpisodesViewModel(
            showDetailRepository,
            watchProgressRepository,
            workManager,
            traktRepository,
            traktAuthManager
        )

        val showTraktId = 123
        val seasonNum = 1
        val episodeNum = 1

        // Mock data loading
        whenever(
            showDetailRepository.getShowSeasonEpisodes(1, seasonNum)
        ).thenReturn(flowOf(com.theupnextapp.domain.Result.Success(emptyList())))
        whenever(watchProgressRepository.getWatchedEpisodesForShow(showTraktId)).thenReturn(flowOf(emptyList()))

        // Initialize VM state
        val args = com.theupnextapp.domain.ShowSeasonEpisodesArg(
            showId = 1,
            showTraktId = showTraktId,
            seasonNumber = seasonNum,
            imdbID = "tt123",
            isAuthorizedOnTrakt = true
        )
        viewModel.selectedSeason(args)

        // Important: Collect the flow so stateIn works
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isAuthorizedOnTrakt.collect {}
        }

        val episode = ShowSeasonEpisode(
            id = 1,
            number = episodeNum,
            season = seasonNum,
            name = "Pilot",
            isWatched = true, // Watched
            originalImageUrl = null,
            mediumImageUrl = null,
            summary = null,
            airstamp = null,
            runtime = null,
            type = null,
            airdate = null,
            airtime = null,
            imdbID = null
        )

        // When
        viewModel.onToggleWatched(episode)

        // Then
        verify(watchProgressRepository).markEpisodeUnwatched(
            showTraktId = showTraktId,
            seasonNumber = seasonNum,
            episodeNumber = episodeNum
        )
    }

    @Test
    fun `onToggleWatched does NOT call repository if token is invalid`() = runTest {
        // Given
        whenever(traktRepository.traktAccessToken).thenReturn(flowOf(null))
        createViewModel()

        val episode = ShowSeasonEpisode(
            id = 1,
            number = 1,
            season = 1,
            name = "Pilot",
            isWatched = false,
            originalImageUrl = null,
            mediumImageUrl = null,
            summary = null,
            airstamp = null,
            runtime = null,
            type = null,
            airdate = null,
            airtime = null,
            imdbID = null
        )

        // When
        viewModel.onToggleWatched(episode)

        // Then
        verifyNoInteractions(watchProgressRepository)
    }

    private fun createViewModel() {
        viewModel = ShowSeasonEpisodesViewModel(
            showDetailRepository,
            watchProgressRepository,
            workManager,
            traktRepository,
            traktAuthManager
        )
    }
}
