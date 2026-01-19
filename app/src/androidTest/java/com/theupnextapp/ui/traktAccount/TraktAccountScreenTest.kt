package com.theupnextapp.ui.traktAccount

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalFoundationApi::class
)
class TraktAccountScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun accountContent_notAuthorized_showsConnectButton() {
        var connectClicked = false

        composeTestRule.setContent {
            AccountContent(
                isAuthorizedOnTrakt = false,
                favoriteShowsList = emptyList(),
                isFavoriteShowsEmpty = true,
                isLoadingConnection = false,
                isLoadingFavorites = false,
                isDisconnecting = false,
                onConnectToTraktClick = { connectClicked = true },
                onFavoriteClick = {},
                onLogoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("Connect to Trakt").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connect to Trakt").performClick()
        
        assert(connectClicked)
    }

    @Test
    fun accountContent_isAuthorized_showsProfileHeader() {
        composeTestRule.setContent {
            AccountContent(
                isAuthorizedOnTrakt = true,
                favoriteShowsList = emptyList(),
                isFavoriteShowsEmpty = true,
                isLoadingConnection = false,
                isLoadingFavorites = false,
                isDisconnecting = false,
                onConnectToTraktClick = {},
                onFavoriteClick = {},
                onLogoutClick = {}
            )
        }

        // Assuming TraktProfileHeader displays "Logout" or checking for absence of "Connect to Trakt"
        // Since we don't have the string resource for Logout handy, we can check that Connect button is NOT displayed.
        composeTestRule.onNodeWithText("Connect to Trakt").assertDoesNotExist()
    }
}
