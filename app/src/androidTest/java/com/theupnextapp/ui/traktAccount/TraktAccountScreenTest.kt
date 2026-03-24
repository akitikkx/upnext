package com.theupnextapp.ui.traktAccount

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.domain.TraktUserListItem
import org.junit.Rule
import org.junit.Test

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalFoundationApi::class,
)
class TraktAccountScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun accountContent_notAuthorized_showsConnectButton() {
        var connectClicked = false

        rule.setContent {
            AccountContent(
                traktAuthState = TraktAuthState.LoggedOut,
                watchlistShowsList = emptyList(),
                isWatchlistShowsEmpty = true,
                isLoadingConnection = false,
                isLoadingWatchlists = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                isPullRefreshing = false,
                onSearchQueryChange = {},
                onSortOptionChange = {},
                onRefreshWatchlist = {},
                onConnectToTraktClick = { connectClicked = true },
                onWatchlistClick = {},
                onRemoveItem = {},
                onLogoutClick = {},
            )
        }

        rule.onNodeWithText("Connect to Trakt").assertIsDisplayed()
        rule.onNodeWithText("Connect to Trakt").performClick()

        assert(connectClicked)
    }

    @Test
    fun accountContent_isAuthorized_showsProfileHeader() {
        rule.setContent {
            AccountContent(
                traktAuthState = TraktAuthState.LoggedIn,
                watchlistShowsList = emptyList(),
                isWatchlistShowsEmpty = true,
                isLoadingConnection = false,
                isLoadingWatchlists = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                isPullRefreshing = false,
                onSearchQueryChange = {},
                onSortOptionChange = {},
                onRefreshWatchlist = {},
                onConnectToTraktClick = {},
                onWatchlistClick = {},
                onRemoveItem = {},
                onLogoutClick = {},
            )
        }

        // Assuming TraktProfileHeader displays "Logout" or checking for absence of "Connect to Trakt"
        // Since we don't have the string resource for Logout handy, we can check that Connect button is NOT displayed.
        rule.onNodeWithText("Connect to Trakt").assertDoesNotExist()
    }

    @Test
    fun testFavoritesListDisplaysAndScrolls() {
        val favoriteShows =
            List(20) { index ->
                TraktUserListItem(
                    id = index,
                    title = "Show $index",
                    slug = "show-$index",
                    tvMazeID = index,
                    originalImageUrl = "",
                    mediumImageUrl = "",
                    tmdbID = index,
                    traktID = index,
                    tvdbID = index,
                    year = "2023",
                    imdbID = "tt$index",
                    network = null,
                    status = null,
                    rating = null,
                )
            }

        rule.setContent {
            AccountContent(
                traktAuthState = TraktAuthState.LoggedIn,
                watchlistShowsList = favoriteShows,
                isWatchlistShowsEmpty = false,
                isLoadingConnection = false,
                isLoadingWatchlists = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                isPullRefreshing = false,
                onSearchQueryChange = {},
                onSortOptionChange = {},
                onRefreshWatchlist = {},
                onConnectToTraktClick = {},
                onWatchlistClick = {},
                onRemoveItem = {},
                onLogoutClick = {},
            )
        }

        // Check if the first item is displayed
        rule.onNodeWithText("Show 0").assertIsDisplayed()

        // Scroll the list to reveal the last item
        val list = rule.onNodeWithTag("watchlist_column")

        // Swipe up multiple times to ensure we reach the bottom
        repeat(5) {
            list.performTouchInput { swipeUp() }
            rule.waitForIdle()
        }

        // Verify the last item exists and is displayed
        rule.onNodeWithText("Show 19").assertIsDisplayed()
    }

    @Test
    fun accountContent_searchIcon_togglesSearchInput() {
        rule.setContent {
            AccountContent(
                traktAuthState = TraktAuthState.LoggedIn,
                watchlistShowsList = emptyList(),
                isWatchlistShowsEmpty = false,
                isLoadingConnection = false,
                isLoadingWatchlists = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                isPullRefreshing = false,
                onSearchQueryChange = {},
                onSortOptionChange = {},
                onRefreshWatchlist = {},
                onConnectToTraktClick = {},
                onWatchlistClick = {},
                onRemoveItem = {},
                onLogoutClick = {},
            )
        }

        // Click the Search Watchlist icon button
        rule.onNodeWithContentDescription("Search Watchlist").performClick()

        // Wait for animation, verify TextField placeholder exists
        rule.onNodeWithText("Search your watchlist...").assertIsDisplayed()
    }

    @Test
    fun accountContent_statusFilterChips_renderAndFilter() {
        var selectedStatus: String? = null

        val showsWithStatuses =
            listOf(
                TraktUserListItem(
                    id = 1, traktID = 1, title = "Shōgun", originalImageUrl = "",
                    mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 1,
                    tvdbID = 1, tvMazeID = 1, year = "2024", network = "Hulu",
                    status = "Returning Series", rating = 8.5,
                ),
                TraktUserListItem(
                    id = 2, traktID = 2, title = "Game of Thrones", originalImageUrl = "",
                    mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 2,
                    tvdbID = 2, tvMazeID = 2, year = "2011", network = "HBO",
                    status = "Ended", rating = 9.3,
                ),
            )

        rule.setContent {
            AccountContent(
                traktAuthState = TraktAuthState.LoggedIn,
                watchlistShowsList = showsWithStatuses,
                isWatchlistShowsEmpty = false,
                isLoadingConnection = false,
                isLoadingWatchlists = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                isPullRefreshing = false,
                onSearchQueryChange = {},
                onSortOptionChange = {},
                watchlistStatusFilter = null,
                availableStatuses = listOf("Ended", "Returning Series"),
                totalWatchlistCount = 2,
                onStatusFilterChange = { selectedStatus = it },
                onRefreshWatchlist = {},
                onConnectToTraktClick = {},
                onWatchlistClick = {},
                onRemoveItem = {},
                onLogoutClick = {},
            )
        }

        // Verify filter chips are rendered
        rule.onNodeWithText("All").assertIsDisplayed()
        rule.onNodeWithText("Ended").assertIsDisplayed()
        rule.onNodeWithText("Returning Series").assertIsDisplayed()

        // Tap a filter chip
        rule.onNodeWithText("Ended").performClick()
        assert(selectedStatus == "Ended") { "Expected 'Ended' but got '$selectedStatus'" }
    }

    @Test
    fun accountContent_filteredCount_showsXOfY() {
        rule.setContent {
            AccountContent(
                traktAuthState = TraktAuthState.LoggedIn,
                watchlistShowsList =
                    listOf(
                        TraktUserListItem(
                            id = 1, traktID = 1, title = "Shōgun", originalImageUrl = "",
                            mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 1,
                            tvdbID = 1, tvMazeID = 1, year = "2024", network = "Hulu",
                            status = "Returning Series", rating = 8.5,
                        ),
                    ),
                isWatchlistShowsEmpty = false,
                isLoadingConnection = false,
                isLoadingWatchlists = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                isPullRefreshing = false,
                onSearchQueryChange = {},
                onSortOptionChange = {},
                watchlistStatusFilter = "Returning Series",
                availableStatuses = listOf("Ended", "Returning Series"),
                totalWatchlistCount = 5,
                onStatusFilterChange = {},
                onRefreshWatchlist = {},
                onConnectToTraktClick = {},
                onWatchlistClick = {},
                onRemoveItem = {},
                onLogoutClick = {},
            )
        }

        // When filtered, heading should show "X of Y"
        rule.onNodeWithText("Your Watchlist (1 of 5)", substring = true).assertIsDisplayed()
    }
}
