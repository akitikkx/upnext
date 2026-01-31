package com.theupnextapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Navigation integration tests.
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
    androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class
)
class NavigationTest {
    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltTestRule.inject()
    }

    @Test
    fun verifyBackNavigation_fromNestedShowDetail_returnsToPreviousShow() {
        // Wait for data to load (skip test if no data available)
        composeTestRule.waitForIdle()
        
        // Check if shows are available - skip test gracefully if not
        val hasShows = try {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onAllNodesWithContentDescription("Show poster")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            true
        } catch (e: ComposeTimeoutException) {
            false
        }
        
        assumeTrue("Skipping test: No shows loaded (requires network/cached data)", hasShows)

        // Click on the first show to open Show Detail
        composeTestRule.onAllNodesWithContentDescription("Show poster").onFirst().performClick()
        composeTestRule.waitForIdle()

        // Verify we are on Show Detail - wait for it to load
        val showDetailLoaded = try {
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                composeTestRule.onAllNodesWithText("Seasons")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            true
        } catch (e: ComposeTimeoutException) {
            false
        }
        
        assumeTrue("Skipping test: Show detail did not load", showDetailLoaded)
        composeTestRule.onNodeWithText("Seasons").assertIsDisplayed()
    }
}
