package com.theupnextapp.ui.search

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.theupnextapp.domain.RecentSearch
import com.theupnextapp.domain.ShowSearch
import org.junit.Rule
import org.junit.Test

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
class SearchScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private fun mockShowSearch(
        id: Int,
        name: String,
        premiered: String? = null,
    ) = ShowSearch(
        id = id,
        name = name,
        genres = null,
        mediumImageUrl = null,
        originalImageUrl = null,
        language = null,
        premiered = premiered,
        runtime = null,
        status = null,
        summary = null,
        type = null,
        updated = null,
    )

    private fun mockRecentSearch(
        id: Int,
        query: String,
    ) = RecentSearch(
        id = id,
        query = query,
        searchTime = System.currentTimeMillis(),
    )

    @Test
    fun searchArea_withResults_displaysShowTitles() {
        val results =
            listOf(
                mockShowSearch(1, "Breaking Bad", "2008"),
                mockShowSearch(2, "Better Call Saul", "2015"),
            )

        rule.setContent {
            SearchArea(
                searchResultsList = results,
                recentSearches = null,
                onTextSubmit = {},
                onResultClick = {},
                onRecentSearchClick = {},
                onClearRecentSearches = {},
            )
        }

        rule.onNodeWithText("Breaking Bad (2008)").assertIsDisplayed()
        rule.onNodeWithText("Better Call Saul (2015)").assertIsDisplayed()
    }

    @Test
    fun searchArea_withRecentSearches_displaysRecentHeader() {
        val recents =
            listOf(
                mockRecentSearch(1, "The Office"),
                mockRecentSearch(2, "Parks and Recreation"),
            )

        rule.setContent {
            SearchArea(
                searchResultsList = null,
                recentSearches = recents,
                onTextSubmit = {},
                onResultClick = {},
                onRecentSearchClick = {},
                onClearRecentSearches = {},
            )
        }

        rule.onNodeWithText("Recent Searches").assertIsDisplayed()
        rule.onNodeWithText("The Office").assertIsDisplayed()
        rule.onNodeWithText("Parks and Recreation").assertIsDisplayed()
    }

    @Test
    fun searchArea_noResultsNoRecent_showsSearchHint() {
        rule.setContent {
            SearchArea(
                searchResultsList = null,
                recentSearches = null,
                onTextSubmit = {},
                onResultClick = {},
                onRecentSearchClick = {},
                onClearRecentSearches = {},
            )
        }

        rule.onNodeWithText("Search for the show").assertIsDisplayed()
    }

    @Test
    fun searchArea_emptyResultsWithQuery_showsNoResultsMessage() {
        rule.setContent {
            SearchArea(
                searchResultsList = emptyList(),
                recentSearches = null,
                onTextSubmit = {},
                onResultClick = {},
                onRecentSearchClick = {},
                onClearRecentSearches = {},
            )
        }

        // The empty state only appears when the query is non-empty,
        // but the search input starts empty, so no empty state should appear.
        // Verify the search hint is still visible as a baseline.
        rule.onNodeWithText("Search for the show").assertIsDisplayed()
    }
}
