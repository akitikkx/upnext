package com.theupnextapp

import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.theupnextapp.ui.main.MainScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalMaterial3WindowSizeClassApi
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainScreenTest {
    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var workWorManagerRule = WorkManagerRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltTestRule.inject()
    }

    @Test
    fun compactDevice_verifyBottomNavigationIsPresent() {
        composeTestRule.activity.setContent {
            val dataString: MutableState<String?> = rememberSaveable { mutableStateOf("") }

            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(200.dp, 600.dp))
            ) {
                MainScreen(
                    valueState = dataString,
                    onTraktAuthCompleted = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("navigation_suite_scaffold").assertExists()
    }

    @Test
    fun mediumDevice_verifyNavigationRailIsPresent() {
        composeTestRule.activity.setContent {
            val dataString: MutableState<String?> = rememberSaveable { mutableStateOf("") }

            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(600.dp, 480.dp))
            ) {
                MainScreen(
                    valueState = dataString,
                    onTraktAuthCompleted = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("navigation_suite_scaffold").assertExists()
    }

    @Test
    fun expandedDevice_verifyNavigationDrawerIsPresent() {
        composeTestRule.activity.setContent {
            val dataString: MutableState<String?> = rememberSaveable { mutableStateOf("") }

            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(840.dp, 480.dp))
            ) {
                MainScreen(
                    valueState = dataString,
                    onTraktAuthCompleted = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("navigation_suite_scaffold").assertExists()
    }
}