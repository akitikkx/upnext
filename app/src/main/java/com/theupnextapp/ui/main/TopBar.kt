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
import androidx.navigation.NavDestination.Companion.hasRoute
import com.theupnextapp.R
import com.theupnextapp.navigation.Destinations

@OptIn(
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun TopBar(
    title: String,
    onArrowClick: () -> Unit,
    showBackArrow: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
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
    val destination = navBackStackEntry?.destination

    // Determine if a back arrow should be shown.
    val showBackArrow =
        when {
            destination?.hasRoute<Destinations.Settings>() == true -> true
            destination?.hasRoute<Destinations.ShowDetail>() == true -> true
            destination?.hasRoute<Destinations.ShowSeasons>() == true -> true
            destination?.hasRoute<Destinations.ShowSeasonEpisodes>() == true -> true
            else -> false
        }

    // Determine the title to display
    // Prioritize passed 'title' from AppNavigation
    val currentTitle: String =
        title ?: when {
            destination?.hasRoute<Destinations.Settings>() == true -> stringResource(R.string.title_settings)
            destination?.hasRoute<Destinations.ShowSeasons>() == true -> stringResource(R.string.title_seasons)
            destination?.hasRoute<Destinations.ShowSeasonEpisodes>() == true -> stringResource(R.string.title_season_episodes)
            destination?.hasRoute<Destinations.ShowDetail>() == true -> stringResource(id = R.string.title_unknown)
            else -> ""
        }

    TopBar(
        title = currentTitle,
        onArrowClick = onArrowClick,
        showBackArrow = showBackArrow,
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    )
}
