package com.theupnextapp.ui.traktAccount

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import com.theupnextapp.domain.TraktUserListItem
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class TraktAccountScrollTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun favoritesList_persistsScrollPosition_afterRecreation() {
        val restorationTester = StateRestorationTester(composeTestRule)

        // Generate a list large enough to scroll
        val items =
            List(50) { index ->
                TraktUserListItem(
                    id = index,
                    traktID = index,
                    title = "Show $index",
                    originalImageUrl = "",
                    mediumImageUrl = "",
                    imdbID = "",
                    slug = "",
                    tmdbID = index,
                    tvdbID = index,
                    tvMazeID = index,
                    year = "2024",
                )
            }

        restorationTester.setContent {
            val lazyGridState = rememberLazyGridState()

            FavoritesListContent(
                favoriteShows = items,
                widthSizeClass = WindowWidthSizeClass.Compact,
                lazyGridState = lazyGridState,
                onFavoriteClick = {},
            )
        }

        // Scroll to the 20th item
        composeTestRule.onNodeWithTag("favorites_grid")
            .performScrollToIndex(20)

        // Trigger recreation (like rotation or process death)
        restorationTester.emulateSavedInstanceStateRestore()

        // Verify that the item at index 20 is visible.
        // If scroll position was lost (reset to 0), this item would likely not be visible
        // (depending on screen size, but 20 items is likely offscreen for a grid).
        composeTestRule.onNodeWithText("Show 20").assertIsDisplayed()
    }
}
