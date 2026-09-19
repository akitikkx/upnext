package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class BackdropAndTitleTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockShowDetailArg =
        ShowDetailArg(
            showId = "100",
            showTitle = "Severance",
            showImageUrl = "https://example.com/poster.jpg",
            showBackgroundUrl = "https://example.com/backdrop.jpg",
            imdbID = "tt11280740",
            isAuthorizedOnTrakt = false,
            showTraktId = 123,
        )

    private val seededShowSummary =
        ShowDetailSummary(
            id = 100,
            imdbID = "tt11280740",
            name = "Severance",
            averageRating = null,
            mediumImageUrl = "https://example.com/poster.jpg",
            originalImageUrl = "https://example.com/backdrop.jpg",
            summary = null,
            genres = null,
            time = null,
            previousEpisodeHref = null,
            nextEpisodeHref = null,
            status = null,
            airDays = null,
            language = null,
            nextEpisodeLinkedId = null,
            previousEpisodeLinkedId = null,
            tmdbID = null,
            network = null,
            premiered = null,
        )

    @Test
    fun backdropAndTitle_seededInitialData_displaysTitleImmediately() {
        composeTestRule.setContent {
            BackdropAndTitle(
                showDetailArgs = mockShowDetailArg,
                showSummary = seededShowSummary,
                certification = null,
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("Severance").assertIsDisplayed()
    }

    @Test
    fun backdropAndTitle_withStatusAndCertification_displaysBothAndBullet() {
        val fullSummary =
            seededShowSummary.copy(
                status = "Returning Series",
            )

        composeTestRule.setContent {
            BackdropAndTitle(
                showDetailArgs = mockShowDetailArg,
                showSummary = fullSummary,
                certification = "TV-MA",
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("Returning Series").assertIsDisplayed()
        composeTestRule.onNodeWithText("TV-MA").assertIsDisplayed()
        composeTestRule.onNodeWithText("•").assertIsDisplayed()
    }

    @Test
    fun backdropAndTitle_backButtonClick_triggersOnBackCallback() {
        var backClicked = false
        composeTestRule.setContent {
            BackdropAndTitle(
                showDetailArgs = mockShowDetailArg,
                showSummary = seededShowSummary,
                certification = null,
                onBack = { backClicked = true },
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun backdropAndTitle_seededWithoutCertification_displaysOnlyStatusWithoutBullet() {
        val summaryWithStatusOnly =
            seededShowSummary.copy(
                status = "Ended",
            )

        composeTestRule.setContent {
            BackdropAndTitle(
                showDetailArgs = mockShowDetailArg,
                showSummary = summaryWithStatusOnly,
                certification = null,
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("Ended").assertIsDisplayed()
        composeTestRule.onNodeWithText("•").assertDoesNotExist()
    }
}
