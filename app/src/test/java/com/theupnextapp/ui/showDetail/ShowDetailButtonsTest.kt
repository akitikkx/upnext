package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class ShowDetailButtonsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showDetailButtons_unauthorized_showsSeasonsButtonOnly() {
        composeTestRule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = false,
                isWatchlist = false,
                isLoading = false,
                onSeasonsClick = {},
                onWatchlistClick = {},
            )
        }

        composeTestRule.onNodeWithText("Seasons").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add to watchlist").assertDoesNotExist()
        composeTestRule.onNodeWithText("Remove from watchlist").assertDoesNotExist()
    }

    @Test
    fun showDetailButtons_authorized_notInWatchlist_showsAddToWatchlist() {
        composeTestRule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = true,
                isWatchlist = false,
                isLoading = false,
                onSeasonsClick = {},
                onWatchlistClick = {},
            )
        }

        composeTestRule.onNodeWithText("Seasons").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add to watchlist").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remove from watchlist").assertDoesNotExist()
    }

    @Test
    fun showDetailButtons_authorized_inWatchlist_showsRemoveFromWatchlist() {
        composeTestRule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = true,
                isWatchlist = true,
                isLoading = false,
                onSeasonsClick = {},
                onWatchlistClick = {},
            )
        }

        composeTestRule.onNodeWithText("Seasons").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remove from watchlist").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add to watchlist").assertDoesNotExist()
    }

    @Test
    fun showDetailButtons_authorized_isLoading_showsLoadingIndicator() {
        composeTestRule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = true,
                isWatchlist = false,
                isLoading = true,
                onSeasonsClick = {},
                onWatchlistClick = {},
            )
        }

        composeTestRule.onNodeWithText("Seasons").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add to watchlist").assertDoesNotExist()
        composeTestRule.onNodeWithText("Remove from watchlist").assertDoesNotExist()
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun showDetailButtons_clickAddToWatchlist_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = true,
                isWatchlist = false,
                isLoading = false,
                onSeasonsClick = {},
                onWatchlistClick = { clicked = true },
            )
        }

        composeTestRule.onNodeWithText("Add to watchlist").performClick()
        assertTrue(clicked)
    }

    @Test
    fun showDetailButtons_clickRemoveFromWatchlist_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = true,
                isWatchlist = true,
                isLoading = false,
                onSeasonsClick = {},
                onWatchlistClick = { clicked = true },
            )
        }

        composeTestRule.onNodeWithText("Remove from watchlist").performClick()
        assertTrue(clicked)
    }

    @Test
    fun showDetailButtons_clickSeasons_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = true,
                isWatchlist = false,
                isLoading = false,
                onSeasonsClick = { clicked = true },
                onWatchlistClick = {},
            )
        }

        composeTestRule.onNodeWithText("Seasons").performClick()
        assertTrue(clicked)
    }

    @Test
    fun showDetailButtons_expandedLayout_rendersCorrectly() {
        composeTestRule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = true,
                isWatchlist = true,
                isLoading = false,
                onSeasonsClick = {},
                onWatchlistClick = {},
                widthSizeClass = WindowWidthSizeClass.Expanded,
            )
        }

        composeTestRule.onNodeWithText("Seasons").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remove from watchlist").assertIsDisplayed()
    }
}
