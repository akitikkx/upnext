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

package com.theupnextapp.ui.components

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class AnimatedToggleTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun animatedWatchedToggleButton_unwatched_rendersAndHandlesToggle() {
        var toggled = false
        composeTestRule.setContent {
            AnimatedWatchedToggleButton(
                isWatched = false,
                onToggle = { toggled = true },
                contentDescription = "Mark as watched",
            )
        }

        composeTestRule
            .onNodeWithTag("animated_watched_toggle_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue(toggled)
    }

    @Test
    fun animatedWatchedToggleButton_watched_rendersCorrectContentDescription() {
        composeTestRule.setContent {
            AnimatedWatchedToggleButton(
                isWatched = true,
                onToggle = {},
                contentDescription = "Mark as unwatched",
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Mark as unwatched")
            .assertIsDisplayed()
    }

    @Test
    fun animatedWatchedToggleButton_reducedMotion_rendersAndTogglesCleanly() {
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

        composeTestRule
            .onNodeWithTag("animated_watched_toggle_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue(toggled)
    }

    @Test
    fun animatedWatchlistIconButton_togglesStateCleanly() {
        var toggled = false
        composeTestRule.setContent {
            AnimatedWatchlistIconButton(
                isWatchlist = false,
                onToggle = { toggled = true },
                contentDescription = "Add to watchlist",
            )
        }

        composeTestRule
            .onNodeWithTag("animated_watchlist_toggle_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue(toggled)
    }

    @Test
    fun upNextEpisodeCard_withProgressPercentage_rendersCorrectly() {
        var cardClicked = false
        var watchedClicked = false

        composeTestRule.setContent {
            UpNextEpisodeCard(
                showTitle = "Severance",
                episodeInfo = "S2 E1",
                airDateRibbon = "Today",
                progressPercentage = 50f,
                imageUrl = null,
                onCardClick = { cardClicked = true },
                onMarkAsWatchedClick = { watchedClicked = true },
            )
        }

        composeTestRule.onNodeWithText("Severance").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("S2 E1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("mark_watched_card_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue(cardClicked)
        assertTrue(watchedClicked)
    }
}
