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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.theupnextapp.R
import com.theupnextapp.core.designsystem.ui.components.SectionHeadingText
import com.theupnextapp.core.designsystem.ui.components.ShimmerPosterCardRow
import com.theupnextapp.core.designsystem.ui.widgets.ListPosterCard
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.navigation.Destinations
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val COLLAPSED_ITEM_COUNT = 6

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalCoroutinesApi
@ExperimentalMaterial3Api
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel(),
    navController: NavController,
) {
    val popularShowsList: List<TraktPopularShows>
        by viewModel.popularShows.collectAsStateWithLifecycle()
    val trendingShowsList: List<TraktTrendingShows>
        by viewModel.trendingShows.collectAsStateWithLifecycle()
    val mostAnticipatedShowsList: List<TraktMostAnticipated>
        by viewModel.mostAnticipatedShows.collectAsStateWithLifecycle()

    val isOverallLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isPullRefreshing by viewModel.isPullRefreshing.collectAsStateWithLifecycle()
    val exploreSearchQuery by viewModel.exploreSearchQuery.collectAsStateWithLifecycle()

    val isLoadingTrending by viewModel.isLoadingTraktTrending.collectAsStateWithLifecycle()
    val isLoadingPopular by viewModel.isLoadingTraktPopular.collectAsStateWithLifecycle()
    val isLoadingMostAnticipated
        by viewModel.isLoadingTraktMostAnticipated.collectAsStateWithLifecycle()

    val onRefresh = {
        viewModel.checkAndRefreshAllExploreData(forceRefresh = true)
    }

    LaunchedEffect(Unit) {
        if (popularShowsList.isEmpty() && trendingShowsList.isEmpty() && mostAnticipatedShowsList.isEmpty()) {
            if (!isOverallLoading) {
                viewModel.checkAndRefreshAllExploreData(forceRefresh = false)
            }
        }
    }

    var selectedTabIndex by rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(id = R.string.explore_trending_shows_list_title),
        stringResource(id = R.string.explore_popular_shows_list_title),
        stringResource(id = R.string.explore_most_anticipated_shows_list_title)
    )

    PullToRefreshBox(
        isRefreshing = isPullRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().testTag("explore_grid"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Search bar (full width)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedTextField(
                        value = exploreSearchQuery,
                        onValueChange = viewModel::onExploreSearchQueryChange,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        placeholder = { Text("Search shows by title or year...") },
                        singleLine = true,
                        trailingIcon = {
                            if (exploreSearchQuery.isNotEmpty()) {
                                androidx.compose.material3.IconButton(
                                    onClick = { viewModel.onExploreSearchQueryChange("") },
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                }
                            }
                        },
                    )
                }

                // TabRow (full width)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    androidx.compose.material3.ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        edgePadding = 8.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(bottom = 8.dp),
                        divider = {},
                    ) {
                        tabs.forEachIndexed { index, title ->
                            androidx.compose.material3.Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }

                // Overall loading indicator
                if (isOverallLoading && !isPullRefreshing &&
                    popularShowsList.isEmpty() &&
                    trendingShowsList.isEmpty() &&
                    mostAnticipatedShowsList.isEmpty()
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                when (selectedTabIndex) {
                    0 -> {
                        if (isLoadingTrending && trendingShowsList.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) { ShimmerPosterCardRow() }
                        } else {
                            items(
                                items = trendingShowsList,
                                key = { "trending_${it.id ?: it.title}" },
                            ) { show ->
                                ListPosterCard(itemName = show.title, itemUrl = show.originalImageUrl) {
                                    navController.navigate(
                                        Destinations.ShowDetail(
                                            source = "trending",
                                            showId = show.tvMazeID?.toString(),
                                            showTitle = show.title,
                                            showImageUrl = show.originalImageUrl,
                                            showBackgroundUrl = show.mediumImageUrl,
                                            imdbID = show.imdbID,
                                            isAuthorizedOnTrakt = null,
                                            showTraktId = show.traktID,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (isLoadingPopular && popularShowsList.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) { ShimmerPosterCardRow() }
                        } else {
                            items(
                                items = popularShowsList,
                                key = { "popular_${it.id ?: it.title}" },
                            ) { show ->
                                ListPosterCard(itemName = show.title, itemUrl = show.originalImageUrl) {
                                    navController.navigate(
                                        Destinations.ShowDetail(
                                            source = "popular",
                                            showId = show.tvMazeID?.toString(),
                                            showTitle = show.title,
                                            showImageUrl = show.originalImageUrl,
                                            showBackgroundUrl = show.mediumImageUrl,
                                            imdbID = show.imdbID,
                                            isAuthorizedOnTrakt = null,
                                            showTraktId = show.traktID,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        if (isLoadingMostAnticipated && mostAnticipatedShowsList.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) { ShimmerPosterCardRow() }
                        } else {
                            items(
                                items = mostAnticipatedShowsList,
                                key = { "anticipated_${it.id ?: it.title}" },
                            ) { show ->
                                ListPosterCard(itemName = show.title, itemUrl = show.originalImageUrl) {
                                    navController.navigate(
                                        Destinations.ShowDetail(
                                            source = "anticipated",
                                            showId = show.tvMazeID?.toString(),
                                            showTitle = show.title,
                                            showImageUrl = show.originalImageUrl,
                                            showBackgroundUrl = show.mediumImageUrl,
                                            imdbID = show.imdbID,
                                            isAuthorizedOnTrakt = null,
                                            showTraktId = show.traktID,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
