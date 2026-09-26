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

package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
class EmptyDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(qualifiers = "w800dp-h1280dp-port")
    fun `portrait tablet does not display empty detail placeholder text`() {
        composeTestRule.setContent {
            EmptyDetailScreen()
        }

        // In portrait mode, EmptyDetailScreen must remain blank so it never covers the list or confuses users
        composeTestRule.onNodeWithText("Select a show from the list to see its details.").assertDoesNotExist()
        composeTestRule.onNodeWithText("Your dashboard, search results, and explore sections will appear on the left.").assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1280dp-h800dp-land")
    fun `landscape tablet displays empty detail placeholder text`() {
        composeTestRule.setContent {
            EmptyDetailScreen()
        }

        // In landscape multi-pane, the placeholder prompt is displayed in the secondary detail pane
        composeTestRule.onNodeWithText("Select a show from the list to see its details.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your dashboard, search results, and explore sections will appear on the left.").assertIsDisplayed()
    }
}
