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

package com.theupnextapp.ui.watchHistory

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class WatchHistoryScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleItem1 =
        WatchHistoryUiItem(
            historyId = 101L,
            watchedAt = "2026-09-20T20:00:00.000Z",
            formattedWatchedAt = "Sep 20, 2026 • 10:00 PM",
            monthYearHeader = "September 2026",
            showTraktId = 100,
            showTvmazeId = 1234,
            showImdbId = "tt12345",
            showTitle = "Severance",
            seasonNumber = 2,
            episodeNumber = 1,
            episodeTitle = "Hello Ms. Cobel",
            imageUrl = "https://image.tmdb/severance.jpg",
            isWatched = true,
        )

    private val sampleItem2 =
        WatchHistoryUiItem(
            historyId = 102L,
            watchedAt = "2026-08-15T18:30:00.000Z",
            formattedWatchedAt = "August 15, 2026 • 8:30 PM",
            monthYearHeader = "August 2026",
            showTraktId = 200,
            showTvmazeId = 5678,
            showImdbId = "tt54321",
            showTitle = "Silo",
            seasonNumber = 1,
            episodeNumber = 1,
            episodeTitle = "Freedom Day",
            imageUrl = "https://image.tmdb/silo.jpg",
            isWatched = true,
        )

    @Test
    fun watchHistory_unauthorizedUser_displaysUnauthorizedState() {
        composeTestRule.setContent {
            WatchHistoryContent(
                uiState = WatchHistoryUiState(isAuthorized = false),
                onSearchQueryChange = {},
                onItemClick = {},
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("unauthorized_state").assertIsDisplayed()
    }

    @Test
    fun watchHistory_loading_displaysLoadingIndicator() {
        composeTestRule.setContent {
            WatchHistoryContent(
                uiState = WatchHistoryUiState(isAuthorized = true, isLoading = true, items = emptyList()),
                onSearchQueryChange = {},
                onItemClick = {},
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("history_loading_indicator").assertIsDisplayed()
    }

    @Test
    fun watchHistory_emptyHistory_displaysEmptyState() {
        composeTestRule.setContent {
            WatchHistoryContent(
                uiState = WatchHistoryUiState(isAuthorized = true, isLoading = false, items = emptyList()),
                onSearchQueryChange = {},
                onItemClick = {},
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("empty_history_state").assertIsDisplayed()
    }

    @Test
    fun watchHistory_noSearchResults_displaysNoSearchResults() {
        composeTestRule.setContent {
            WatchHistoryContent(
                uiState =
                    WatchHistoryUiState(
                        isAuthorized = true,
                        isLoading = false,
                        items = emptyList(),
                        searchQuery = "UnknownShow",
                    ),
                onSearchQueryChange = {},
                onItemClick = {},
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("no_search_results_text").assertIsDisplayed()
    }

    @Test
    fun watchHistory_withItems_displaysMonthHeadersAndItemCards() {
        val items = listOf(sampleItem1, sampleItem2)
        val grouped = items.groupBy { it.monthYearHeader }

        composeTestRule.setContent {
            WatchHistoryContent(
                uiState =
                    WatchHistoryUiState(
                        isAuthorized = true,
                        isLoading = false,
                        items = items,
                        groupedItems = grouped,
                    ),
                onSearchQueryChange = {},
                onItemClick = {},
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("watch_history_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("month_header_September 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("September 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("Severance").assertIsDisplayed()
        composeTestRule.onNodeWithText("S2E1 • Hello Ms. Cobel").assertIsDisplayed()
    }

    @Test
    fun watchHistory_clickItem_triggersCallbackWithCorrectItem() {
        var clickedItem: WatchHistoryUiItem? = null
        val items = listOf(sampleItem1)
        val grouped = items.groupBy { it.monthYearHeader }

        composeTestRule.setContent {
            WatchHistoryContent(
                uiState =
                    WatchHistoryUiState(
                        isAuthorized = true,
                        items = items,
                        groupedItems = grouped,
                    ),
                onSearchQueryChange = {},
                onItemClick = { clickedItem = it },
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("watch_history_item_101").performClick()

        assertNotNull(clickedItem)
        assertEquals(101L, clickedItem?.historyId)
        assertEquals("Severance", clickedItem?.showTitle)
        assertEquals(100, clickedItem?.showTraktId)
    }

    @Test
    fun watchHistory_searchQuery_clearButtonInvokesCallback() {
        var updatedQuery: String? = null

        composeTestRule.setContent {
            WatchHistoryContent(
                uiState =
                    WatchHistoryUiState(
                        isAuthorized = true,
                        searchQuery = "Severance",
                    ),
                onSearchQueryChange = { updatedQuery = it },
                onItemClick = {},
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("clear_search_button").performClick()
        assertEquals("", updatedQuery)
    }

    @Test
    fun watchHistory_viewModeToggle_displaysToggleButtonsAndTriggersCallback() {
        var selectedMode: WatchHistoryViewMode? = null

        composeTestRule.setContent {
            WatchHistoryContent(
                uiState = WatchHistoryUiState(isAuthorized = true),
                onSearchQueryChange = {},
                onViewModeChange = { selectedMode = it },
                onItemClick = {},
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("watch_history_view_mode_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("view_mode_shows").performClick()
        assertEquals(WatchHistoryViewMode.SHOWS, selectedMode)
    }

    @Test
    fun watchHistory_showsView_displaysShowsListAndInvokesCallback() {
        var clickedShow: WatchHistoryShowItem? = null
        val sampleShow =
            WatchHistoryShowItem(
                showTraktId = 100,
                showTvmazeId = 1234,
                showImdbId = "tt12345",
                showTitle = "Severance",
                imageUrl = "https://image.tmdb/severance.jpg",
                lastWatchedAt = "2026-09-20T20:00:00.000Z",
                formattedLastWatchedAt = "Sep 20, 2026 • 10:00 PM",
                episodesWatchedCount = 5,
                latestSeasonNumber = 2,
                latestEpisodeNumber = 1,
            )

        composeTestRule.setContent {
            WatchHistoryContent(
                uiState =
                    WatchHistoryUiState(
                        isAuthorized = true,
                        viewMode = WatchHistoryViewMode.SHOWS,
                        groupedShows = listOf(sampleShow),
                    ),
                onSearchQueryChange = {},
                onItemClick = {},
                onShowClick = { clickedShow = it },
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("watch_history_shows_list").assertIsDisplayed()
        composeTestRule.onNodeWithText("Severance").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 episodes").assertIsDisplayed()
        composeTestRule.onNodeWithTag("watch_history_show_100").performClick()

        assertNotNull(clickedShow)
        assertEquals(100, clickedShow?.showTraktId)
        assertEquals("Severance", clickedShow?.showTitle)
        assertEquals(5, clickedShow?.episodesWatchedCount)
    }

    @Test
    fun watchHistory_monthChips_displayedAndClickTriggersCallback() {
        var selectedMonth: String? = "initial"

        composeTestRule.setContent {
            WatchHistoryContent(
                uiState =
                    WatchHistoryUiState(
                        isAuthorized = true,
                        availableMonthYears = listOf("September 2026", "August 2026"),
                        selectedMonthFilter = null,
                    ),
                onSearchQueryChange = {},
                onMonthFilterChange = { selectedMonth = it },
                onItemClick = {},
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("watch_history_month_chips").assertIsDisplayed()
        composeTestRule.onNodeWithTag("month_chip_all").assertIsDisplayed()
        composeTestRule.onNodeWithTag("month_chip_September 2026").assertIsDisplayed()
        composeTestRule.onNodeWithTag("month_chip_September 2026").performClick()

        assertEquals("September 2026", selectedMonth)
    }

    @Test
    fun watchHistory_collapsedMonthHeader_hidesEpisodesUnderHeader() {
        val items = listOf(sampleItem1)
        val grouped = items.groupBy { it.monthYearHeader }

        composeTestRule.setContent {
            WatchHistoryContent(
                uiState =
                    WatchHistoryUiState(
                        isAuthorized = true,
                        items = items,
                        groupedItems = grouped,
                        collapsedMonths = setOf("September 2026"),
                    ),
                onSearchQueryChange = {},
                onItemClick = {},
                onLoadNextPage = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("month_header_September 2026").assertIsDisplayed()
        composeTestRule.onNodeWithTag("watch_history_item_101").assertDoesNotExist()
    }
}
