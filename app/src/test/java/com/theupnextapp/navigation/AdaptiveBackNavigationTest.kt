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

package com.theupnextapp.navigation

import android.app.Application
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class AdaptiveBackNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun returningFromDetailFlowDrainsScaffoldBackstackLeavingCanNavigateBackFalse() {
        var isDetailActive by mutableStateOf(false)
        var canNavigateBack by mutableStateOf(false)
        var currentPane: Any? = null

        composeTestRule.setContent {
            val navigator = rememberListDetailPaneScaffoldNavigator<Any>()

            LaunchedEffect(isDetailActive) {
                if (isDetailActive) {
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                } else {
                    while (navigator.canNavigateBack()) {
                        navigator.navigateBack(BackNavigationBehavior.PopUntilCurrentDestinationChange)
                    }
                    if (navigator.currentDestination?.pane != ListDetailPaneScaffoldRole.List) {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.List)
                    }
                }
            }

            canNavigateBack = navigator.canNavigateBack()
            currentPane = navigator.currentDestination?.pane
        }

        composeTestRule.waitForIdle()

        // 1. Initial state on Dashboard (List pane)
        assertFalse("Navigator must not have back entries initially", canNavigateBack)
        assertEquals(ListDetailPaneScaffoldRole.List, currentPane)

        // 2. User taps a show: detail flow becomes active
        composeTestRule.runOnUiThread {
            isDetailActive = true
        }
        composeTestRule.waitForIdle()

        assertTrue("Navigator must have back entries when Detail is open", canNavigateBack)
        assertEquals(ListDetailPaneScaffoldRole.Detail, currentPane)

        // 3. User navigates back from detail (detail flow becomes inactive)
        composeTestRule.runOnUiThread {
            isDetailActive = false
        }
        composeTestRule.waitForIdle()

        // 4. Critical verification: navigator backstack is fully drained to List!
        // canNavigateBack must be FALSE, so scaffold BackHandler does not intercept
        // and cannot cause an empty detail / grey screen!
        assertFalse("Navigator must NOT have back entries after returning to Dashboard", canNavigateBack)
        assertEquals(ListDetailPaneScaffoldRole.List, currentPane)
    }
}
