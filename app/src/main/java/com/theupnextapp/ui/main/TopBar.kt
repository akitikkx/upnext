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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import com.theupnextapp.R
import com.theupnextapp.ui.destinations.ShowDetailScreenDestination
import com.theupnextapp.ui.destinations.ShowSeasonEpisodesScreenDestination
import com.theupnextapp.ui.destinations.ShowSeasonsScreenDestination

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@Composable
fun TopBar(
    navBackStackEntry: NavBackStackEntry?,
    onArrowClick: () -> Unit
) {
    val appBarIconState = rememberSaveable { mutableStateOf(true) }

    appBarIconState.value = isChildScreen(navBackStackEntry = navBackStackEntry)

    TopAppBar(
        title = {
            AnimatedVisibility(visible = !appBarIconState.value) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        navigationIcon = {
            NavigationIcon(appBarIconState = appBarIconState) {
                onArrowClick()
            }
        }
    )
}

@Composable
fun NavigationIcon(
    appBarIconState: State<Boolean>,
    onArrowClick: () -> Unit
) {
    AnimatedVisibility(visible = appBarIconState.value) {
        IconButton(onClick = { onArrowClick() }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back arrow")
        }
    }
}

/**
 * Determine whether this screen is not one of the main screens found on the
 * bottom navigation bar
 *
 * If it is a child screen then the bottom navigation should not be shown
 * and the app bar should have a back arrow displayed
 */
@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
fun isChildScreen(navBackStackEntry: NavBackStackEntry?): Boolean {
    return when (navBackStackEntry?.destination?.route) {
        ShowDetailScreenDestination.route,
        ShowSeasonsScreenDestination.route,
        ShowSeasonEpisodesScreenDestination.route -> true
        else -> false
    }
}
