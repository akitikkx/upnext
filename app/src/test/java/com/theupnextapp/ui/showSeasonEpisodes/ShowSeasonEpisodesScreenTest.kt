package com.theupnextapp.ui.showSeasonEpisodes

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.domain.ShowSeasonEpisode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@ExperimentalMaterial3Api
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class) // Bypass UpnextApplication to avoid Firebase init issues
class ShowSeasonEpisodesScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showSeasonEpisodes_unauthorizedUser_checkmarkHidden() {
        ShadowLog.stream = System.out // Enable logging

        val episodes =
            listOf(
                ShowSeasonEpisode(
                    id = 1,
                    number = 1,
                    season = 1,
                    name = "Pilot",
                    isWatched = false,
                    originalImageUrl = "",
                    mediumImageUrl = "",
                    summary = "Summary",
                    airstamp = "2023-01-01T20:00:00.000Z",
                    runtime = 60,
                    type = "scripted",
                    airdate = "2023-01-01",
                    airtime = "20:00",
                    imdbID = "tt1234567",
                ),
            )

        composeTestRule.setContent {
            ShowSeasonEpisodes(
                seasonNumber = 1,
                list = episodes,
                isAuthorizedOnTrakt = false,
            )
        }

        // Checkmark for "Mark as watched" should NOT be displayed
        composeTestRule.onNodeWithContentDescription("Mark as watched").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Mark as unwatched").assertDoesNotExist()
    }

    @Test
    fun showSeasonEpisodes_authorizedUser_checkmarkVisible() {
        val episodes =
            listOf(
                ShowSeasonEpisode(
                    id = 1,
                    number = 1,
                    season = 1,
                    name = "Pilot",
                    isWatched = false,
                    originalImageUrl = "",
                    mediumImageUrl = "",
                    summary = "Summary",
                    airstamp = "2023-01-01T20:00:00.000Z",
                    runtime = 60,
                    type = "scripted",
                    airdate = "2023-01-01",
                    airtime = "20:00",
                    imdbID = "tt1234567",
                ),
            )

        composeTestRule.setContent {
            ShowSeasonEpisodes(
                seasonNumber = 1,
                list = episodes,
                isAuthorizedOnTrakt = true,
            )
        }

        // Checkmark for "Mark as watched" SHOULD be displayed (because isWatched is false)
        composeTestRule.onNodeWithContentDescription("Mark as watched").assertIsDisplayed()
    }
}
