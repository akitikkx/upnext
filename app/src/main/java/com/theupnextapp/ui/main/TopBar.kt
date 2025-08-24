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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.generated.destinations.ShowDetailScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ShowSeasonEpisodesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ShowSeasonsScreenDestination
import com.theupnextapp.R

@OptIn(
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun TopBar(
    navBackStackEntry: NavBackStackEntry?,
    onArrowClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null, // Allow passing a specific title
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if a back arrow should be shown.
    // Show arrow if it's a detail screen deeper than the initial ShowDetailScreen,
    // or if it's ShowDetailScreen itself and not the EmptyDetailScreen.
    // The overrideUpNavigation in MainScreen handles what "back" means.
    val showBackArrow =
        when (currentRoute) {
            ShowDetailScreenDestination.route,
            ShowSeasonsScreenDestination.route,
            ShowSeasonEpisodesScreenDestination.route,
            -> true
            // Do not show back arrow for EmptyDetailScreen or other non-detail-flow
            // screens in this TopBar's context
            else -> false
        }

    // Determine the title to display
    // Prioritize passed 'title' from AppNavigation
    val currentTitle: String =
        title ?: when (currentRoute) {
            ShowSeasonsScreenDestination.route -> stringResource(R.string.title_seasons)
            ShowSeasonEpisodesScreenDestination.route -> stringResource(R.string.title_season_episodes)
            // If currentRoute is ShowDetailScreenDestination but 'title' (dynamicTitle) was
            // null from AppNavigation
            // Fallback for show detail if title is missing
            ShowDetailScreenDestination.route -> stringResource(id = R.string.title_unknown)
            else -> ""
        }

    TopAppBar(
        title = {
            Text(
                text = currentTitle,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (showBackArrow) {
                IconButton(onClick = onArrowClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_navigate_up_description),
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
