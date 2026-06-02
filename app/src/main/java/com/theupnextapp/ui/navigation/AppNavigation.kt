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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.ui.episodeDetail.EpisodeDetailScreen
import com.theupnextapp.ui.main.TopBar
import com.theupnextapp.ui.personDetail.PersonDetailScreen
import com.theupnextapp.ui.settings.SettingsScreen
import com.theupnextapp.ui.showDetail.EmptyDetailScreen
import com.theupnextapp.ui.showDetail.ShowDetailScreen
import com.theupnextapp.ui.showSeasonEpisodes.ShowSeasonEpisodesScreen
import com.theupnextapp.ui.showSeasons.ShowSeasonsScreen
import com.theupnextapp.ui.traktAccount.TraktAccountScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Suppress("MagicNumber")
@OptIn(
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
@Composable
fun AppNavigation(
    backStack: SnapshotStateList<Any>,
    onBack: () -> Unit,
) {
    val currentKey = backStack.lastOrNull()

    // Extract title from navigation arguments for detail screens
    val dynamicTitle: String? =
        when (currentKey) {
            is Destinations.ShowDetail -> currentKey.showTitle
            is Destinations.ShowSeasons -> currentKey.showTitle
            is Destinations.PersonDetail -> currentKey.personName
            else -> null
        }

    val showTopBar =
        currentKey != null &&
            currentKey !is Destinations.ShowDetail &&
            currentKey !is Destinations.EmptyDetail &&
            currentKey !is Destinations.ShowSeasons &&
            currentKey !is Destinations.ShowSeasonEpisodes &&
            currentKey !is Destinations.EpisodeDetail &&
            currentKey !is Destinations.PersonDetail

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (showTopBar) {
                TopBar(
                    currentKey = currentKey,
                    onArrowClick = onBack,
                    title = dynamicTitle,
                    onSettingsClick = {
                        if (backStack.lastOrNull() != Destinations.Settings) {
                            backStack.add(Destinations.Settings)
                        }
                    },
                )
            }

            NavDisplay(
                backStack = backStack,
                onBack = onBack,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (slideInHorizontally(
                        initialOffsetX = { 300 },
                        animationSpec = tween(300),
                    ) + fadeIn(animationSpec = tween(300))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { -300 },
                        animationSpec = tween(300),
                    ) + fadeOut(animationSpec = tween(300)))
                },
                popTransitionSpec = {
                    (slideInHorizontally(
                        initialOffsetX = { -300 },
                        animationSpec = tween(300),
                    ) + fadeIn(animationSpec = tween(300))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { 300 },
                        animationSpec = tween(300),
                    ) + fadeOut(animationSpec = tween(300)))
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider<Any> {
                    entry<Destinations.EmptyDetail> {
                        EmptyDetailScreen()
                    }

                    entry<Destinations.ShowDetail> { key ->
                        ShowDetailScreen(
                            showDetailArgs = key.toArg(),
                            onNavigate = { backStack.add(it) },
                            onBack = onBack,
                        )
                    }

                    entry<Destinations.PersonDetail> { key ->
                        PersonDetailScreen(
                            personDetailArg = key.toArg(),
                            onNavigate = { backStack.add(it) },
                            onBack = onBack,
                        )
                    }

                    entry<Destinations.ShowSeasons> { key ->
                        ShowSeasonsScreen(
                            showDetailArg = key.toArg(),
                            onNavigate = { backStack.add(it) },
                            onBack = onBack,
                        )
                    }

                    entry<Destinations.ShowSeasonEpisodes> { key ->
                        ShowSeasonEpisodesScreen(
                            showSeasonEpisodesArg = key.toArg(),
                            onNavigate = { backStack.add(it) },
                            onBack = onBack,
                        )
                    }

                    entry<Destinations.EpisodeDetail> { key ->
                        EpisodeDetailScreen(
                            episodeDetailArg = key.toArg(),
                            onNavigate = { backStack.add(it) },
                            onBack = onBack,
                            onNavigateToShowDetail = { episodeArg ->
                                backStack.add(
                                    Destinations.ShowDetail(
                                        showId = episodeArg.showId?.toString(),
                                        showTitle = episodeArg.showTitle,
                                        showImageUrl = episodeArg.showImageUrl,
                                        showBackgroundUrl = episodeArg.showBackgroundUrl,
                                        imdbID = episodeArg.imdbID,
                                        isAuthorizedOnTrakt = episodeArg.isAuthorizedOnTrakt,
                                        showTraktId = episodeArg.showTraktId,
                                    ),
                                )
                            },
                        )
                    }

                    entry<Destinations.Settings> {
                        SettingsScreen()
                    }

                    entry<Destinations.TraktAccount> { key ->
                        TraktAccountScreen(
                            code = key.code,
                            onNavigate = { backStack.add(it) },
                        )
                    }
                },
            )
        }
    }
}
