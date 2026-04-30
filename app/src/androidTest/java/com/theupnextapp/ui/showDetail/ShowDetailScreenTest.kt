package com.theupnextapp.ui.showDetail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import androidx.test.platform.app.InstrumentationRegistry
import com.theupnextapp.R
import org.junit.Rule
import org.junit.Test

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
)
class ShowDetailScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val testShowDetailArg =
        ShowDetailArg(
            showId = "123",
            showTitle = "Breaking Bad",
            showImageUrl = null,
            showBackgroundUrl = null,
            imdbID = "tt0903747",
            isAuthorizedOnTrakt = true,
            showTraktId = 1,
        )

    private val testShowSummary =
        ShowDetailSummary(
            id = 123,
            imdbID = "tt0903747",
            name = "Breaking Bad",
            averageRating = "9.5",
            mediumImageUrl = null,
            originalImageUrl = null,
            summary = "A high school chemistry teacher turned meth manufacturer.",
            genres = "Drama, Crime",
            time = null,
            previousEpisodeHref = null,
            nextEpisodeHref = null,
            status = "Ended",
            airDays = null,
            language = "English",
            nextEpisodeLinkedId = null,
            previousEpisodeLinkedId = null,
            tmdbID = 1396,
            network = "AMC",
            premiered = "2008-01-20",
        )

    @Test
    fun showDetailButtons_whenAuthorized_showsSeasonsAndWatchlistButtons() {
        rule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = true,
                isWatchlist = false,
                isLoading = false,
                onSeasonsClick = {},
                onWatchlistClick = {},
                onRateClick = {},
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithText(context.getString(R.string.btn_show_detail_seasons)).assertIsDisplayed()
        rule.onNodeWithText(context.getString(R.string.btn_show_detail_add_to_favorites)).assertIsDisplayed()
    }

    @Test
    fun showDetailButtons_whenNotAuthorized_showsOnlySeasonsButton() {
        rule.setContent {
            ShowDetailButtons(
                isAuthorizedOnTrakt = false,
                isWatchlist = false,
                isLoading = false,
                onSeasonsClick = {},
                onWatchlistClick = {},
                onRateClick = {},
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithText(context.getString(R.string.btn_show_detail_seasons)).assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorMessageAndRetryButton() {
        rule.setContent {
            ErrorState(
                message = "Network error occurred",
                onRetry = {},
            )
        }

        rule.onNodeWithText("Network error occurred").assertIsDisplayed()
        rule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun episodeSummary_displaysFormattedText() {
        rule.setContent {
            EpisodeSummary(summary = "Walter White begins his transformation.")
        }

        rule.onNodeWithText("Walter White begins his transformation.").assertIsDisplayed()
    }
}
