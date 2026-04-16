package com.theupnextapp.ui.episodeDetail

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.domain.EpisodeDetail
import com.theupnextapp.domain.EpisodeDetailArg
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalMaterial3Api
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class EpisodeSummaryCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockEpisodeDetailArg =
        EpisodeDetailArg(
            showId = 1,
            showTitle = "Test Show",
            showImageUrl = null,
            showBackgroundUrl = null,
            imdbID = null,
            isAuthorizedOnTrakt = false,
            showTraktId = 1,
            seasonNumber = 1,
            episodeNumber = 5,
        )

    private val mockEpisodeDetail =
        EpisodeDetail(
            title = "Test Episode",
            overview = "Test Overview",
            season = 1,
            number = 5,
            firstAired = "2026-01-01T00:00:00Z",
            runtime = 60,
            rating = 8.5,
            tvdbId = null,
            imdbId = "tt123",
            tmdbId = null,
            votes = 100,
        )

    @Test
    fun episodeSummaryCard_unauthorizedUser_checkInButtonHidden() {
        composeTestRule.setContent {
            val uriHandler = LocalUriHandler.current
            EpisodeSummaryCard(
                episodeDetailArg = mockEpisodeDetailArg,
                episodeDetail = mockEpisodeDetail,
                uriHandler = uriHandler,
                isCheckingIn = false,
                isCheckInSuccessful = false,
                isAuthorizedOnTrakt = false,
                onCheckInClick = {},
                onCancelCheckInClick = {},
            )
        }

        // Check-in button should not be displayed
        composeTestRule.onNodeWithText("Check In to Episode on Trakt").assertDoesNotExist()
        composeTestRule.onNodeWithText("Cancel Check-in").assertDoesNotExist()
    }

    @Test
    fun episodeSummaryCard_authorizedUser_checkInButtonVisible() {
        composeTestRule.setContent {
            val uriHandler = LocalUriHandler.current
            EpisodeSummaryCard(
                episodeDetailArg = mockEpisodeDetailArg,
                episodeDetail = mockEpisodeDetail,
                uriHandler = uriHandler,
                isCheckingIn = false,
                isCheckInSuccessful = false,
                isAuthorizedOnTrakt = true,
                onCheckInClick = {},
                onCancelCheckInClick = {},
            )
        }

        // Check-in button should be displayed
        composeTestRule.onNodeWithText("Check In to Episode on Trakt").assertIsDisplayed()
    }
}
