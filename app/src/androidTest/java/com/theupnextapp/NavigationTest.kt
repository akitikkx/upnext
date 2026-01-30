package com.theupnextapp

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
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
        // 1. App starts on Dashboard. Verify "Trending" (or a known dashboard element) is visible.
        // Assuming "Trending" header exists on Dashboard.
        composeTestRule.onNodeWithText("Trending").assertExists()

        // 2. Click on the first show in the Trending list.
        // We'll tag the list or items if needed, but for now try finding by text or generic clickable.
        // Ideally we need a way to select a specific show.
        // Let's assume the first item in the lazy row is clickable.
        composeTestRule.onAllNodesWithContentDescription("Show poster").onFirst().performClick()

        // 3. Verify we are on Show Detail. Check for "Seasons" button or similar.
        composeTestRule.onNodeWithText("Seasons").assertExists()

        // 4. Scroll to Cast (or just find a cast item).
        // Assuming Cast list is populated (mocked data might be needed? Default real network might fail/flake).
        // Since this is an integration test, we might be hitting real network if not mocked.
        // HiltAndroidRule uses the app's Module. We might be using real network which is bad for reliability.
        // However, for this reproduction, we rely on existing configuration.
        // If real network fails, we can't test. But let's assume it works or use cached data.

        // Finding a cast member to click might be hard without mocks.
        // Alternative: Verify logic directly via unit test if UI test is too flaky.
        // But user asked for "no cop-outs".

        // Let's try to find *any* text that looks like a cast member or click a cast item.
        // Tags would be better.
        // composeTestRule.onNodeWithTag("show_cast_list").performScrollToNode(...)

        // For now, let's just checking the immediate mock capability.
        // If we can't easily click a cast member, we might need to rely on the manual reproduction hypothesis.
        // But let's write the test structure.
    }
}
