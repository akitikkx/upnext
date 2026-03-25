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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.theupnextapp.core.designsystem.ui.components.PosterImage
import com.theupnextapp.core.designsystem.ui.modifiers.bounceClick
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.navigation.Destinations
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs =
        listOf(
            "Trending",
            "Popular",
            "Anticipated",
        )

    PullToRefreshBox(
        isRefreshing = isPullRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .testTag("explore_grid"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val currentList =
                    when (selectedTabIndex) {
                        0 -> trendingShowsList
                        1 -> popularShowsList
                        2 -> mostAnticipatedShowsList
                        else -> emptyList<Any>()
                    }

                if (isOverallLoading && !isPullRefreshing && currentList.isEmpty()) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                if (currentList.isNotEmpty()) {
                    val heroShow = currentList.first()
                    item {
                        FeaturedShowHero(
                            item = heroShow,
                            onClick = { navigateToShowDetails(heroShow, tabs[selectedTabIndex].lowercase(), navController) },
                        )
                    }
                }

                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        edgePadding = 8.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        divider = {},
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title.uppercase(),
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    )
                                },
                            )
                        }
                    }
                }

                if (currentList.size > 1) {
                    item {
                        val bentoItems = currentList.drop(1).take(5)
                        BentoBoxGrid(
                            items = bentoItems,
                            source = tabs[selectedTabIndex].lowercase(),
                            navController = navController,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoBoxGrid(
    items: List<Any>,
    source: String,
    navController: NavController,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (items.size >= 2) {
            // Row 1: Two portrait squares
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                BentoCard(
                    item = items[0],
                    onClick = { navigateToShowDetails(items[0], source, navController) },
                    modifier = Modifier.weight(1f).height(220.dp),
                )
                BentoCard(
                    item = items[1],
                    onClick = { navigateToShowDetails(items[1], source, navController) },
                    modifier = Modifier.weight(1f).height(220.dp),
                )
            }
        } else if (items.size == 1) {
            BentoCard(
                item = items[0],
                onClick = { navigateToShowDetails(items[0], source, navController) },
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )
        }

        if (items.size >= 3) {
            // Row 2: One wide rectangle
            BentoCard(
                item = items[2],
                onClick = { navigateToShowDetails(items[2], source, navController) },
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
        }

        if (items.size >= 5) {
            // Row 3: Two portrait squares
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                BentoCard(
                    item = items[3],
                    onClick = { navigateToShowDetails(items[3], source, navController) },
                    modifier = Modifier.weight(1f).height(220.dp),
                )
                BentoCard(
                    item = items[4],
                    onClick = { navigateToShowDetails(items[4], source, navController) },
                    modifier = Modifier.weight(1f).height(220.dp),
                )
            }
        } else if (items.size == 4) {
            BentoCard(
                item = items[3],
                onClick = { navigateToShowDetails(items[3], source, navController) },
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BentoCard(
    item: Any,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val title =
        when (item) {
            is TraktTrendingShows -> item.title
            is TraktPopularShows -> item.title
            is TraktMostAnticipated -> item.title
            else -> null
        }
    val imageUrl =
        when (item) {
            is TraktTrendingShows -> item.originalImageUrl
            is TraktPopularShows -> item.originalImageUrl
            is TraktMostAnticipated -> item.originalImageUrl
            else -> null
        }

    Card(
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        modifier = modifier.bounceClick(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrEmpty()) {
                PosterImage(
                    url = imageUrl,
                    modifier = Modifier.fillMaxSize(),
                    height = Dp.Unspecified,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 150f,
                            ),
                        ),
            )
            Text(
                text = title ?: "",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedShowHero(
    item: Any,
    onClick: () -> Unit,
) {
    val title =
        when (item) {
            is TraktTrendingShows -> item.title
            is TraktPopularShows -> item.title
            is TraktMostAnticipated -> item.title
            else -> null
        }
    val imageUrl =
        when (item) {
            is TraktTrendingShows -> item.originalImageUrl
            is TraktPopularShows -> item.originalImageUrl
            is TraktMostAnticipated -> item.originalImageUrl
            else -> null
        }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .bounceClick(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrEmpty()) {
                PosterImage(
                    url = imageUrl,
                    modifier = Modifier.fillMaxSize(),
                    height = Dp.Unspecified,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)),
                                startY = 200f,
                            ),
                        ),
            )
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
            ) {
                Text(
                    text = "TOP PICK",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Text(
                    text = title ?: "Unknown",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

private fun navigateToShowDetails(
    item: Any,
    source: String,
    navController: NavController,
) {
    val match =
        when (item) {
            is TraktTrendingShows ->
                listOf(
                    item.title,
                    item.originalImageUrl,
                    item.mediumImageUrl,
                    item.tvMazeID,
                    item.imdbID,
                    item.traktID,
                )
            is TraktPopularShows -> listOf(item.title, item.originalImageUrl, item.mediumImageUrl, item.tvMazeID, item.imdbID, item.traktID)
            is TraktMostAnticipated ->
                listOf(
                    item.title,
                    item.originalImageUrl,
                    item.mediumImageUrl,
                    item.tvMazeID,
                    item.imdbID,
                    item.traktID,
                )
            else -> listOf(null, null, null, null, null, null)
        }

    val title = match[0] as? String
    val originalImageUrl = match[1] as? String
    val mediumImageUrl = match[2] as? String
    val tvMazeID = match[3] as? Int
    val imdbID = match[4] as? String
    val traktID = match[5] as? Int

    navController.navigate(
        Destinations.ShowDetail(
            source = source,
            showId = (tvMazeID as? Int)?.toString(),
            showTitle = title as? String,
            showImageUrl = originalImageUrl as? String,
            showBackgroundUrl = mediumImageUrl as? String,
            imdbID = imdbID as? String,
            isAuthorizedOnTrakt = null,
            showTraktId = traktID as? Int,
        ),
    )
}
