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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import com.theupnextapp.ui.destinations.Destination
import com.theupnextapp.ui.destinations.TraktAccountScreenDestination
import com.theupnextapp.ui.navigation.AppNavigation

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun MediumScreen(
    navController: NavHostController,
    destination: Destination,
    valueState: MutableState<String?>,
    onTraktAuthCompleted: () -> Unit,
) {
    if (!valueState.value.isNullOrEmpty()) {
        navController.navigate(TraktAccountScreenDestination(code = valueState.value).route)
        onTraktAuthCompleted()
    }

    Row(modifier = Modifier.fillMaxSize()) {
        NavRail(
            currentDestination = destination,
            onNavRailItemClick = {
                navController.navigate(it) {
                    popUpTo(destination) {
                        saveState = true
                        inclusive = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        AppNavigation(
            navHostController = navController,
            contentPadding = PaddingValues()
        )
    }
}
