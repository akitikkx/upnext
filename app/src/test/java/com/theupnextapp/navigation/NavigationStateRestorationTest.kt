/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.navigation

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class NavigationStateRestorationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `navigation backstack state is preserved across configuration changes`() {
        val restorationTester = StateRestorationTester(composeTestRule)
        var backStackReference: SnapshotStateList<Any>? = null

        restorationTester.setContent {
            val backStack = rememberSaveable(saver = DestinationsNavSaver) {
                mutableStateListOf<Any>(Destinations.EmptyDetail)
            }
            backStackReference = backStack
        }

        // 1. Initial state: only EmptyDetail
        assertEquals(1, backStackReference?.size)
        assertEquals(Destinations.EmptyDetail, backStackReference?.lastOrNull())

        // 2. User selects a show (e.g. Saturday Night Live)
        val selectedShow = Destinations.ShowDetail(
            source = "dashboard",
            showId = "123",
            showTitle = "Saturday Night Live",
            showImageUrl = "http://example.com/snl.jpg",
            showBackgroundUrl = "http://example.com/banner.jpg",
            imdbID = "tt0072562",
            isAuthorizedOnTrakt = true,
            showTraktId = 999,
        )
        composeTestRule.runOnUiThread {
            backStackReference?.add(selectedShow)
        }

        assertEquals(2, backStackReference?.size)
        assertEquals(selectedShow, backStackReference?.lastOrNull())

        // 3. Emulate configuration change / rotation / activity recreation
        restorationTester.emulateSavedInstanceStateRestore()

        // 4. Verify that state was NOT lost: selected show is still active
        assertEquals(2, backStackReference?.size)
        assertEquals(selectedShow, backStackReference?.lastOrNull())

        // 5. Deep navigation: user drills down to seasons and episodes
        val seasonNav = Destinations.ShowSeasons(
            source = "show_detail",
            showId = "123",
            showTitle = "Saturday Night Live",
            showImageUrl = "http://example.com/snl.jpg",
            showBackgroundUrl = "http://example.com/banner.jpg",
            imdbID = "tt0072562",
            isAuthorizedOnTrakt = true,
            showTraktId = 999,
        )
        composeTestRule.runOnUiThread {
            backStackReference?.add(seasonNav)
        }

        assertEquals(3, backStackReference?.size)
        assertEquals(seasonNav, backStackReference?.lastOrNull())

        // 6. Another configuration change (e.g. rotate back to portrait)
        restorationTester.emulateSavedInstanceStateRestore()

        assertEquals(3, backStackReference?.size)
        assertEquals(selectedShow, backStackReference?.get(1))
        assertEquals(seasonNav, backStackReference?.lastOrNull())

        // 7. User pops back
        composeTestRule.runOnUiThread {
            backStackReference?.removeAt(backStackReference!!.lastIndex)
        }
        assertEquals(2, backStackReference?.size)
        assertEquals(selectedShow, backStackReference?.lastOrNull())

        // 8. User pops back to root (dashboard)
        composeTestRule.runOnUiThread {
            backStackReference?.removeAt(backStackReference!!.lastIndex)
        }
        assertEquals(1, backStackReference?.size)
        assertEquals(Destinations.EmptyDetail, backStackReference?.lastOrNull())

        // 9. Recreate when at root: backstack remains clean
        restorationTester.emulateSavedInstanceStateRestore()
        assertEquals(1, backStackReference?.size)
        assertEquals(Destinations.EmptyDetail, backStackReference?.lastOrNull())
    }
}
