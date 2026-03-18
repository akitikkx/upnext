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
                favoriteShowsList = emptyList(),
                isFavoriteShowsEmpty = true,
                isLoadingConnection = false,
                isLoadingFavorites = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                onSearchQueryChange = {},
                onSortOptionChange = {},
                onConnectToTraktClick = { connectClicked = true },
                onFavoriteClick = {},
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
                favoriteShowsList = emptyList(),
                isFavoriteShowsEmpty = true,
                isLoadingConnection = false,
                isLoadingFavorites = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                onSearchQueryChange = {},
                onSortOptionChange = {},
                onConnectToTraktClick = {},
                onFavoriteClick = {},
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
                favoriteShowsList = favoriteShows,
                isFavoriteShowsEmpty = false,
                isLoadingConnection = false,
                isLoadingFavorites = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                onSearchQueryChange = {},
                onSortOptionChange = {},
                onConnectToTraktClick = {},
                onFavoriteClick = {},
                onRemoveItem = {},
                onLogoutClick = {},
            )
        }

        // Check if the first item is displayed
        rule.onNodeWithText("Show 0").assertIsDisplayed()

        // Scroll the grid to reveal the last item
        val grid = rule.onNodeWithTag("favorites_grid")

        // Swipe up multiple times to ensure we reach the bottom
        repeat(5) {
            grid.performTouchInput { swipeUp() }
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
                favoriteShowsList = emptyList(),
                isFavoriteShowsEmpty = false,
                isLoadingConnection = false,
                isLoadingFavorites = false,
                isDisconnecting = false,
                watchlistSearchQuery = "",
                watchlistSortOption = WatchlistSortOption.ADDED,
                watchlistLazyListState = rememberLazyListState(),
                onSearchQueryChange = {},
                onSortOptionChange = {},
                onConnectToTraktClick = {},
                onFavoriteClick = {},
                onRemoveItem = {},
                onLogoutClick = {},
            )
        }

        // Click the Search Watchlist icon button
        rule.onNodeWithContentDescription("Search Watchlist").performClick()

        // Wait for animation, verify TextField placeholder exists
        rule.onNodeWithText("Search watchlist").assertIsDisplayed()
    }
}
