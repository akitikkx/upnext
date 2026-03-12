package com.theupnextapp.ui.showSeasons

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkManager
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ShowSeasonsViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val showDetailRepository: ShowDetailRepository = mock()
    private val watchProgressRepository: WatchProgressRepository = mock()
    private val workManager: WorkManager = mock()
    private val traktRepository: TraktRepository = mock()
    private val traktAuthManager: TraktAuthManager = mock()
    private val savedStateHandle: SavedStateHandle = mock()

    private lateinit var viewModel: ShowSeasonsViewModel

    @Before
    fun setup() {
        whenever(traktRepository.traktAccessToken).thenReturn(flowOf(null))
        whenever(traktAuthManager.traktAuthState).thenReturn(MutableStateFlow(TraktAuthState.LoggedIn))
        whenever(savedStateHandle.get<String>(ShowSeasonsViewModel.SHOW_ID)).thenReturn("1")
    }

    private fun createViewModel() {
        viewModel =
            ShowSeasonsViewModel(
                savedStateHandle = savedStateHandle,
                showDetailRepository = showDetailRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = workManager,
                traktRepository = traktRepository,
                traktAuthManager = traktAuthManager,
            )
    }

    @Test
    fun `onToggleSeasonWatched calls markSeasonUnwatched when season is currently watched`() =
        runTest {
            val accessToken =
                TraktAccessToken(
                    access_token = "token",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 3000000000L,
                )
            whenever(traktRepository.traktAccessToken).thenReturn(flowOf(accessToken))

            createViewModel()

            val arg =
                ShowDetailArg(
                    showId = "1",
                    showTitle = "Title",
                    showImageUrl = null,
                    showBackgroundUrl = null,
                    imdbID = "tt123",
                    isAuthorizedOnTrakt = true,
                    showTraktId = 123,
                )
            whenever(showDetailRepository.getShowSeasons(1)).thenReturn(flowOf(Result.Success(emptyList())))
            viewModel.setSelectedShow(arg)

            val season =
                ShowSeason(
                    id = 1,
                    seasonNumber = 1,
                    episodeCount = 10,
                    name = "Season 1",
                    premiereDate = null,
                    endDate = null,
                    originalImageUrl = null,
                    mediumImageUrl = null,
                    isWatched = true,
                )

            viewModel.onToggleSeasonWatched(season)

            verify(watchProgressRepository).markSeasonUnwatched(
                showTraktId = 123,
                seasonNumber = 1,
            )
        }

    @Test
    fun `onToggleSeasonWatched calls markSeasonWatched when season is currently not watched`() =
        runTest {
            val accessToken =
                TraktAccessToken(
                    access_token = "token",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 3000000000L,
                )
            whenever(traktRepository.traktAccessToken).thenReturn(flowOf(accessToken))

            createViewModel()

            val arg =
                ShowDetailArg(
                    showId = "1",
                    showTitle = "Title",
                    showImageUrl = null,
                    showBackgroundUrl = null,
                    imdbID = "tt123",
                    isAuthorizedOnTrakt = true,
                    showTraktId = 123,
                )
            whenever(showDetailRepository.getShowSeasons(1)).thenReturn(flowOf(Result.Success(emptyList())))
            viewModel.setSelectedShow(arg)

            val episodes =
                listOf(
                    ShowSeasonEpisode(
                        id = 1, number = 1, season = 1, name = "Pilot",
                        isWatched = false, originalImageUrl = null, mediumImageUrl = null,
                        summary = null, airstamp = null, runtime = null, type = null,
                        airdate = null, airtime = null, imdbID = "tt123",
                    ),
                )
            whenever(showDetailRepository.getShowSeasonEpisodes(1, 1))
                .thenReturn(flowOf(Result.Success(episodes)))

            val season =
                ShowSeason(
                    id = 1,
                    seasonNumber = 1,
                    episodeCount = 10,
                    name = "Season 1",
                    premiereDate = null,
                    endDate = null,
                    originalImageUrl = null,
                    mediumImageUrl = null,
                    isWatched = false,
                )

            viewModel.onToggleSeasonWatched(season)

            verify(watchProgressRepository).markSeasonWatched(
                showTraktId = 123,
                showTvMazeId = 1,
                showImdbId = "tt123",
                seasonNumber = 1,
                episodes = episodes,
            )
        }
}
