package com.theupnextapp.screengrab

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.By
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Assume

@androidx.compose.animation.ExperimentalAnimationApi
@androidx.compose.foundation.ExperimentalFoundationApi
@androidx.compose.ui.ExperimentalComposeUiApi
@androidx.compose.material3.ExperimentalMaterial3Api
@androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
@androidx.compose.ui.test.ExperimentalTestApi
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ScreenshotGenerationTest {

    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    companion object {
        @ClassRule
        @JvmField
        val localeTestRule = LocaleTestRule()
    }

    private fun isTablet(): Boolean {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }

    @Before
    fun init() {
        hiltTestRule.inject()
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        
        try {
            androidx.work.testing.WorkManagerTestInitHelper.initializeTestWorkManager(
                InstrumentationRegistry.getInstrumentation().targetContext
            )
        } catch (e: Exception) {
            // Already initialized
        }
        
        // Enforce Dark Mode using UiDevice shell command before snapping
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("cmd uimode night yes")
        Thread.sleep(1500) // Allow OS to apply the dark theme fully
    }

    @Test
    fun capturePhoneAuthenticatedScreenshots() {
        // Skip this test if running on a tablet emulator
        Assume.assumeFalse(isTablet())

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Skip onboarding if it appears
        device.findObject(By.text("Skip"))?.click()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // 1. Dashboard Top
        composeTestRule.waitForIdle()
        
        try {
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                composeTestRule.onAllNodesWithTag("dashboard_show_card").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: Throwable) {}
        
        Screengrab.screenshot("01_dashboard_top")

        // 1.1 Show Detail
        try {
            val cards = composeTestRule.onAllNodesWithTag("dashboard_show_card")
            try {
                cards[1].performClick()
            } catch (e: Throwable) {
                cards.onFirst().performClick()
            }
            try {
                composeTestRule.waitUntil(timeoutMillis = 15000) {
                    composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
                }
                composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("watch_providers_loading"), timeoutMillis = 15000)
                composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("cast_loading"), timeoutMillis = 15000)
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
            Thread.sleep(4000) // Phone navigates to a new screen, small sleep to allow image rendering
            Screengrab.screenshot("02_show_detail")
            device.pressBack()
            composeTestRule.waitForIdle()
        } catch (e: AssertionError) {
            // Dashboard cards missing, fallback to Explore to get a show detail screenshot!
            try {
                device.findObject(By.text("Explore"))?.click()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithText("POPULAR", ignoreCase = true).performClick()
                composeTestRule.waitUntil(timeoutMillis = 45000) {
                    composeTestRule.onAllNodesWithTag("explore_bento_card").fetchSemanticsNodes().isNotEmpty()
                }
                val exploreCards = composeTestRule.onAllNodesWithTag("explore_bento_card")
                exploreCards.onFirst().performClick()
                
                try {
                    composeTestRule.waitUntil(timeoutMillis = 15000) {
                        composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
                    }
                    composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("watch_providers_loading"), timeoutMillis = 15000)
                    composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("cast_loading"), timeoutMillis = 15000)
                } catch (e: Throwable) {}
                
                composeTestRule.waitForIdle()
                Thread.sleep(4000)
                Screengrab.screenshot("02_show_detail")
                device.pressBack() // back to explore
                device.findObject(By.text("Dashboard"))?.click() // back to dashboard
                composeTestRule.waitForIdle()
            } catch (inner: Throwable) {}
        }

        // 1.2 Dashboard Bottom (Recent Activity)
        try {
            composeTestRule.onNodeWithText("Recent Activity").performScrollTo()
            composeTestRule.waitForIdle()
            Thread.sleep(4000) // Allow Recent Activity posters to render
            Screengrab.screenshot("03_dashboard_recent_activity")
        } catch (e: AssertionError) {
            // "Recent Activity" not found, fail gracefully
        }

        device.findObject(By.text("Explore"))?.click()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        
        try {
            composeTestRule.onNodeWithText("POPULAR", ignoreCase = true).performClick()
        } catch (e: Throwable) {}
        
        try {
            composeTestRule.waitUntil(timeoutMillis = 45000) {
                composeTestRule.onAllNodesWithTag("explore_bento_card").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: Throwable) { Thread.sleep(5000) }
        Screengrab.screenshot("04_explore")

        // 3. Schedule
        device.findObject(By.text("Schedule"))?.click()
        composeTestRule.waitForIdle()
        try {
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                composeTestRule.onAllNodesWithTag("show_item").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: Throwable) {}
        Screengrab.screenshot("05_schedule")
        
        // 4. Search
        device.findObject(By.text("Search"))?.click()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        Screengrab.screenshot("06_search")
    }

    @Test
    fun captureTabletUnauthenticatedScreenshots() {
        // Skip this test if running on a phone emulator
        Assume.assumeTrue(isTablet())

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Skip onboarding if it appears
        device.findObject(By.text("Skip"))?.click()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // 1. Dashboard Top
        composeTestRule.waitForIdle()
        try {
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                composeTestRule.onAllNodesWithTag("dashboard_show_card").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: Throwable) { Thread.sleep(5000) }
        
        // 1.1 Populate Detail Pane with the FIRST item
        val cards = composeTestRule.onAllNodesWithTag("dashboard_show_card")
        try {
            cards.onFirst().performClick()
        } catch (e: Throwable) {}
        
        try {
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("watch_providers_loading"), timeoutMillis = 15000)
            composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("cast_loading"), timeoutMillis = 15000)
        } catch (e: Throwable) {}
        composeTestRule.waitForIdle()
        Thread.sleep(4000) // Buffer for Coil images
        Screengrab.screenshot("01_dashboard_top")
        
        // 1.2 Click SECOND item to make detail pane distinct!
        try {
            cards[1].performClick()
        } catch (e: Throwable) {}
        
        try {
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("watch_providers_loading"), timeoutMillis = 15000)
            composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("cast_loading"), timeoutMillis = 15000)
        } catch (e: Throwable) {}
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        Screengrab.screenshot("02_show_detail")

        // 1.2 Dashboard Bottom (Recent Activity)
        try {
            composeTestRule.onNodeWithText("Recent Activity").performScrollTo()
            composeTestRule.waitForIdle()
            Thread.sleep(4000) // Allow Recent Activity posters to render
            Screengrab.screenshot("03_dashboard_recent_activity")
        } catch (e: AssertionError) {
            // "Recent Activity" not found, fail gracefully
        }

        device.findObject(By.text("Explore"))?.click()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        
        try {
            composeTestRule.onNodeWithText("POPULAR", ignoreCase = true).performClick()
        } catch (e: Throwable) {}
        
        try {
            composeTestRule.waitUntil(timeoutMillis = 45000) {
                composeTestRule.onAllNodesWithTag("explore_bento_card").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: Throwable) { Thread.sleep(5000) }
        
        // Populate Explore detail pane!
        try {
            composeTestRule.onAllNodesWithTag("explore_bento_card").onFirst().performClick()
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("watch_providers_loading"), timeoutMillis = 15000)
            composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("cast_loading"), timeoutMillis = 15000)
        } catch (e: Throwable) { Thread.sleep(5000) }
        composeTestRule.waitForIdle()
        Thread.sleep(2000) // Small buffer for Coil images

        Screengrab.screenshot("04_explore")

        device.findObject(By.text("Schedule"))?.click()
        composeTestRule.waitForIdle()
        try {
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                composeTestRule.onAllNodesWithTag("show_item").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: Throwable) {}
        
        // Populate Schedule detail pane!
        val hasShowItems = composeTestRule.onAllNodesWithTag("show_item").fetchSemanticsNodes().isNotEmpty()
        if (hasShowItems) {
            composeTestRule.onAllNodesWithTag("show_item").onFirst().performClick()
            try {
                composeTestRule.waitUntil(timeoutMillis = 15000) {
                    composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
                }
                composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("watch_providers_loading"), timeoutMillis = 15000)
                composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("cast_loading"), timeoutMillis = 15000)
            } catch (e: Throwable) {}
        }
        composeTestRule.waitForIdle()
        Thread.sleep(2000) // Small buffer for Coil images

        Screengrab.screenshot("05_schedule")
        
        device.findObject(By.text("Search"))?.click()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        
        // Populate Search detail pane!
        try {
            composeTestRule.onNodeWithTag("search_input").performTextReplacement("Batman")
            composeTestRule.onNodeWithTag("search_input").performImeAction()
            composeTestRule.waitForIdle()
            try {
                composeTestRule.waitUntil(timeoutMillis = 15000) {
                    composeTestRule.onAllNodesWithTag("search_result_card").fetchSemanticsNodes().isNotEmpty()
                }
            } catch (e: Throwable) {}
            
            val hasSearchResults = composeTestRule.onAllNodesWithTag("search_result_card").fetchSemanticsNodes().isNotEmpty()
            if (hasSearchResults) {
                composeTestRule.onAllNodesWithTag("search_result_card").onFirst().performClick()
                try {
                    composeTestRule.waitUntil(timeoutMillis = 15000) {
                        composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
                    }
                    composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("watch_providers_loading"), timeoutMillis = 15000)
                    composeTestRule.waitUntilDoesNotExist(androidx.compose.ui.test.hasTestTag("cast_loading"), timeoutMillis = 15000)
                } catch (e: Throwable) {}
            }
        } catch (e: Throwable) {}
        composeTestRule.waitForIdle()
        Thread.sleep(2000) // Small buffer for Coil images

        Screengrab.screenshot("06_search")
    }
}
