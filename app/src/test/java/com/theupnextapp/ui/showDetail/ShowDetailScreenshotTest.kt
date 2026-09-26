package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.theupnextapp.core.designsystem.ui.theme.UpnextBackgroundDark
import com.theupnextapp.core.designsystem.ui.theme.UpnextTheme
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.Theme
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.ui.previewdata.SampleShowDetailSummary
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class)
class ShowDetailScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSummary = SampleShowDetailSummary.summaryMinimal
    private val sampleRating = TraktShowRating(rating = 7.1, votes = 1878, distribution = null)
    private val sampleArgs = ShowDetailArg(
        source = "dashboard",
        showId = "1",
        showTitle = "Awesome Show Title",
        showImageUrl = "http://example.com/medium.jpg",
        showBackgroundUrl = "http://example.com/original.jpg",
        imdbID = "tt1234567",
        isAuthorizedOnTrakt = true,
        showTraktId = 1,
    )
    private val sampleUiState = ShowDetailViewModel.ShowDetailUiState(
        showSummary = sampleSummary,
        certification = "TV-14",
        isRating = false,
        userRating = null,
    )

    private val roborazziOptions = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(
            resultValidator = { result ->
                // Allow up to 1.5% difference to account for cross-platform (macOS vs Linux) anti-aliasing
                val diffRate = result.pixelDifferences.toDouble() / result.pixelCount.toDouble()
                diffRate <= 0.015
            },
        ),
    )

    @Test
    @Config(qualifiers = "w411dp-h891dp-port")
    fun detailArea_phonePortrait() {
        composeTestRule.setContent {
            UpnextTheme(themeState = Theme.DARK) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DetailArea(
                        uiState = sampleUiState,
                        showDetailArgs = sampleArgs,
                        isAuthorizedOnTrakt = true,
                        isWatchlist = false,
                        isWatchlistLoading = false,
                        showRating = sampleRating,
                        showStats = null,
                        onSeasonsClick = {},
                        onWatchlistClick = {},
                        onRateClick = {},
                        onCastItemClick = {},
                        onSimilarShowClick = {},
                        onRetry = {},
                        onBack = {},
                        contentPadding = PaddingValues(0.dp),
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    @Config(qualifiers = "w800dp-h1280dp-port")
    fun detailArea_tabletPortrait() {
        composeTestRule.setContent {
            UpnextTheme(themeState = Theme.DARK) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DetailArea(
                        uiState = sampleUiState,
                        showDetailArgs = sampleArgs,
                        isAuthorizedOnTrakt = true,
                        isWatchlist = false,
                        isWatchlistLoading = false,
                        showRating = sampleRating,
                        showStats = null,
                        onSeasonsClick = {},
                        onWatchlistClick = {},
                        onRateClick = {},
                        onCastItemClick = {},
                        onSimilarShowClick = {},
                        onRetry = {},
                        onBack = {},
                        contentPadding = PaddingValues(0.dp),
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    @Config(qualifiers = "w1280dp-h800dp-land")
    fun detailArea_tabletLandscape() {
        composeTestRule.setContent {
            UpnextTheme(themeState = Theme.DARK) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DetailArea(
                        uiState = sampleUiState,
                        showDetailArgs = sampleArgs,
                        isAuthorizedOnTrakt = true,
                        isWatchlist = false,
                        isWatchlistLoading = false,
                        showRating = sampleRating,
                        showStats = null,
                        onSeasonsClick = {},
                        onWatchlistClick = {},
                        onRateClick = {},
                        onCastItemClick = {},
                        onSimilarShowClick = {},
                        onRetry = {},
                        onBack = {},
                        contentPadding = PaddingValues(0.dp),
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun traktRatingSummary_componentSnapshot() {
        composeTestRule.setContent {
            UpnextTheme(themeState = Theme.DARK) {
                Surface(
                    color = UpnextBackgroundDark,
                    modifier = Modifier
                        .width(400.dp)
                        .padding(16.dp),
                ) {
                    TraktRatingSummary(
                        rating = sampleRating,
                        userRating = 8,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun showDetailButtonsExpanded_componentSnapshot() {
        composeTestRule.setContent {
            UpnextTheme(themeState = Theme.DARK) {
                Surface(
                    color = UpnextBackgroundDark,
                    modifier = Modifier.padding(16.dp),
                ) {
                    ShowDetailButtons(
                        isAuthorizedOnTrakt = true,
                        isWatchlist = false,
                        isLoading = false,
                        onSeasonsClick = {},
                        onWatchlistClick = {},
                        onRateClick = {},
                        userRating = 8,
                        isRating = false,
                        widthSizeClass = WindowWidthSizeClass.Expanded,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }
}
