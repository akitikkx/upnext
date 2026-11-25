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

package com.theupnextapp.ui.search

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.domain.RecentSearch
import com.theupnextapp.domain.ShowSearch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class SearchScreenRobolectricTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchInputField_emptyQuery_clearButtonNotDisplayed() {
        val queryState = mutableStateOf("")
        composeTestRule.setContent {
            SearchInputField(
                inputLabel = "Search for a show...",
                valueState = queryState,
                onValueChange = {},
            )
        }

        composeTestRule.onNodeWithTag("search_clear_button").assertDoesNotExist()
    }

    @Test
    fun searchInputField_withQuery_clearButtonDisplayedAndClearsInput() {
        val queryState = mutableStateOf("Severance")
        var reportedValue = "Severance"
        composeTestRule.setContent {
            SearchInputField(
                inputLabel = "Search for a show...",
                valueState = queryState,
                onValueChange = { reportedValue = it },
            )
        }

        composeTestRule.onNodeWithTag("search_clear_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_clear_button").performClick()

        assertEquals("", queryState.value)
        assertEquals("", reportedValue)
        composeTestRule.onNodeWithTag("search_clear_button").assertDoesNotExist()
    }

    @Test
    fun recentSearchesList_rendersItemsAndHandlesClicks() {
        var clickedQuery = ""
        var clearClicked = false
        val recentSearches =
            listOf(
                RecentSearch(id = 1, query = "Ted Lasso", searchTime = 1000L),
                RecentSearch(id = 2, query = "Silo", searchTime = 2000L),
            )

        composeTestRule.setContent {
            RecentSearchesList(
                list = recentSearches,
                onRecentSearchClick = { clickedQuery = it },
                onClearRecentSearches = { clearClicked = true },
            )
        }

        composeTestRule.onNodeWithText("Ted Lasso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Silo").assertIsDisplayed()

        composeTestRule.onNodeWithText("Ted Lasso").performClick()
        assertEquals("Ted Lasso", clickedQuery)

        composeTestRule.onNodeWithTag("search_clear_recent_button").performClick()
        assertTrue(clearClicked)
    }

    @Test
    fun searchResultsList_rendersResultsAndHandlesClicks() {
        var selectedItem: ShowSearch? = null
        val results =
            listOf(
                ShowSearch(
                    id = 10,
                    name = "Severance",
                    premiered = "2022",
                    mediumImageUrl = null,
                    originalImageUrl = null,
                    genres = null,
                    language = null,
                    runtime = null,
                    status = null,
                    summary = null,
                    type = null,
                    updated = null,
                ),
            )

        composeTestRule.setContent {
            SearchResultsList(
                list = results,
                onClick = { selectedItem = it },
            )
        }

        composeTestRule.onNodeWithText("Severance (2022)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_result_card").performClick()
        assertEquals(10, selectedItem?.id)
    }
}
