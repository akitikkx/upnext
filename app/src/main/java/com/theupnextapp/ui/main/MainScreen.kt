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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.theupnextapp.R
import com.theupnextapp.ui.navigation.NavigationGraph
import com.theupnextapp.ui.navigation.NavigationScreen

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun MainScreen() {
    val navController = rememberAnimatedNavController()

    // the app bar navigation icon will be hidden on certain screens so we need to use
    // a state variable to determine whether it should be visible or not
    // and use AnimatedVisibility to handle its visibility based on this state
    val appBarIconState = rememberSaveable { mutableStateOf(true) }

    // the bottom bar will be hidden on certain screens so we need to use
    // a state variable to determine whether it should be visible or not
    // and use AnimatedVisibility to handle its visibility based on this state
    val bottomBarState = rememberSaveable { mutableStateOf(true) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    appBarIconState.value = isChildScreen(navBackStackEntry = navBackStackEntry)

    bottomBarState.value = !isChildScreen(navBackStackEntry = navBackStackEntry)

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.app_name),
                navController = navController,
                appBarIconState = appBarIconState
            )
        },
        bottomBar = {
            BottomBar(
                navController = navController,
                bottomBarState = bottomBarState
            )
        }
    ) {
        NavigationGraph(navHostController = navController)
    }
}

@Composable
fun TopBar(
    title: String,
    navController: NavHostController,
    appBarIconState: MutableState<Boolean>
) {
    TopAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = appBarIconState.value) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back arrow")
                    }
                }
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun BottomBar(
    navController: NavHostController,
    bottomBarState: MutableState<Boolean>
) {
    val menuItems = listOf(
        NavigationScreen.Search,
        NavigationScreen.Dashboard,
        NavigationScreen.Explore,
        NavigationScreen.TraktAccount,
    )

    // Observe the current backstack entry and be notified when it is changed
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    AnimatedVisibility(
        visible = bottomBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        content = {
            BottomNavigation {
                menuItems.forEach { item ->
                    BottomNavigationItem(
                        label = { item.title?.let { Text(text = it) } },
                        icon = {
                            item.menuIcon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = "Bottom navigation menu icon"
                                )
                            }
                        },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == item.routeName
                        } == true,
                        unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                        onClick = { navController.navigate(item.routeName) }
                    )
                }
            }
        }
    )
}

/**
 * Determine whether this screen is not one of the main screens found on the
 * bottom navigation bar
 *
 * If it is a child screen then the bottom navigation should not be shown
 * and the app bar should have a back arrow displayed
 */
fun isChildScreen(navBackStackEntry: NavBackStackEntry?): Boolean {
    return when (navBackStackEntry?.destination?.route) {
        NavigationScreen.ShowDetail.routeName,
        NavigationScreen.ShowSeasons.routeName,
        NavigationScreen.ShowSeasonEpisodes.routeName -> {
            true
        }
        else -> false
    }
}