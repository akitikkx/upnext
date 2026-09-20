/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.dashboard

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.domain.ExtractedTraktInfo
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleEpisode
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleShow
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponseItemIds
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedEpisode
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowIds
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowInfo
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class DashboardScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: DashboardViewModel

    private val tokenFlow = MutableStateFlow<TraktAccessToken?>(null)
    private val upNextShowsFlow = MutableStateFlow<List<NetworkTraktPlaybackResponse>?>(null)
    private val upNextImagesFlow = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
    private val isLoadingUpNextFlow = MutableStateFlow(false)
    private val airingSoonShowsFlow = MutableStateFlow<NetworkTraktMyScheduleResponse?>(null)
    private val airingSoonImagesFlow = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
    private val isLoadingAiringSoonFlow = MutableStateFlow(false)
    private val recentHistoryFlow = MutableStateFlow<List<NetworkTraktHistoryResponse>?>(null)
    private val historyImagesFlow = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
    private val isLoadingHistoryFlow = MutableStateFlow(false)
    private val recommendedShowsFlow = MutableStateFlow<NetworkTraktRecommendationsResponse?>(null)
    private val recommendedShowsImagesFlow = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
    private val isLoadingRecommendationsFlow = MutableStateFlow(false)
    private val todayShowsFlow = MutableStateFlow<List<ScheduleShow>?>(null)
    private val mostAnticipatedShowsFlow = MutableStateFlow<List<TraktMostAnticipated>?>(null)
    private val isLoadingTodayShowsFlow = MutableStateFlow(false)
    private val isLoadingMostAnticipatedFlow = MutableStateFlow(false)
    private val regionalTrendingShowsFlow = MutableStateFlow<List<TraktTrendingShows>?>(null)
    private val regionalTrendingShowsImagesFlow = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
    private val isLoadingRegionalTrendingFlow = MutableStateFlow(false)
    private val isLoadingFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        viewModel = mock(DashboardViewModel::class.java)

        `when`(viewModel.traktAccessToken).thenReturn(tokenFlow)
        `when`(viewModel.upNextShows).thenReturn(upNextShowsFlow)
        `when`(viewModel.upNextImages).thenReturn(upNextImagesFlow)
        `when`(viewModel.isLoadingUpNext).thenReturn(isLoadingUpNextFlow)
        `when`(viewModel.airingSoonShows).thenReturn(airingSoonShowsFlow)
        `when`(viewModel.airingSoonImages).thenReturn(airingSoonImagesFlow)
        `when`(viewModel.isLoadingAiringSoon).thenReturn(isLoadingAiringSoonFlow)
        `when`(viewModel.recentHistory).thenReturn(recentHistoryFlow)
        `when`(viewModel.historyImages).thenReturn(historyImagesFlow)
        `when`(viewModel.isLoadingHistory).thenReturn(isLoadingHistoryFlow)
        `when`(viewModel.recommendedShows).thenReturn(recommendedShowsFlow)
        `when`(viewModel.recommendedShowsImages).thenReturn(recommendedShowsImagesFlow)
        `when`(viewModel.isLoadingRecommendations).thenReturn(isLoadingRecommendationsFlow)
        `when`(viewModel.todayShows).thenReturn(todayShowsFlow)
        `when`(viewModel.mostAnticipatedShows).thenReturn(mostAnticipatedShowsFlow)
        `when`(viewModel.isLoadingTodayShows).thenReturn(isLoadingTodayShowsFlow)
        `when`(viewModel.isLoadingMostAnticipated).thenReturn(isLoadingMostAnticipatedFlow)
        `when`(viewModel.regionalTrendingShows).thenReturn(regionalTrendingShowsFlow)
        `when`(viewModel.regionalTrendingShowsImages).thenReturn(regionalTrendingShowsImagesFlow)
        `when`(viewModel.isLoadingRegionalTrending).thenReturn(isLoadingRegionalTrendingFlow)
        `when`(viewModel.isLoading).thenReturn(isLoadingFlow)
    }

    @Test
    fun dashboardScreen_whenAuthenticated_displaysUpNextSection() {
        tokenFlow.value =
            TraktAccessToken(
                access_token = "valid_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "refresh",
                scope = "public",
                token_type = "bearer",
            )
        val playback =
            listOf(
                NetworkTraktPlaybackResponse(
                    progress = 50f,
                    action = "pause",
                    type = "episode",
                    show =
                        NetworkTraktWatchedShowInfo(
                            title = "Severance",
                            year = 2022,
                            ids =
                                NetworkTraktWatchedShowIds(
                                    trakt = 100,
                                    tvdb = null,
                                    imdb = "tt123",
                                    tmdb = null,
                                    slug = "severance",
                                ),
                        ),
                    episode =
                        NetworkTraktWatchedEpisode(
                            season = 2,
                            number = 1,
                            title = "Hello Ms. Cobel",
                            plays = 1,
                            lastWatchedAt = "2026-09-20T00:00:00.000Z",
                        ),
                ),
            )
        upNextShowsFlow.value = playback

        composeTestRule.setContent {
            DashboardScreen(
                onNavigate = {},
                viewModel = viewModel,
            )
        }

        composeTestRule.onNodeWithText("Up Next to Watch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("up_next_pager").assertIsDisplayed()
        composeTestRule.onNodeWithText("Severance").assertIsDisplayed()
        composeTestRule.onNodeWithText("50% completed").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_whenAuthenticated_emptyUpNext_displaysEmptyState() {
        tokenFlow.value =
            TraktAccessToken(
                access_token = "valid_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "refresh",
                scope = "public",
                token_type = "bearer",
            )
        upNextShowsFlow.value = emptyList()

        composeTestRule.setContent {
            DashboardScreen(
                onNavigate = {},
                viewModel = viewModel,
            )
        }

        composeTestRule.onNodeWithText("Up Next to Watch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("up_next_empty_state").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_clickingSeeAllRecentActivity_navigatesToWatchHistory() {
        tokenFlow.value =
            TraktAccessToken(
                access_token = "valid_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "refresh",
                scope = "public",
                token_type = "bearer",
            )
        var navigatedDestination: Destinations? = null

        composeTestRule.setContent {
            DashboardScreen(
                onNavigate = { navigatedDestination = it },
                viewModel = viewModel,
            )
        }

        composeTestRule.onNodeWithTag("dashboard_list").performScrollToNode(hasTestTag("recent_activity_see_all_button"))
        composeTestRule.onNodeWithTag("recent_activity_see_all_button").performClick()
        assertEquals(Destinations.WatchHistory, navigatedDestination)
    }

    @Test
    fun dashboardScreen_clickMarkWatchedOnUpNext_invokesViewModel() {
        tokenFlow.value =
            TraktAccessToken(
                access_token = "valid_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "refresh",
                scope = "public",
                token_type = "bearer",
            )
        val playback =
            listOf(
                NetworkTraktPlaybackResponse(
                    progress = 30f,
                    action = "pause",
                    type = "episode",
                    show =
                        NetworkTraktWatchedShowInfo(
                            title = "Silo",
                            year = 2023,
                            ids =
                                NetworkTraktWatchedShowIds(
                                    trakt = 200,
                                    tvdb = null,
                                    imdb = "tt456",
                                    tmdb = null,
                                    slug = "silo",
                                ),
                        ),
                    episode =
                        NetworkTraktWatchedEpisode(
                            season = 1,
                            number = 2,
                            title = "Holston's Pick",
                            plays = 0,
                            lastWatchedAt = null,
                        ),
                ),
            )
        upNextShowsFlow.value = playback

        composeTestRule.setContent {
            DashboardScreen(
                onNavigate = {},
                viewModel = viewModel,
            )
        }

        composeTestRule.onNodeWithTag("mark_watched_card_button").performClick()
        verify(viewModel).onMarkEpisodeWatched(
            showTvMazeId = null,
            imdbId = "tt456",
            showTraktId = 200,
            season = 1,
            number = 2,
        )
    }

    @Test
    fun dashboardScreen_whenAiringSoonPopulated_rendersAiringSoonCards() {
        tokenFlow.value =
            TraktAccessToken(
                access_token = "valid_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "refresh",
                scope = "public",
                token_type = "bearer",
            )
        val scheduleItem =
            NetworkTraktMyScheduleResponseItem(
                first_aired = "2026-09-21T00:00:00.000Z",
                episode =
                    NetworkTraktMyScheduleEpisode(
                        season = 1,
                        number = 5,
                        title = "The Next Episode",
                        ids = null,
                    ),
                show =
                    NetworkTraktMyScheduleShow(
                        title = "Slow Horses",
                        year = 2022,
                        ids =
                            NetworkTraktMyScheduleShowIds(
                                trakt = 300,
                                slug = "slow-horses",
                                tvdb = null,
                                imdb = "tt789",
                                tmdb = null,
                            ),
                    ),
            )
        airingSoonShowsFlow.value =
            NetworkTraktMyScheduleResponse().apply {
                add(scheduleItem)
            }

        composeTestRule.setContent {
            DashboardScreen(
                onNavigate = {},
                viewModel = viewModel,
            )
        }

        composeTestRule.onNodeWithTag("dashboard_list").performScrollToNode(hasTestTag("airing_soon_card_0"))
        composeTestRule.onNodeWithTag("airing_soon_card_0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Slow Horses").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_whenRecommendedPopulated_rendersRecommendedCards() {
        tokenFlow.value =
            TraktAccessToken(
                access_token = "valid_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "refresh",
                scope = "public",
                token_type = "bearer",
            )
        val recommendationItem =
            NetworkTraktRecommendationsResponseItem(
                title = "Severance",
                year = 2022,
                ids =
                    NetworkTraktRecommendationsResponseItemIds(
                        trakt = 100,
                        slug = "severance",
                        tvdb = null,
                        imdb = "tt123",
                        tmdb = null,
                        tvmaze = null,
                    ),
            )
        recommendedShowsFlow.value =
            NetworkTraktRecommendationsResponse().apply {
                add(recommendationItem)
            }

        composeTestRule.setContent {
            DashboardScreen(
                onNavigate = {},
                viewModel = viewModel,
            )
        }

        composeTestRule.onNodeWithTag("dashboard_list").performScrollToNode(hasTestTag("recommended_card_0"))
        composeTestRule.onNodeWithTag("recommended_card_0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Severance").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_whenRecommendedEmpty_rendersEmptyState() {
        tokenFlow.value =
            TraktAccessToken(
                access_token = "valid_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "refresh",
                scope = "public",
                token_type = "bearer",
            )
        recommendedShowsFlow.value = NetworkTraktRecommendationsResponse()

        composeTestRule.setContent {
            DashboardScreen(
                onNavigate = {},
                viewModel = viewModel,
            )
        }

        composeTestRule.onNodeWithTag("dashboard_list").performScrollToNode(hasTestTag("recommended_empty_state"))
        composeTestRule.onNodeWithTag("recommended_empty_state").assertIsDisplayed()
    }
}
