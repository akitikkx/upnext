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

package com.theupnextapp.ui.dashboard

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
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theupnextapp.R
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.ui.components.SectionHeadingText
import com.theupnextapp.ui.destinations.ShowDetailScreenDestination
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalMaterial3Api
@RootNavGraph(start = true)
@Destination
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val yesterdayShowsList = viewModel.yesterdayShowsList.observeAsState()

    val todayShowsList = viewModel.todayShowsList.observeAsState()

    val tomorrowShowsList = viewModel.tomorrowShowsList.observeAsState()

    val scrollState = rememberScrollState()

    val isLoading = viewModel.isLoading.observeAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                Column(modifier = Modifier.padding(top = 8.dp)) {
                    yesterdayShowsList.value?.let { list ->
                        if (list.isNotEmpty()) {
                            ShowsRow(
                                list = list,
                                rowTitle = stringResource(id = R.string.title_yesterday_shows)
                            ) {
                                navigator.navigate(
                                    ShowDetailScreenDestination(
                                        source = "dashboard",
                                        showId = it.id.toString(),
                                        showTitle = it.name,
                                        showImageUrl = it.originalImage,
                                        showBackgroundUrl = it.mediumImage
                                    )
                                )
                            }
                        }
                    }

                    todayShowsList.value?.let { list ->
                        if (list.isNotEmpty()) {
                            ShowsRow(
                                list = list,
                                rowTitle = stringResource(id = R.string.title_today_shows)
                            ) {
                                navigator.navigate(
                                    ShowDetailScreenDestination(
                                        source = "dashboard",
                                        showId = it.id.toString(),
                                        showTitle = it.name,
                                        showImageUrl = it.originalImage,
                                        showBackgroundUrl = it.mediumImage
                                    )
                                )
                            }
                        }
                    }

                    tomorrowShowsList.value?.let { list ->
                        if (list.isNotEmpty()) {
                            ShowsRow(
                                list = list,
                                rowTitle = stringResource(id = R.string.title_tomorrow_shows)
                            ) {
                                navigator.navigate(
                                    ShowDetailScreenDestination(
                                        source = "dashboard",
                                        showId = it.id.toString(),
                                        showTitle = it.name,
                                        showImageUrl = it.originalImage,
                                        showBackgroundUrl = it.mediumImage
                                    )
                                )
                            }
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
fun ShowsRow(
    list: List<ScheduleShow>,
    rowTitle: String,
    onClick: (item: ScheduleShow) -> Unit
) {
    Column {
        SectionHeadingText(text = rowTitle)

        LazyRow(modifier = Modifier.padding(8.dp)) {
            items(list, key = { show -> show.id }) { show ->
                ListPosterCard(
                    itemName = show.name,
                    itemUrl = show.originalImage
                ) {
                    onClick(show)
                }
            }
        }
    }
}
