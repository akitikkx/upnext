/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ShowDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theupnextapp.R
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.ui.components.SectionHeadingText
import com.theupnextapp.ui.components.ShimmerPosterCardRow
import com.theupnextapp.ui.widgets.ListPosterCard
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalCoroutinesApi
@ExperimentalMaterial3Api
@Destination<RootGraph>
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
) {
    val popularShowsList: List<TraktPopularShows>
        by viewModel.popularShows.collectAsStateWithLifecycle()
    val trendingShowsList: List<TraktTrendingShows>
        by viewModel.trendingShows.collectAsStateWithLifecycle()
    val mostAnticipatedShowsList: List<TraktMostAnticipated>
        by viewModel.mostAnticipatedShows.collectAsStateWithLifecycle()

    val isOverallLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isPullRefreshing by viewModel.isPullRefreshing.collectAsStateWithLifecycle()

    val isLoadingTrending by viewModel.isLoadingTraktTrending.collectAsStateWithLifecycle()
    val isLoadingPopular by viewModel.isLoadingTraktPopular.collectAsStateWithLifecycle()
    val isLoadingMostAnticipated
        by viewModel.isLoadingTraktMostAnticipated.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    val onRefresh = {
        viewModel.checkAndRefreshAllExploreData(forceRefresh = true)
    }

    LaunchedEffect(Unit) {
        if (popularShowsList.isEmpty() && trendingShowsList.isEmpty() && mostAnticipatedShowsList.isEmpty()) {
            // Check if not already loading overall to prevent duplicate calls if init already started something
            // or if a configuration change happened while loading.
            if (!isOverallLoading) {
                viewModel.checkAndRefreshAllExploreData(forceRefresh = false)
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isPullRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                if (isOverallLoading && !isPullRefreshing && popularShowsList.isEmpty() && trendingShowsList.isEmpty() && mostAnticipatedShowsList.isEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Column(
                    modifier =
                    Modifier.padding(
                        top = 8.dp,
                        bottom = 16.dp,
                    ),
                ) {
                    // Trending Shows Section
                    if (isLoadingTrending && trendingShowsList.isEmpty()) {
                        ShimmerPosterCardRow()
                    } else if (trendingShowsList.isNotEmpty()) {
                        TrendingShowsRow(
                            list = trendingShowsList,
                            rowTitle = stringResource(id = R.string.explore_trending_shows_list_title),
                        ) { traktShow ->
                            navigator.navigate(
                                ShowDetailScreenDestination(
                                    source = "trending",
                                    showId = traktShow.tvMazeID.toString(),
                                    showTitle = traktShow.title,
                                    showImageUrl = traktShow.originalImageUrl,
                                    showBackgroundUrl = traktShow.mediumImageUrl,
                                ),
                            )
                        }
                    }

                    // Popular Shows Section
                    if (isLoadingPopular && popularShowsList.isEmpty()) {
                        ShimmerPosterCardRow()
                    } else if (popularShowsList.isNotEmpty()) {
                        PopularShowsRow(
                            list = popularShowsList,
                            rowTitle = stringResource(id = R.string.explore_popular_shows_list_title),
                        ) { traktShow ->
                            navigator.navigate(
                                ShowDetailScreenDestination(
                                    source = "popular",
                                    showId = traktShow.tvMazeID.toString(),
                                    showTitle = traktShow.title,
                                    showImageUrl = traktShow.originalImageUrl,
                                    showBackgroundUrl = traktShow.mediumImageUrl,
                                ),
                            )
                        }
                    }

                    // Most Anticipated Shows Section
                    if (isLoadingMostAnticipated && mostAnticipatedShowsList.isEmpty()) {
                        ShimmerPosterCardRow()
                    } else if (mostAnticipatedShowsList.isNotEmpty()) {
                        MostAnticipatedShowsRow(
                            list = mostAnticipatedShowsList,
                            rowTitle = stringResource(id = R.string.explore_most_anticipated_shows_list_title),
                        ) { traktShow ->
                            navigator.navigate(
                                ShowDetailScreenDestination(
                                    source = "most_anticipated",
                                    showId = traktShow.tvMazeID.toString(),
                                    showTitle = traktShow.title,
                                    showImageUrl = traktShow.originalImageUrl,
                                    showBackgroundUrl = traktShow.mediumImageUrl,
                                ),
                            )
                        }
                    } //
                }
            }
        }
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingShowsRow(
    list: List<TraktTrendingShows>,
    rowTitle: String,
    onClick: (item: TraktTrendingShows) -> Unit,
) {
    Column {
        SectionHeadingText(text = rowTitle)

        LazyRow(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            items(items = list, key = { it.id ?: it.title ?: "" }) { show ->
                ListPosterCard(
                    itemName = show.title,
                    itemUrl = show.originalImageUrl,
                ) {
                    onClick(show)
                }
            }
        }
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularShowsRow(
    list: List<TraktPopularShows>,
    rowTitle: String,
    onClick: (item: TraktPopularShows) -> Unit,
) {
    Column {
        SectionHeadingText(text = rowTitle)

        LazyRow(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            items(items = list, key = { it.id ?: it.title ?: "" }) { show ->
                ListPosterCard(
                    itemName = show.title,
                    itemUrl = show.originalImageUrl,
                ) {
                    onClick(show)
                }
            }
        }
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MostAnticipatedShowsRow(
    list: List<TraktMostAnticipated>,
    rowTitle: String,
    onClick: (item: TraktMostAnticipated) -> Unit,
) {
    Column {
        SectionHeadingText(text = rowTitle)

        LazyRow(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            items(items = list, key = { it.id ?: it.title ?: "" }) { show ->
                ListPosterCard(
                    itemName = show.title,
                    itemUrl = show.originalImageUrl,
                ) {
                    onClick(show)
                }
            }
        }
    }
}
