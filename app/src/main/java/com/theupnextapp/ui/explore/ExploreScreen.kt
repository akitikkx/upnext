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

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theupnextapp.R
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.ui.components.SectionHeadingText
import com.theupnextapp.ui.destinations.ShowDetailScreenDestination
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalMaterial3Api
@Destination
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val popularShowsList = viewModel.popularShows.observeAsState()

    val trendingShowsList = viewModel.trendingShows.observeAsState()

    val mostAnticipatedShowsList = viewModel.mostAnticipatedShows.observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()

    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                Column(modifier = Modifier.padding(top = 8.dp)) {
                    trendingShowsList.value?.let { list ->
                        if (list.isNotEmpty())
                            TrendingShowsRow(
                                list = list,
                                rowTitle = stringResource(id = R.string.explore_trending_shows_list_title)
                            ) {
                                navigator.navigate(
                                    ShowDetailScreenDestination(
                                        source = "trending",
                                        showId = it.tvMazeID.toString(),
                                        showTitle = it.title,
                                        showImageUrl = it.originalImageUrl,
                                        showBackgroundUrl = it.mediumImageUrl
                                    )
                                )
                            }
                    }

                    popularShowsList.value?.let { list ->
                        if (list.isNotEmpty())
                            PopularShowsRow(
                                list = list,
                                rowTitle = stringResource(id = R.string.explore_popular_shows_list_title)
                            ) {
                                navigator.navigate(
                                    ShowDetailScreenDestination(
                                        source = "popular",
                                        showId = it.tvMazeID.toString(),
                                        showTitle = it.title,
                                        showImageUrl = it.originalImageUrl,
                                        showBackgroundUrl = it.mediumImageUrl
                                    )
                                )
                            }
                    }

                    mostAnticipatedShowsList.value?.let { list ->
                        if (list.isNotEmpty())
                            MostAnticipatedShowsRow(
                                list = list,
                                rowTitle = stringResource(id = R.string.explore_most_anticipated_shows_list_title)
                            ) {
                                navigator.navigate(
                                    ShowDetailScreenDestination(
                                        source = "most_anticipated",
                                        showId = it.tvMazeID.toString(),
                                        showTitle = it.title,
                                        showImageUrl = it.originalImageUrl,
                                        showBackgroundUrl = it.mediumImageUrl
                                    )
                                )
                            }
                    }
                }

                if (isLoading.value == true) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun TrendingShowsRow(
    list: List<TraktTrendingShows>,
    rowTitle: String,
    onClick: (item: TraktTrendingShows) -> Unit
) {
    Column {
        SectionHeadingText(text = rowTitle)

        LazyRow(modifier = Modifier.padding(8.dp)) {
            items(list) { show ->
                ListPosterCard(
                    itemName = show.title,
                    itemUrl = show.originalImageUrl
                ) {
                    onClick(show)
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun PopularShowsRow(
    list: List<TraktPopularShows>,
    rowTitle: String,
    onClick: (item: TraktPopularShows) -> Unit
) {
    Column {
        SectionHeadingText(text = rowTitle)

        LazyRow(modifier = Modifier.padding(8.dp)) {
            items(list) { show ->
                ListPosterCard(
                    itemName = show.title,
                    itemUrl = show.originalImageUrl
                ) {
                    onClick(show)
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun MostAnticipatedShowsRow(
    list: List<TraktMostAnticipated>,
    rowTitle: String,
    onClick: (item: TraktMostAnticipated) -> Unit
) {
    Column {
        SectionHeadingText(text = rowTitle)

        LazyRow(modifier = Modifier.padding(8.dp)) {
            items(list) { show ->
                ListPosterCard(
                    itemName = show.title,
                    itemUrl = show.originalImageUrl
                ) {
                    onClick(show)
                }
            }
        }
    }
}
