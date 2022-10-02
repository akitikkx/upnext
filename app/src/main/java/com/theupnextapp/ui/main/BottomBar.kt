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

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.spec.Direction
import com.theupnextapp.R
import com.theupnextapp.ui.destinations.DashboardScreenDestination
import com.theupnextapp.ui.destinations.Destination
import com.theupnextapp.ui.destinations.ExploreScreenDestination
import com.theupnextapp.ui.destinations.SearchScreenDestination
import com.theupnextapp.ui.destinations.TraktAccountScreenDestination

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
enum class BottomBarDestination(
    val direction: Direction,
    val icon: ImageVector,
    @StringRes val label: Int
) {
    SearchScreen(SearchScreenDestination, Icons.Default.Search, R.string.bottom_nav_title_search),
    Dashboard(DashboardScreenDestination, Icons.Default.Home, R.string.bottom_nav_title_dashboard),
    Explore(ExploreScreenDestination, Icons.Filled.Explore, R.string.bottom_nav_title_explore),
    TraktAccount(
        TraktAccountScreenDestination(),
        Icons.Filled.AccountBox,
        R.string.bottom_nav_title_account
    )
}

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun BottomBar(
    currentDestination: Destination,
    onBottomBarItemClick: (Direction) -> Unit
) {
    val bottomBarState = rememberSaveable { mutableStateOf(true) }

    bottomBarState.value = isMainScreen(destination = currentDestination)

    AnimatedVisibility(
        visible = bottomBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        content = {
            BottomAppBar {
                BottomBarDestination.values().forEach { destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = stringResource(id = destination.label)
                            )
                        },
                        label = {
                            Text(stringResource(id = destination.label))
                        },
                        selected = destination.direction.route in currentDestination.baseRoute,
                        onClick = { onBottomBarItemClick(destination.direction) }
                    )
                }
            }
        }
    )
}

/**
 * Determine whether this screen is one of the main screens found on the
 * bottom navigation bar
 *
 * If it is a child screen then the bottom navigation should not be shown
 * and the app bar should have a back arrow displayed
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@ExperimentalMaterial3Api
fun isMainScreen(destination: Destination?): Boolean {
    return when (destination?.route) {
        SearchScreenDestination.route,
        DashboardScreenDestination.route,
        ExploreScreenDestination.route,
        TraktAccountScreenDestination.route -> true
        else -> false
    }
}
