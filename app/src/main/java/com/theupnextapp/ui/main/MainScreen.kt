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

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.theupnextapp.R
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.ui.dashboard.DashboardScreen
import com.theupnextapp.ui.explore.ExploreScreen
import com.theupnextapp.ui.navigation.AppNavigation
import com.theupnextapp.ui.search.SearchScreen
import com.theupnextapp.ui.traktAccount.TraktAccountScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalCoroutinesApi::class,
)
@Composable
fun MainScreen(
    valueState: MutableState<String?>,
    onTraktAuthCompleted: () -> Unit,
) {
    // val scope = rememberCoroutineScope() // Removed unused scope

    val activity = LocalActivity.current
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }

    val mainNavController = rememberNavController()

    // State to track the currently active top-level list section
    var currentListSection by rememberSaveable { mutableStateOf(NavigationDestination.Dashboard) }

    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isDetailFlowActive =
        remember(currentDestination) {
            currentDestination?.hasRoute<Destinations.EmptyDetail>() == false &&
                currentDestination.route != null // If null, maybe nothing loaded yet, but usually means not Empty
        }

    val listDetailNavigator = rememberSupportingPaneScaffoldNavigator<ThreePaneScaffoldRole>()

    LaunchedEffect(
        isDetailFlowActive,
        listDetailNavigator.currentDestination,
    ) {
        val currentPaneRole = listDetailNavigator.currentDestination?.pane

        // Common logic for determining target pane based on isDetailFlowActive
        val targetPaneRole =
            if (isDetailFlowActive) {
                ThreePaneScaffoldRole.Primary // Show detail
            } else {
                ThreePaneScaffoldRole.Secondary // Show list
            }

        if (currentPaneRole != targetPaneRole) {
            listDetailNavigator.navigateTo(targetPaneRole)
        }
    }

    // Back handler for adaptive layouts and top-level navigation
    BackHandler(
        enabled = isDetailFlowActive || currentListSection != NavigationDestination.Dashboard,
    ) {
        if (isDetailFlowActive) { // If a detail screen is active in mainNavController
            val previousEntry = mainNavController.previousBackStackEntry
            if (previousEntry != null && previousEntry.destination.hasRoute<Destinations.EmptyDetail>() == false) {
                mainNavController.popBackStack()
            } else {
                // If popping sends us to EmptyDetail (or there is no previous), we navigate to EmptyDetail explicitly to clear
                mainNavController.navigate(Destinations.EmptyDetail) {
                    popUpTo(Destinations.EmptyDetail) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else if (currentListSection != NavigationDestination.Dashboard) {
            // If we are at a top-level section other than Dashboard, go back to Dashboard
            currentListSection = NavigationDestination.Dashboard
        }
    }

    NavigationSuiteScaffold(
        modifier = Modifier.testTag("navigation_suite_scaffold"),
        navigationSuiteItems = {
            NavigationDestination.entries.forEach { item ->
                val isSelected = item == currentListSection
                item(
                    icon = { Icon(item.icon, contentDescription = stringResource(item.label)) },
                    label = { Text(stringResource(item.label)) },
                    selected = isSelected,
                    onClick = {
                        // This will change the content of listPane
                        currentListSection = item

                        // When a new top-level section is clicked,
                        // ensure the detail pane is reset to its empty state
                        // if it's currently showing actual details.
                        if (isDetailFlowActive) {
                            mainNavController.navigate(Destinations.EmptyDetail) {
                                popUpTo(Destinations.EmptyDetail) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        // isDetailFlowActive will become false if we navigated to EmptyDetailScreen,
                        // triggering listDetailNavigator to Secondary via the LaunchedEffect.
                    },
                )
            }
        },
    ) {
        NavigableListDetailPaneScaffold(
            modifier = Modifier.fillMaxSize(),
            navigator = listDetailNavigator,
            listPane = {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface), // Ensure opaque background
                ) {
                    TopAppBar(
                        title = { Text(stringResource(currentListSection.label)) },
                        actions = {
                            if (currentListSection == NavigationDestination.TraktAccount) {
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        mainNavController.navigate(Destinations.Settings)
                                    },
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Filled.Settings,
                                        contentDescription = stringResource(R.string.title_settings),
                                    )
                                }
                            }
                        },
                    )
                    // Content of the current list section
                    // IMPORTANT: Pass destinationsNavigatorForDetail for navigation to detail screens
                    when (currentListSection) {
                        NavigationDestination.Dashboard -> DashboardScreen(navController = mainNavController)
                        NavigationDestination.SearchScreen -> SearchScreen(navController = mainNavController)
                        NavigationDestination.Explore -> ExploreScreen(navController = mainNavController)
                        NavigationDestination.TraktAccount ->
                            TraktAccountScreen(
                                navController = mainNavController,
                                code = null, // Code handled by LaunchedEffect below
                            )
                    }
                }
            },
            detailPane = {
                windowSizeClass?.let { windowSizeClass ->
                    AppNavigation(
                        navHostController = mainNavController,
                        overrideUpNavigation = {
                            mainNavController.navigateUp()
                        },
                    )
                }
            },
        )
    }

    // Trakt OAuth handling
    LaunchedEffect(valueState.value) {
        if (!valueState.value.isNullOrEmpty()) {
            val codeArg = valueState.value
            valueState.value = null
            onTraktAuthCompleted()

            currentListSection = NavigationDestination.TraktAccount // Switch list pane

            mainNavController.navigate(Destinations.TraktAccount(code = codeArg)) {
                // Clear backstack logic if needed, or just push
                launchSingleTop = true
            }
            // isDetailFlowActive will become true after navigation to TraktAccountScreen,
            // and listDetailNavigator will switch to Primary.
        }
    }
}
