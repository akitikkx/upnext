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


import AppNavigation
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.EmptyDetailScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ShowDetailScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ShowSeasonEpisodesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ShowSeasonsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TraktAccountScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.composedestinations.utils.startDestination
import com.theupnextapp.ui.dashboard.DashboardScreen
import com.theupnextapp.ui.explore.ExploreScreen
import com.theupnextapp.ui.search.SearchScreen
import com.theupnextapp.ui.traktAccount.TraktAccountScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


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
    val scope = rememberCoroutineScope()

    val activity = LocalActivity.current
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }

    val mainNavController = rememberNavController()
    val destinationsNavigatorForDetail: DestinationsNavigator =
        mainNavController.rememberDestinationsNavigator()

    // State to track the currently active top-level list section
    var currentListSection by rememberSaveable { mutableStateOf(NavigationDestination.Dashboard) }

    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRouteForDetailPane = navBackStackEntry?.destination?.route

    val isDetailFlowActive = remember(currentRouteForDetailPane) {
        val isActive = currentRouteForDetailPane?.let { route ->
            route.startsWith(ShowDetailScreenDestination.baseRoute) ||
                    route.startsWith(ShowSeasonsScreenDestination.baseRoute) ||
                    route.startsWith(ShowSeasonEpisodesScreenDestination.baseRoute) ||
                    route.startsWith(TraktAccountScreenDestination.baseRoute)
                    // Explicitly ensure EmptyDetailScreen is NOT considered part of an active detail flow
                    && route != EmptyDetailScreenDestination.route
        } == true
        isActive
    }

    val showDetailScreenArgs = remember(currentRouteForDetailPane, navBackStackEntry) {
        if (currentRouteForDetailPane?.startsWith(ShowDetailScreenDestination.baseRoute) == true) {
            navBackStackEntry?.let { ShowDetailScreenDestination.argsFrom(it) }
        } else {
            null
        }
    }

    val listDetailNavigator = rememberSupportingPaneScaffoldNavigator<ThreePaneScaffoldRole>()

    windowSizeClass?.let { wsc ->
        LaunchedEffect(
            isDetailFlowActive,
            listDetailNavigator.currentDestination,
            wsc.widthSizeClass
        ) {
            val currentPaneRole = listDetailNavigator.currentDestination?.pane

            if (wsc.widthSizeClass == WindowWidthSizeClass.Compact) {
                if (isDetailFlowActive) {
                    if (currentPaneRole != ThreePaneScaffoldRole.Primary) {
                        listDetailNavigator.navigateTo(ThreePaneScaffoldRole.Primary)
                    }
                } else {
                    if (currentPaneRole != ThreePaneScaffoldRole.Secondary) {
                        listDetailNavigator.navigateTo(ThreePaneScaffoldRole.Secondary)
                    }
                }
            } else { // Medium or Expanded
                if (isDetailFlowActive) {
                    if (currentPaneRole != ThreePaneScaffoldRole.Primary) {
                        listDetailNavigator.navigateTo(ThreePaneScaffoldRole.Primary)
                    }
                } else {
                    if (currentPaneRole != ThreePaneScaffoldRole.Secondary) {
                        listDetailNavigator.navigateTo(ThreePaneScaffoldRole.Secondary)
                    }
                }
            }
        }
    }

    // Back handler for adaptive layouts
    BackHandler(enabled = listDetailNavigator.canNavigateBack() || isDetailFlowActive) {
        if (isDetailFlowActive) { // If a detail screen is active in mainNavController
            if (showDetailScreenArgs != null) { // If at the root of detail flow (ShowDetailScreen)
                destinationsNavigatorForDetail.navigate(EmptyDetailScreenDestination) {
                    popUpTo(NavGraphs.root.startDestination) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                // Deeper in detail flow (Seasons, Episodes)
                mainNavController.popBackStack()
            }
            // isDetailFlowActive will become false once mainNavController is on EmptyDetailScreen or similar,
            // which will trigger the LaunchedEffect above to set listDetailNavigator to Secondary.
        } else if (listDetailNavigator.canNavigateBack()) {
            scope.launch {
                // Handles scaffold-level back, e.g., from an extra pane if one is used.
                listDetailNavigator.navigateBack()
            }
        }
    }

    NavigationSuiteScaffold(
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
                        if (isDetailFlowActive || (currentRouteForDetailPane != EmptyDetailScreenDestination.route && currentRouteForDetailPane != null)) {
                            // The second condition ensures reset even if isDetailFlowActive was
                            // somehow false but detail pane isn't empty
                            if (currentRouteForDetailPane != EmptyDetailScreenDestination.route) {
                                destinationsNavigatorForDetail.navigate(EmptyDetailScreenDestination) {
                                    popUpTo(NavGraphs.root.startDestination) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                        // isDetailFlowActive will become false if we navigated to EmptyDetailScreen,
                        // triggering listDetailNavigator to Secondary via the LaunchedEffect.
                    }
                )
            }
        }
    ) {
        NavigableListDetailPaneScaffold(
            modifier = Modifier.fillMaxSize(),
            navigator = listDetailNavigator,
            listPane = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface) // Ensure opaque background
                ) {
                    TopAppBar(
                        title = { Text(stringResource(currentListSection.label)) }
                    )
                    // Content of the current list section
                    // IMPORTANT: Pass destinationsNavigatorForDetail for navigation to detail screens
                    when (currentListSection) {
                        NavigationDestination.Dashboard -> DashboardScreen(navigator = destinationsNavigatorForDetail)
                        NavigationDestination.SearchScreen -> SearchScreen(navigator = destinationsNavigatorForDetail)
                        NavigationDestination.Explore -> ExploreScreen(navigator = destinationsNavigatorForDetail)
                        NavigationDestination.TraktAccount -> TraktAccountScreen(
                            navigator = destinationsNavigatorForDetail,
                            code = null /* Code handled by LaunchedEffect below */
                        )
                    }
                }
            },
            detailPane = {
                windowSizeClass?.let { windowSizeClass ->
                    AppNavigation(
                        navHostController = mainNavController,
                        overrideUpNavigation = {
                            if (showDetailScreenArgs != null) {
                                destinationsNavigatorForDetail.navigate(EmptyDetailScreenDestination) {
                                    popUpTo(NavGraphs.root.startDestination) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                mainNavController.popBackStack()
                            }
                        }
                    )
                }
            }
        )
    }

    // Trakt OAuth handling
    LaunchedEffect(valueState.value) {
        if (!valueState.value.isNullOrEmpty()) {
            val codeArg = valueState.value
            valueState.value = null
            onTraktAuthCompleted()

            currentListSection = NavigationDestination.TraktAccount // Switch list pane

            destinationsNavigatorForDetail.navigate(
                TraktAccountScreenDestination(code = codeArg)
            ) {
                // Clear backstack up to start, then add Empty
                popUpTo(NavGraphs.root.startDestination) { inclusive = true; saveState = true }
                launchSingleTop = true
            }
            // isDetailFlowActive will become true after navigation to TraktAccountScreen,
            // and listDetailNavigator will switch to Primary.
        }
    }
}