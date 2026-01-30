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

package com.theupnextapp.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.ui.main.TopBar
import com.theupnextapp.ui.settings.SettingsScreen
import com.theupnextapp.ui.showDetail.EmptyDetailScreen
import com.theupnextapp.ui.showDetail.ShowDetailScreen
import com.theupnextapp.ui.showSeasonEpisodes.ShowSeasonEpisodesScreen
import com.theupnextapp.ui.showSeasons.ShowSeasonsScreen
import com.theupnextapp.ui.traktAccount.TraktAccountScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
)
@Composable
fun AppNavigation(
    navHostController: NavHostController,
    overrideUpNavigation: (() -> Unit)? = null,
) {
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Extract title from navigation arguments for detail screens
    val currentEntry = navBackStackEntry
    val dynamicTitle: String? =
        when {
            currentEntry?.destination?.hasRoute<Destinations.ShowDetail>() == true -> {
                try {
                    currentEntry.toRoute<Destinations.ShowDetail>().showTitle
                } catch (e: Exception) {
                    null
                }
            }
            currentEntry?.destination?.hasRoute<Destinations.ShowSeasons>() == true -> {
                try {
                    currentEntry.toRoute<Destinations.ShowSeasons>().showTitle
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopBar(
                navBackStackEntry = navBackStackEntry,
                onArrowClick = {
                    if (overrideUpNavigation != null) {
                        overrideUpNavigation()
                    } else {
                        navHostController.navigateUp()
                    }
                },
                title = dynamicTitle,
            )

            NavHost(
                navController = navHostController,
                startDestination = Destinations.EmptyDetail,
                modifier = Modifier.weight(1f),
            ) {
                composable<Destinations.EmptyDetail> {
                    EmptyDetailScreen()
                }

                composable<Destinations.ShowDetail> { backStackEntry ->
                    val args = backStackEntry.toRoute<Destinations.ShowDetail>()
                    ShowDetailScreen(
                        showDetailArgs = args.toArg(),
                        navController = navHostController,
                    )
                }

                composable<Destinations.ShowSeasons> { backStackEntry ->
                    val args = backStackEntry.toRoute<Destinations.ShowSeasons>()
                    ShowSeasonsScreen(
                        showDetailArg = args.toArg(),
                        navController = navHostController,
                    )
                }

                composable<Destinations.ShowSeasonEpisodes> { backStackEntry ->
                    val args = backStackEntry.toRoute<Destinations.ShowSeasonEpisodes>()
                    ShowSeasonEpisodesScreen(
                        showSeasonEpisodesArg = args.toArg(),
                        navController = navHostController,
                    )
                }

                composable<Destinations.Settings> {
                    SettingsScreen(navController = navHostController)
                }

                composable<Destinations.TraktAccount> { backStackEntry ->
                    val args = backStackEntry.toRoute<Destinations.TraktAccount>()
                    TraktAccountScreen(
                        code = args.code,
                        navController = navHostController,
                    )
                }
            }
        }
    }
}
