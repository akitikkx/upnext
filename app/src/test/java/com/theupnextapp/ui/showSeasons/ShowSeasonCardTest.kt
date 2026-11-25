package com.theupnextapp.ui.showSeasons

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.domain.ShowSeason
import org.junit.Assert.assertTrue
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

    @Test
    fun showSeasonCard_authorizedUser_clickToggleWatched_callsCallback() {
        var toggled = false
        composeTestRule.setContent {
            ShowSeasonCard(
                item = mockSeason,
                isAuthorizedOnTrakt = true,
                onToggleWatched = { toggled = true },
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("Mark Season Unwatched").performClick()
        assertTrue(toggled)
    }

    @Test
    fun showSeasonCard_authorizedUser_unwatchedSeason_clickToggleWatched_callsCallback() {
        var toggled = false
        val unwatchedSeason = mockSeason.copy(isWatched = false)
        composeTestRule.setContent {
            ShowSeasonCard(
                item = unwatchedSeason,
                isAuthorizedOnTrakt = true,
                onToggleWatched = { toggled = true },
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("Mark Season Watched").performClick()
        assertTrue(toggled)
    }

    @Test
    fun showSeasonCard_clickAnimatedToggleWatched_callsCallback() {
        var toggled = false
        composeTestRule.setContent {
            ShowSeasonCard(
                item = mockSeason,
                isAuthorizedOnTrakt = true,
                onToggleWatched = { toggled = true },
                onClick = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Watched").performClick()
        assertTrue(toggled)
    }

    @Test
    fun showSeasonCard_displaysEpisodeCount_whenAvailable() {
        val season = mockSeason.copy(episodeCount = 12)
        composeTestRule.setContent {
            ShowSeasonCard(
                item = season,
                isAuthorizedOnTrakt = false,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("12 Episodes").assertIsDisplayed()
    }

    @Test
    fun showSeasonCard_futureEndDate_displaysEndsLabel() {
        // Date far in the future
        val season = mockSeason.copy(endDate = "2035-12-31")
        composeTestRule.setContent {
            ShowSeasonCard(
                item = season,
                isAuthorizedOnTrakt = false,
                onClick = {},
            )
        }

        // Should say "Ends: Dec 31, 2035", not "Ended: Dec 31, 2035"
        composeTestRule.onNodeWithText("Ends: Dec 31, 2035").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ended: Dec 31, 2035").assertDoesNotExist()
    }

    @Test
    fun showSeasonCard_pastEndDate_displaysEndedLabel() {
        // Date in the past
        val season = mockSeason.copy(endDate = "2015-05-20")
        composeTestRule.setContent {
            ShowSeasonCard(
                item = season,
                isAuthorizedOnTrakt = false,
                onClick = {},
            )
        }

        // Should say "Ended: May 20, 2015"
        composeTestRule.onNodeWithText("Ended: May 20, 2015").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ends: May 20, 2015").assertDoesNotExist()
    }

    @Test
    fun showSeasonCard_futurePremiereDate_displaysPremieresLabel() {
        val season = mockSeason.copy(premiereDate = "2035-09-15")
        composeTestRule.setContent {
            ShowSeasonCard(
                item = season,
                isAuthorizedOnTrakt = false,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("Premieres: Sep 15, 2035").assertIsDisplayed()
        composeTestRule.onNodeWithText("Premiered: Sep 15, 2035").assertDoesNotExist()
    }

    @Test
    fun showSeasonCard_pastPremiereDate_displaysPremieredLabel() {
        val season = mockSeason.copy(premiereDate = "2015-01-10")
        composeTestRule.setContent {
            ShowSeasonCard(
                item = season,
                isAuthorizedOnTrakt = false,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("Premiered: Jan 10, 2015").assertIsDisplayed()
        composeTestRule.onNodeWithText("Premieres: Jan 10, 2015").assertDoesNotExist()
    }
}
