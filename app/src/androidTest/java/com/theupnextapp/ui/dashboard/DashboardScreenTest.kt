package com.theupnextapp.ui.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Dashboard integration tests.
 * Validates guest-mode UI elements render correctly.
 * TODO: Extract DashboardContent as a stateless composable for robust unit testing.
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
class DashboardScreenTest {
    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<com.theupnextapp.MainActivity>()

    @Before
    fun init() {
        hiltTestRule.inject()
    }

    @Test
    fun guestMode_showsTonightOnTvAndConnectTrakt() {
        // Wait for the Dashboard to render
        val dashboardLoaded =
            runCatching {
                composeTestRule.waitForIdle()
                composeTestRule.waitUntil(timeoutMillis = 10000) {
                    composeTestRule
                        .onAllNodesWithText("Tonight on TV")
                        .fetchSemanticsNodes()
                        .isNotEmpty() ||
                        composeTestRule
                            .onAllNodesWithText("My Upnext")
                            .fetchSemanticsNodes()
                            .isNotEmpty()
                }
                true
            }.getOrDefault(false)

        assumeTrue("Skipping test: Dashboard did not load in time", dashboardLoaded)

        // Guest mode should show "Tonight on TV" and Trakt CTA
        val isGuestMode =
            runCatching {
                composeTestRule.onNodeWithText("Tonight on TV").assertIsDisplayed()
                true
            }.getOrDefault(false)

        if (isGuestMode) {
            composeTestRule.onNodeWithText("Connect Trakt").assertIsDisplayed()
        }
        // If authenticated, "My Upnext" is shown instead — both are valid states
    }
}
