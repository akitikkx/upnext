/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 */

package com.theupnextapp.ui.animation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.core.designsystem.ui.components.AnimatedWatchedToggleButton
import com.theupnextapp.core.designsystem.ui.components.AnimatedWatchlistIconButton
import com.theupnextapp.core.designsystem.ui.theme.LocalReducedMotion
import com.theupnextapp.core.designsystem.ui.widgets.UpNextEpisodeCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DelightAnimationAndAccessibilityDeviceTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun animatedWatchedToggleButton_onDevice_rendersAndToggles() {
        var toggled = false
        composeTestRule.setContent {
            AnimatedWatchedToggleButton(
                isWatched = false,
                onToggle = { toggled = true },
                contentDescription = "Mark as watched",
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("animated_watched_toggle_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue("Toggle callback should have been called on device", toggled)
    }

    @Test
    fun animatedWatchedToggleButton_underReducedMotion_operatesCalmly() {
        var toggled = false
        composeTestRule.setContent {
            CompositionLocalProvider(LocalReducedMotion provides true) {
                AnimatedWatchedToggleButton(
                    isWatched = false,
                    onToggle = { toggled = true },
                    contentDescription = "Mark as watched",
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("animated_watched_toggle_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue("Toggle under reduced motion should function calmly", toggled)
    }

    @Test
    fun animatedWatchlistIconButton_onDevice_rendersAndToggles() {
        var toggled = false
        composeTestRule.setContent {
            AnimatedWatchlistIconButton(
                isWatchlist = false,
                onToggle = { toggled = true },
                contentDescription = "Add to watchlist",
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("animated_watchlist_toggle_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue("Watchlist toggle callback should have been called on device", toggled)
    }

    @Test
    fun upNextEpisodeCard_withProgress_rendersOnDevice() {
        var markWatchedClicked = false
        composeTestRule.setContent {
            UpNextEpisodeCard(
                showTitle = "The Bear",
                episodeInfo = "S3 E1",
                airDateRibbon = "Tonight",
                progressPercentage = 75f,
                imageUrl = null,
                onMarkAsWatchedClick = { markWatchedClicked = true },
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("The Bear").assertIsDisplayed()
        composeTestRule.onNodeWithText("S3 E1").assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("mark_watched_card_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue("Mark watched button on card should be clickable", markWatchedClicked)
    }
}
