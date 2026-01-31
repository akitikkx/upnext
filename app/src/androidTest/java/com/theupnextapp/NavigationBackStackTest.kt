package com.theupnextapp

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.fetchSemanticsNodes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the Cast Bottom Sheet navigation fix (Hostage/Flicker issue).
 *
 * These tests require network connectivity and cached data to pass reliably.
 * Tests use assumeTrue to skip gracefully when data is unavailable.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@OptIn(
    androidx.compose.animation.ExperimentalAnimationApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.ui.test.ExperimentalTestApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class,
)
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
     * If running in an environment without cached data/network, test will be skipped.
     */
    @Test
    fun verifyCastBottomSheetBreadcrumbsAndExit() {
        // Wait for Dashboard content - skip if no data
        composeTestRule.waitForIdle()

        val hasShows =
            try {
                composeTestRule.waitUntil(timeoutMillis = 10000) {
                    composeTestRule.onAllNodesWithContentDescription("Show poster")
                        .fetchSemanticsNodes().isNotEmpty()
                }
                true
            } catch (e: ComposeTimeoutException) {
                false
            }
        assumeTrue("Skipping: No shows loaded (requires network/cached data)", hasShows)

        // Click first show to open Show Detail
        composeTestRule.onAllNodesWithContentDescription("Show poster").onFirst().performClick()

        // Wait for Show Detail
        val showDetailLoaded =
            try {
                composeTestRule.waitUntil(timeoutMillis = 5000) {
                    composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
                }
                true
            } catch (e: ComposeTimeoutException) {
                false
            }
        assumeTrue("Skipping: Show detail did not load", showDetailLoaded)

        // Wait for Cast items to load
        val hasCast =
            try {
                composeTestRule.waitUntil(timeoutMillis = 5000) {
                    composeTestRule.onAllNodesWithTag("cast_list_item").fetchSemanticsNodes().isNotEmpty()
                }
                true
            } catch (e: ComposeTimeoutException) {
                false
            }
        assumeTrue("Skipping: No cast data available", hasCast)

        // Click first cast member to open sheet
        composeTestRule.onAllNodesWithTag("cast_list_item").onFirst().performClick()
        composeTestRule.waitForIdle()

        // Verify Sheet Opens
        composeTestRule.onNodeWithTag("cast_bottom_sheet").assertExists()

        // Wait for credits to load inside sheet
        val hasCredits =
            try {
                composeTestRule.waitUntil(timeoutMillis = 5000) {
                    composeTestRule.onAllNodesWithTag("bottom_sheet_credit_item")
                        .fetchSemanticsNodes().isNotEmpty()
                }
                true
            } catch (e: ComposeTimeoutException) {
                false
            }
        assumeTrue("Skipping: No filmography credits available", hasCredits)

        // Navigate deeper - click a show from filmography
        composeTestRule.onAllNodesWithTag("bottom_sheet_credit_item").onFirst().performClick()
        composeTestRule.waitForIdle()

        // Verify new Show Detail loaded
        composeTestRule.onNodeWithText("Seasons").assertExists()

        // PRESS BACK (Simulate System Back)
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.waitForIdle()

        // VERIFY BREADCRUMB: Sheet should be VISIBLE (fix for "breadcrumbs")
        composeTestRule.onNodeWithTag("cast_bottom_sheet").assertIsDisplayed()

        // PRESS BACK AGAIN (Simulate dismissing sheet)
        // This verifies the "Hostage" fix - BackHandler should dismiss sheet
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.waitForIdle()

        // VERIFY DISMISSAL: Sheet should be GONE
        composeTestRule.onNodeWithTag("cast_bottom_sheet").assertDoesNotExist()

        // Verify we are still on the Show Detail screen
        composeTestRule.onNodeWithText("Seasons").assertExists()
    }
}
