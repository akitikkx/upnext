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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.theupnextapp.R
import com.theupnextapp.ui.destinations.DashboardScreenDestination
import com.theupnextapp.ui.destinations.Destination
import com.theupnextapp.ui.destinations.ExploreScreenDestination
import com.theupnextapp.ui.destinations.SearchScreenDestination
import com.theupnextapp.ui.destinations.TraktAccountScreenDestination

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    val icon: ImageVector,
    @StringRes val label: Int
) {
    SearchScreen(SearchScreenDestination, Icons.Default.Search, R.string.bottom_nav_title_search),
    Dashboard(DashboardScreenDestination, Icons.Default.Home, R.string.bottom_nav_title_dashboard),
    Explore(ExploreScreenDestination, Icons.Filled.Explore, R.string.bottom_nav_title_explore),
    TraktAccount(
        TraktAccountScreenDestination,
        Icons.Filled.AccountBox,
        R.string.bottom_nav_title_account
    )
}

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun BottomBar(
    currentDestination: Destination,
    onBottomBarItemClick: (Direction) -> Unit
) {
    BottomNavigation {
        BottomBarDestination.values().forEach { destination ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(id = destination.label)
                    )
                },
                label = {
                    Text(stringResource(id = destination.label))
                },
                selected = currentDestination == destination.direction,
                onClick = { onBottomBarItemClick(destination.direction) }
            )
        }
    }

}