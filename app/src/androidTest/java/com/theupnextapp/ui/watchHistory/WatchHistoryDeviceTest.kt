package com.theupnextapp.ui.watchHistory

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.WorkManagerRule
import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.DatabaseWatchHistory
import com.theupnextapp.database.TraktDao
import com.theupnextapp.repository.SettingsRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalTestApi::class,
)
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class WatchHistoryDeviceTest {

    @get:Rule(order = -1)
    val workManagerRule = WorkManagerRule()

    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var traktDao: TraktDao

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        hiltTestRule.inject()
        runBlocking {
            settingsRepository.setOnboardingCompleted(true)
            traktDao.clearWatchHistory()
            traktDao.insertAllTraktAccessData(
                DatabaseTraktAccess(
                    id = 1,
                    access_token = "valid_test_token",
                    created_at = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                    expires_in = 7200L,
                    refresh_token = "valid_refresh_token",
                    scope = "public",
                    token_type = "Bearer",
                ),
            )
            traktDao.insertWatchHistory(
                listOf(
                    DatabaseWatchHistory(
                        historyId = 1001L,
                        watchedAt = "2026-09-15T20:30:00.000Z",
                        watchedAtEpochMillis = 1789500000000L,
                        showTraktId = 100,
                        episodeTraktId = 1001,
                        showTvmazeId = 1,
                        showImdbId = "tt100",
                        showTitle = "Severance",
                        seasonNumber = 1,
                        episodeNumber = 1,
                        episodeTitle = "Good News About Hell",
                        episodeImageUrl = null,
                        showPosterUrl = null,
                    ),
                    DatabaseWatchHistory(
                        historyId = 1002L,
                        watchedAt = "2026-08-10T19:00:00.000Z",
                        watchedAtEpochMillis = 1786400000000L,
                        showTraktId = 200,
                        episodeTraktId = 2001,
                        showTvmazeId = 2,
                        showImdbId = "tt200",
                        showTitle = "Slow Horses",
                        seasonNumber = 1,
                        episodeNumber = 1,
                        episodeTitle = "Failure's Contagious",
                        episodeImageUrl = null,
                        showPosterUrl = null,
                    ),
                ),
            )
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            traktDao.clearWatchHistory()
            traktDao.deleteTraktAccessData()
        }
    }

    @Test
    fun verifyRoomWatchHistoryPersistsAndQueriesCorrectlyOnDevice() {
        runBlocking {
            val count = traktDao.getWatchHistoryCount()
            assertEquals(2, count)

            val raw = traktDao.getWatchHistoryRaw()
            assertEquals(2, raw.size)
            assertEquals("Severance", raw[0].showTitle)
            assertEquals("Slow Horses", raw[1].showTitle)
        }
    }

    @Test
    fun navigateToWatchHistoryAndVerifyOfflineDataRendered() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.findObject(By.text("Skip"))?.click()
        composeTestRule.waitForIdle()

        // Wait for navigation bar/rail to settle and click Watch History
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithTag("WatchHistory").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("WatchHistory").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithTag("watch_history_item_1001").fetchSemanticsNodes().isNotEmpty()
        }

        // Take on-device screenshot
        val screenshotFile = File("/sdcard/Download/screen_watch_history_device_verified.png")
        device.takeScreenshot(screenshotFile)

        // Verify cached episodes from Room are rendered
        composeTestRule.onNodeWithText("Severance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Slow Horses").assertIsDisplayed()

        // Verify month headers exist
        composeTestRule.onAllNodesWithText("September 2026").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("August 2026").onFirst().assertIsDisplayed()

        // Test search input filters instantly
        composeTestRule.onNodeWithTag("watch_history_search_input").performTextInput("Sever")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Severance").assertIsDisplayed()
    }
}
