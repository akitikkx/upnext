package com.theupnextapp.ui.showDetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class ShowDetailButtonsAdaptiveTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun seasonsButton_onExpandedTablet_doesNotStretchToFullWidth() {
        val tabletWidth = 1000.dp

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(tabletWidth, 800.dp)),
            ) {
                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    ShowDetailButtons(
                        isAuthorizedOnTrakt = true,
                        isWatchlist = false,
                        isLoading = false,
                        isRating = false,
                        userRating = null,
                        onSeasonsClick = {},
                        onWatchlistClick = {},
                        onRateClick = {},
                        widthSizeClass = WindowWidthSizeClass.Expanded,
                    )
                }
            }
        }

        // On a tablet (1000dp wide), the old implementation stretched the button to the full width
        // Wait for UI to settle just in case
        composeTestRule.waitForIdle()

        // Get the seasons button
        // Note: OutlinedButton has semantics { mergeDescendants = true } so onNodeWithText matches the Button itself
        val seasonsButton = composeTestRule.onNodeWithText("Seasons", useUnmergedTree = true)

        // Let's print the semantics tree for debugging if it fails
        composeTestRule.onRoot().printToLog("ADAPTIVE_TEST")

        // Assert that the button's width is significantly smaller than the screen width.
        // It has widthIn(min = 120.dp), it should NOT be 1000dp or even close to it.
        // The max button width should be around 120-200dp based on text content.

        // We can't use assertBounds or assertWidthIsEqualTo with an exact value easily because of font scaling,
        // but we can assert it is NOT stretched to the parent width (1000dp) minus paddings.
        seasonsButton.assert(
            androidx.compose.ui.test.SemanticsMatcher("Width is less than half tablet width") { node ->
                node.boundsInWindow.width < (1000f * node.layoutInfo.density.density) / 2f
            },
        )
    }
}
