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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
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
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.ui.episodeDetail.EpisodeTraktActions
import com.theupnextapp.ui.search.SearchInputField
import com.theupnextapp.ui.showSeasons.ShowSeasonCard
import org.junit.Assert.assertEquals
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun searchInputField_onDevice_clearsInputState() {
        val queryState = mutableStateOf("Severance")
        var reported = "Severance"
        composeTestRule.setContent {
            SearchInputField(
                inputLabel = "Search for a show...",
                valueState = queryState,
                onValueChange = { reported = it },
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("search_clear_button")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        assertEquals("", queryState.value)
        assertEquals("", reported)
        composeTestRule.onNodeWithTag("search_clear_button").assertDoesNotExist()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun showSeasonCard_onDevice_animatedWatchedToggleOperates() {
        var toggled = false
        val season =
            ShowSeason(
                id = 101,
                seasonNumber = 2,
                episodeCount = 8,
                name = "Season 2",
                premiereDate = null,
                endDate = null,
                originalImageUrl = null,
                mediumImageUrl = null,
                isWatched = false,
            )

        composeTestRule.setContent {
            ShowSeasonCard(
                item = season,
                isAuthorizedOnTrakt = true,
                onToggleWatched = { toggled = true },
                onClick = {},
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("season_watched_toggle_2")
            .assertIsDisplayed()
            .performClick()

        assertTrue("Season animated watched toggle should trigger callback on device", toggled)
    }

    @Test
    fun episodeTraktActions_onDevice_toggleWatchedOperates() {
        var toggled = false
        composeTestRule.setContent {
            EpisodeTraktActions(
                isWatched = false,
                isCheckingIn = false,
                isCheckInSuccessful = false,
                onToggleWatchedClick = { toggled = true },
                onCheckInClick = {},
                onCancelCheckInClick = {},
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("episode_toggle_watched_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue("Episode toggle watched button should trigger callback on device", toggled)
    }
}
