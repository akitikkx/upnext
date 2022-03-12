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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationScreen (
    val routeName: String,
    val title: String? = null,
    val menuIcon: ImageVector? = null
) {

    object Search: NavigationScreen(
        routeName = "search",
        title = "Search",
        menuIcon = Icons.Default.Search
    )

    object Dashboard: NavigationScreen(
        routeName = "dashboard",
        title = "Dashboard",
        menuIcon = Icons.Default.Home
    )

    object Explore: NavigationScreen(
        routeName = "explore",
        title = "Explore",
        menuIcon = Icons.Filled.Explore
    )

    object ShowDetail: NavigationScreen(
        routeName = "show_detail"
    )

    object ShowSeasons: NavigationScreen(
        routeName = "show_seasons"
    )

    object TraktAccount: NavigationScreen(
        routeName = "trakt_account",
        title = "Account",
        menuIcon = Icons.Filled.AccountBox
    )
}