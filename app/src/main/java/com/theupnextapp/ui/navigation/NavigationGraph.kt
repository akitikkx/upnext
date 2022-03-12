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

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.dashboard.DashboardScreen
import com.theupnextapp.ui.explore.ExploreScreen
import com.theupnextapp.ui.search.SearchScreen
import com.theupnextapp.ui.showDetail.ShowDetailScreen
import com.theupnextapp.ui.traktAccount.TraktAccountScreen

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun NavigationGraph(navHostController: NavHostController) {
    AnimatedNavHost(
        navController = navHostController,
        startDestination = NavigationScreen.Dashboard.routeName
    ) {

        // Search Screen
        composable(
            route = NavigationScreen.Search.routeName,
            enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Left) },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            }
        ) {
            SearchScreen(navController = navHostController)
        }

        // Dashboard Screen
        composable(
            route = NavigationScreen.Dashboard.routeName,
            enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Left) },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            }
        ) {
            DashboardScreen {
                navHostController.currentBackStackEntry?.savedStateHandle?.set(
                    "show",
                    ShowDetailArg(
                        source = "dashboard",
                        showId = it.id,
                        showTitle = it.name,
                        showImageUrl = it.originalImage,
                        showBackgroundUrl = it.mediumImage
                    )
                )
                navHostController.navigate(NavigationScreen.ShowDetail.routeName)
            }
        }

        // Explore Screen
        composable(
            route = NavigationScreen.Explore.routeName,
            enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Left) },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            }
        ) {
            ExploreScreen(
                onPopularShowClick = {},
                onMostAnticipatedShowClick = {},
                onTrendingShowClick = {}
            )
        }

        // Show Detail Screen
        composable(
            route = NavigationScreen.ShowDetail.routeName,
            enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Left) },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            }
        ) {
            val selectedShow =
                navHostController.previousBackStackEntry?.savedStateHandle?.get<ShowDetailArg>("show")

            ShowDetailScreen(
                showDetailArg = selectedShow,
                onSeasonsClick = { },
                onCastItemClick = {},
                onFavoriteClick = {}
            )
        }

        // Trakt Account Screen
        composable(
            route = NavigationScreen.TraktAccount.routeName,
            enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Left) },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            }
        ) {
            TraktAccountScreen(
                onConnectToTraktClick = {},
                onLogoutClick = {},
                onFavoriteClick = {}
            )
        }
    }
}