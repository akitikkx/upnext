package com.theupnextapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.ui.main.MainScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class NavigationBackStackTest {

    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltTestRule.inject()
    }

    /**
     * Verifies the critical fix for the "Hostage" situation.
     *
     * Flow:
     * 1. Open Show Detail (via Trending or similar).
     * 2. Open Cast Sheet.
     * 3. Navigate to a Show from the Sheet.
     * 4. Perform Back Navigation (Simulate System Back).
     * 5. Verify Sheet is PRESENT (Breadcrumb maintained).
     * 6. Perform Back Navigation AGAIN.
     * 7. Verify Sheet is DISMISSED (Hostage situation resolved).
     * 8. Verify Navigation back to original Show or Dashboard.
     *
     * Note: This test requires data to be available (Trending shows, Cast info).
     * If running in an environment without cached data/network, it may fail at step 2.
     */
    @Test
    fun verifyCastBottomSheetBreadcrumbsAndExit() {
        // 1. Initial State: Dashboard
        // Wait for content (Dashboard usually shows "Trending" or "Discover")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("Show poster").fetchSemanticsNodes().isNotEmpty()
        }
        
        // 2. Click first show to open Show Detail
        composeTestRule.onAllNodesWithContentDescription("Show poster").onFirst().performClick()
        
        // Wait for Show Detail to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Open Cast Sheet
        // Find cast items (Horizontal list)
        // If cast list is loading, we might need to wait.
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("cast_list_item").fetchSemanticsNodes().isNotEmpty()
        }
        
        // Click first cast member
        composeTestRule.onAllNodesWithTag("cast_list_item").onFirst().performClick()
        
        // 4. Verify Sheet Opens
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("cast_bottom_sheet").assertExists()
        
        // 5. Navigate Deeper (Click a show in Filmography)
        // Wait for credits to load inside sheet
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("bottom_sheet_credit_item").fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onAllNodesWithTag("bottom_sheet_credit_item").onFirst().performClick()
        
        // 6. Verify New Show Detail Loaded
        // We look for "Seasons" again, implying we are on a detail screen.
        // Ideally checking for a different title would be better, but generic check works for navigation flow.
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Seasons").assertExists()
        
        // 7. PRESS BACK (Simulate System Back)
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.waitForIdle()
        
        // 8. VERIFY BREADCRUMB: Sheet should be VISIBLE
        // The fix in "fix/sheet-persistence" ensures the sheet stays open.
        composeTestRule.onNodeWithTag("cast_bottom_sheet").assertIsDisplayed()
        
        // 9. PRESS BACK AGAIN (Simulate dismissing sheet)
        // This verifies the "Hostage" fix. The explicit BackHandler in ShowDetailScreen should catch this.
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.waitForIdle()
        
        // 10. VERIFY DISMISSAL: Sheet should be GONE
        composeTestRule.onNodeWithTag("cast_bottom_sheet").assertDoesNotExist()
        
        // 11. Verify we are still on the Show Detail screen (or handled gracefully)
        composeTestRule.onNodeWithText("Seasons").assertExists()
    }
}
