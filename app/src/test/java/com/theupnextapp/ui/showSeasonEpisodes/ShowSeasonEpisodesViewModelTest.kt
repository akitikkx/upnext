package com.theupnextapp.ui.showSeasonEpisodes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.domain.WatchedEpisode
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
import org.mockito.kotlin.any
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
    private val providerManager: com.theupnextapp.repository.ProviderManager = mock()
    private val simklRepository: com.theupnextapp.repository.SimklRepository = mock()
    private val simklAuthManager: com.theupnextapp.repository.SimklAuthManager = mock()

    @Before
    fun setup() {
        // Default to not authorized/valid unless specified in test
        whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))
        whenever(traktRepository.watchlistShow).thenReturn(MutableStateFlow(null))
        whenever(traktAuthManager.traktAuthState).thenReturn(MutableStateFlow(TraktAuthState.LoggedIn))
        whenever(providerManager.activeProvider).thenReturn(MutableStateFlow(com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT))
    }

    @Test
    fun `onToggleWatched calls markEpisodeWatched when authorized and episode is not watched`() =
        runTest {
            // Given
            val accessToken =
                TraktAccessToken(
                    access_token = "token",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 3000000000L, // Year 2065
                )
            whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(accessToken))

            createViewModel()

            val showTraktId = 123
            val seasonNum = 1
            val episodeNum = 1

            // Mock data loading
            whenever(
                showDetailRepository.getShowSeasonEpisodes(1, seasonNum),
            ).thenReturn(flowOf(Result.Success(emptyList())))
            whenever(watchProgressRepository.getWatchedEpisodesForShow(showTraktId)).thenReturn(flowOf(emptyList()))

            // Initialize VM state
            val args =
                ShowSeasonEpisodesArg(
                    showId = 1,
                    showTraktId = showTraktId,
                    seasonNumber = seasonNum,
                    imdbID = "tt123",
                    isAuthorizedOnTrakt = true,
                )
            viewModel.selectedSeason(args)

            // Important: Collect the flow so stateIn works
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.isAuthorizedOnTrakt.collect {}
            }

            val episode =
                ShowSeasonEpisode(
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
                    imdbID = null,
                )

            // When
            viewModel.onToggleWatched(episode)

            // Then
            verify(watchProgressRepository).markEpisodeWatched(
                showTraktId = showTraktId,
                showTvMazeId = 1,
                showImdbId = "tt123",
                seasonNumber = seasonNum,
                episodeNumber = episodeNum,
            )

            verify(workManager).enqueue(any<WorkRequest>())
        }

    @Test
    fun `onToggleWatched calls markEpisodeWatched for SIMKL when authorized and episode is not watched`() =
        runTest {
            // Given
            whenever(providerManager.activeProvider).thenReturn(MutableStateFlow(com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL))
            val simklToken = com.theupnextapp.domain.SimklAccessToken(
                accessToken = "simkl_token",
                tokenType = "bearer",
                scope = "public"
            )
            whenever(simklAuthManager.simklAccessToken).thenReturn(MutableStateFlow(simklToken))

            createViewModel()

            val showTraktId = 123
            val seasonNum = 1
            val episodeNum = 1

            // Mock data loading
            whenever(
                showDetailRepository.getShowSeasonEpisodes(1, seasonNum),
            ).thenReturn(flowOf(Result.Success(emptyList())))
            whenever(simklRepository.getWatchedEpisodesForShowByImdbId("tt123")).thenReturn(flowOf(emptyList()))

            // Initialize VM state
            val args =
                ShowSeasonEpisodesArg(
                    showId = 1,
                    showTraktId = showTraktId,
                    seasonNumber = seasonNum,
                    imdbID = "tt123",
                    isAuthorizedOnTrakt = true,
                )
            viewModel.selectedSeason(args)

            val episode =
                ShowSeasonEpisode(
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
                    imdbID = null,
                )

            // When
            viewModel.onToggleWatched(episode)

            // Then
            verify(simklRepository).markEpisodeWatched(
                simklId = showTraktId,
                imdbID = "tt123",
                seasonNumber = seasonNum,
                episodeNumber = episodeNum,
                token = "simkl_token"
            )

            verify(workManager).enqueue(any<WorkRequest>())
        }

    @Test
    fun `onToggleWatched calls markEpisodeUnwatched when authorized and episode is watched`() =
        runTest {
            // Given
            val accessToken =
                TraktAccessToken(
                    access_token = "token",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 3000000000L, // Year 2065
                )
            whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(accessToken))

            createViewModel()

            val showTraktId = 123
            val seasonNum = 1
            val episodeNum = 1

            // Mock data loading
            whenever(
                showDetailRepository.getShowSeasonEpisodes(1, seasonNum),
            ).thenReturn(flowOf(Result.Success(emptyList())))
            whenever(watchProgressRepository.getWatchedEpisodesForShow(showTraktId)).thenReturn(flowOf(emptyList()))

            // Initialize VM state
            val args =
                ShowSeasonEpisodesArg(
                    showId = 1,
                    showTraktId = showTraktId,
                    seasonNumber = seasonNum,
                    imdbID = "tt123",
                    isAuthorizedOnTrakt = true,
                )
            viewModel.selectedSeason(args)

            // Important: Collect the flow so stateIn works
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.isAuthorizedOnTrakt.collect {}
            }

            val episode =
                ShowSeasonEpisode(
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
                    imdbID = null,
                )

            // When
            viewModel.onToggleWatched(episode)

            // Then
            verify(watchProgressRepository).markEpisodeUnwatched(
                showTraktId = showTraktId,
                seasonNumber = seasonNum,
                episodeNumber = episodeNum,
            )

            verify(workManager).enqueue(any<WorkRequest>())
        }

    @Test
    fun `onToggleWatched does NOT call repository if token is invalid`() =
        runTest {
            // Given
            whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))
            createViewModel()

            val episode =
                ShowSeasonEpisode(
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
                    imdbID = null,
                )

            // When
            viewModel.onToggleWatched(episode)

            // Then
            verifyNoInteractions(watchProgressRepository)
        }

    @Test
    fun `markSeasonAsWatched does NOT call repository if unauthorized`() =
        runTest {
            // Given
            whenever(traktAuthManager.traktAuthState).thenReturn(MutableStateFlow(TraktAuthState.LoggedOut))
            createViewModel()

            // When
            viewModel.markSeasonAsWatched()

            // Then
            verifyNoInteractions(watchProgressRepository)
        }

    @Test
    fun `markSeasonAsUnwatched does NOT call repository if unauthorized`() =
        runTest {
            // Given
            whenever(traktAuthManager.traktAuthState).thenReturn(MutableStateFlow(TraktAuthState.LoggedOut))
            createViewModel()

            // When
            viewModel.markSeasonAsUnwatched()

            // Then
            verifyNoInteractions(watchProgressRepository)
        }

    private fun createViewModel() {
        viewModel =
            ShowSeasonEpisodesViewModel(
                showDetailRepository,
                watchProgressRepository,
                simklRepository,
                providerManager,
                simklAuthManager,
                workManager,
                traktRepository,
                traktAuthManager,
            )
    }

    @Test
    fun `selectedSeason calls refreshWatchedFromTrakt when authenticated`() =
        runTest {
            // Given
            val accessToken =
                TraktAccessToken(
                    access_token = "token",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 3000000000L,
                )
            whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(accessToken))

            val showTraktId = 456
            val seasonNum = 2

            whenever(
                showDetailRepository.getShowSeasonEpisodes(10, seasonNum),
            ).thenReturn(flowOf(Result.Success(emptyList())))
            whenever(watchProgressRepository.getWatchedEpisodesForShow(showTraktId)).thenReturn(flowOf(emptyList()))
            whenever(watchProgressRepository.refreshWatchedFromTrakt("token", showTraktId)).thenReturn(kotlin.Result.success(Unit))

            createViewModel()

            // When
            val args =
                ShowSeasonEpisodesArg(
                    showId = 10,
                    showTraktId = showTraktId,
                    seasonNumber = seasonNum,
                )
            viewModel.selectedSeason(args)

            // Allow coroutines to execute
            testScheduler.advanceUntilIdle()

            // Then
            verify(watchProgressRepository).refreshWatchedFromTrakt("token", showTraktId)
        }

    @Test
    fun `selectedSeason does NOT call refreshWatchedFromTrakt when no token`() =
        runTest {
            // Given
            whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))

            val seasonNum = 1

            whenever(
                showDetailRepository.getShowSeasonEpisodes(1, seasonNum),
            ).thenReturn(flowOf(Result.Success(emptyList())))
            whenever(watchProgressRepository.getWatchedEpisodesForShow(any())).thenReturn(flowOf(emptyList()))

            createViewModel()

            // When
            val args =
                ShowSeasonEpisodesArg(
                    showId = 1,
                    showTraktId = 789,
                    seasonNumber = seasonNum,
                )
            viewModel.selectedSeason(args)

            // Allow coroutines to execute
            testScheduler.advanceUntilIdle()

            // Then
            verify(watchProgressRepository, org.mockito.kotlin.never()).refreshWatchedFromTrakt(any(), any())
        }

    @Test
    fun `selectedSeason does NOT call refreshWatchedFromTrakt when SIMKL is active`() =
        runTest {
            // Given
            val accessToken =
                TraktAccessToken(
                    access_token = "token",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 3000000000L,
                )
            whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(accessToken))
            whenever(providerManager.activeProvider).thenReturn(MutableStateFlow(com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL))

            val showTraktId = 456
            val seasonNum = 2

            whenever(
                showDetailRepository.getShowSeasonEpisodes(10, seasonNum),
            ).thenReturn(flowOf(Result.Success(emptyList())))
            whenever(watchProgressRepository.getWatchedEpisodesForShow(showTraktId)).thenReturn(flowOf(emptyList()))

            createViewModel()

            // When
            val args =
                ShowSeasonEpisodesArg(
                    showId = 10,
                    showTraktId = showTraktId,
                    seasonNumber = seasonNum,
                )
            viewModel.selectedSeason(args)

            // Allow coroutines to execute
            testScheduler.advanceUntilIdle()

            // Then
            verify(watchProgressRepository, org.mockito.kotlin.never()).refreshWatchedFromTrakt(any(), any())
        }

    @Test
    fun `episodes emit with isWatched false when SIMKL is active ignoring Trakt cache`() =
        runTest {
            // Given
            whenever(providerManager.activeProvider).thenReturn(MutableStateFlow(com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL))

            val showTraktId = 456
            val seasonNum = 2
            val episodeNum = 1

            val episode =
                ShowSeasonEpisode(
                    id = 1,
                    number = episodeNum,
                    season = seasonNum,
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
                    imdbID = null,
                )

            val watchedEpisode =
                WatchedEpisode(
                    showTraktId = showTraktId,
                    showTvMazeId = null,
                    showImdbId = null,
                    seasonNumber = seasonNum,
                    episodeNumber = episodeNum,
                    watchedAt = 123456789L,
                    isSynced = true,
                )

            whenever(
                showDetailRepository.getShowSeasonEpisodes(10, seasonNum),
            ).thenReturn(flowOf(Result.Success(listOf(episode))))
            // Trakt DB has this episode marked as watched
            whenever(watchProgressRepository.getWatchedEpisodesForShow(showTraktId)).thenReturn(flowOf(listOf(watchedEpisode)))

            createViewModel()

            // When
            val args =
                ShowSeasonEpisodesArg(
                    showId = 10,
                    showTraktId = showTraktId,
                    seasonNumber = seasonNum,
                )
            viewModel.selectedSeason(args)

            val emittedEpisodes = mutableListOf<List<ShowSeasonEpisode>>()
            val observer = androidx.lifecycle.Observer<List<ShowSeasonEpisode>?> { it?.let { emittedEpisodes.add(it) } }
            viewModel.episodes.observeForever(observer)

            testScheduler.advanceUntilIdle()

            // Then
            val actualEpisodes = emittedEpisodes.lastOrNull()
            assert(actualEpisodes != null)
            assert(actualEpisodes!!.size == 1)
            // Should be false because active provider is SIMKL, ignoring the watchedEpisode in Trakt cache
            assert(!actualEpisodes[0].isWatched)

            viewModel.episodes.removeObserver(observer)
        }
}
