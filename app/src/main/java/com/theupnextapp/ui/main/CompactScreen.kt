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

package com.theupnextapp.ui.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.navigate
import com.theupnextapp.ui.destinations.TraktAccountScreenDestination
import com.theupnextapp.ui.navigation.AppNavigation
@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun CompactScreen(
    navController: NavHostController,
    valueState: MutableState<String?>,
    onTraktAuthCompleted: () -> Unit,
) {
    if (!valueState.value.isNullOrEmpty()) {
        navController.navigate(TraktAccountScreenDestination(code = valueState.value).route)
        onTraktAuthCompleted()
    }

    CompactScaffold(
        navHostController = navController,
        topBar = { navBackStackEntry ->
            TopBar(
                navBackStackEntry = navBackStackEntry
            ) {
                navController.navigateUp()
            }
        },
        bottomBar = { destination ->
            BottomBar(
                currentDestination = destination,
                onBottomBarItemClick = {
                    navController.navigate(it) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) {
        AppNavigation(
            navHostController = navController,
            contentPadding = it
        )
    }
}
