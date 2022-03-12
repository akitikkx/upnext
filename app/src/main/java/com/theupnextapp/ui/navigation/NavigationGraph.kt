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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.theupnextapp.ui.dashboard.DashboardScreen
import com.theupnextapp.ui.explore.ExploreScreen
import com.theupnextapp.ui.search.SearchScreen
import com.theupnextapp.ui.traktAccount.TraktAccountScreen

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun NavigationGraph(navHostController: NavHostController) {
    NavHost(
        navController = navHostController,
        startDestination = BottomNavigationScreen.Dashboard.routeName
    ) {

        composable(route = BottomNavigationScreen.Search.routeName) {
            SearchScreen(navController = navHostController)
        }

        composable(route = BottomNavigationScreen.Dashboard.routeName) {
            DashboardScreen() {

            }
        }

        composable(route = BottomNavigationScreen.Explore.routeName) {
            ExploreScreen(
                onPopularShowClick = {},
                onMostAnticipatedShowClick = {},
                onTrendingShowClick = {}
            )
        }

        composable(route = BottomNavigationScreen.TraktAccount.routeName) {
            TraktAccountScreen(
                onConnectToTraktClick = {},
                onLogoutClick = {},
                onFavoriteClick = {}
            )
        }
    }
}