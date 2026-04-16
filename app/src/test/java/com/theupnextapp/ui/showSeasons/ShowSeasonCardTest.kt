package com.theupnextapp.ui.showSeasons

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.domain.ShowSeason
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalMaterial3Api
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class ShowSeasonCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockSeason =
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

    @Test
    fun showSeasonCard_unauthorizedUser_watchedCheckmarkHidden() {
        composeTestRule.setContent {
            ShowSeasonCard(
                item = mockSeason,
                isAuthorizedOnTrakt = false,
                onClick = {},
            )
        }

        // Watched checkmark should not be displayed
        composeTestRule.onNodeWithContentDescription("Watched").assertDoesNotExist()

        // Mark Season Unwatched button should not be displayed inside card
        composeTestRule.onNodeWithText("Mark Season Unwatched").assertDoesNotExist()
    }

    @Test
    fun showSeasonCard_authorizedUser_watchedCheckmarkVisible() {
        composeTestRule.setContent {
            ShowSeasonCard(
                item = mockSeason,
                isAuthorizedOnTrakt = true,
                onClick = {},
            )
        }

        // Watched checkmark should be displayed because isWatched is true
        composeTestRule.onNodeWithContentDescription("Watched").assertIsDisplayed()

        // Mark Season Unwatched button should be displayed inside card
        composeTestRule.onNodeWithText("Mark Season Unwatched").assertIsDisplayed()
    }
}
