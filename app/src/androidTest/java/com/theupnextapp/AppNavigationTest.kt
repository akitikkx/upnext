/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.ui.navigation.AppNavigation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.ExperimentalComposeUiApi
import kotlinx.coroutines.ExperimentalCoroutinesApi

import androidx.work.Configuration
import androidx.work.WorkManager

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalCoroutinesApi::class,
)
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AppNavigationTest {

    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        try {
            val config = Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
            WorkManager.initialize(context, config)
        } catch (e: IllegalStateException) {
            // Already initialized in a previous test
        }
        hiltTestRule.inject()
    }

    @Test
    fun appNavigation_settingsScreen_isDisplayed() {
        composeTestRule.activity.setContent {
            val backStack = remember { mutableStateListOf<Any>(Destinations.Settings) }
            AppNavigation(
                backStack = backStack,
                onBack = {}
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(context.getString(R.string.settings_general)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_app_theme)).assertIsDisplayed()
    }

    @Test
    fun appNavigation_episodeDetailScreen_isDisplayed() {
        composeTestRule.activity.setContent {
            val backStack = remember {
                mutableStateListOf<Any>(
                    Destinations.EpisodeDetail(
                        showTraktId = 1234,
                        seasonNumber = 1,
                        episodeNumber = 5,
                        showTitle = "The Boys",
                    )
                )
            }
            AppNavigation(
                backStack = backStack,
                onBack = {}
            )
        }

        composeTestRule.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.back_arrow_content_description)).assertIsDisplayed()
    }

    @Test
    fun appNavigation_showDetailScreen_isDisplayed() {
        composeTestRule.activity.setContent {
            val backStack = remember {
                mutableStateListOf<Any>(
                    Destinations.ShowDetail(
                        showId = "123",
                        showTitle = "The Boys",
                        showImageUrl = "https://example.com/poster.jpg",
                        showBackgroundUrl = "https://example.com/backdrop.jpg"
                    )
                )
            }
            AppNavigation(
                backStack = backStack,
                onBack = {}
            )
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun appNavigation_showSeasonsScreen_isDisplayed() {
        composeTestRule.activity.setContent {
            val backStack = remember {
                mutableStateListOf<Any>(
                    Destinations.ShowSeasons(
                        showId = "123",
                        showTitle = "The Boys",
                        showImageUrl = "https://example.com/poster.jpg",
                        showBackgroundUrl = "https://example.com/backdrop.jpg"
                    )
                )
            }
            AppNavigation(
                backStack = backStack,
                onBack = {}
            )
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun appNavigation_showSeasonEpisodesScreen_isDisplayed() {
        composeTestRule.activity.setContent {
            val backStack = remember {
                mutableStateListOf<Any>(
                    Destinations.ShowSeasonEpisodes(
                        showId = 123,
                        seasonNumber = 1,
                        showTitle = "The Boys"
                    )
                )
            }
            AppNavigation(
                backStack = backStack,
                onBack = {}
            )
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun appNavigation_personDetailScreen_isDisplayed() {
        composeTestRule.activity.setContent {
            val backStack = remember {
                mutableStateListOf<Any>(
                    Destinations.PersonDetail(
                        personId = "123",
                        personName = "Karl Urban"
                    )
                )
            }
            AppNavigation(
                backStack = backStack,
                onBack = {}
            )
        }

        composeTestRule.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.person_detail_navigate_back)).assertIsDisplayed()
    }
}
