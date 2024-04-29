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

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theupnextapp.R
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.extensions.ReferenceDevices
import com.theupnextapp.ui.components.SectionHeadingText
import com.theupnextapp.ui.destinations.ShowDetailScreenDestination
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalMaterial3WindowSizeClassApi
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
            modifier = Modifier
                .verticalScroll(scrollState)
                .testTag("dashboard_list")
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    yesterdayShowsList.value?.let { list ->
                        AnimatedVisibility(visible = list.isNotEmpty()) {
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
                        AnimatedVisibility(visible = list.isNotEmpty()) {
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
                        AnimatedVisibility(visible = list.isNotEmpty()) {
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

    ReportDrawnWhen {
        !yesterdayShowsList.value.isNullOrEmpty() ||
                !tomorrowShowsList.value.isNullOrEmpty() ||
                !todayShowsList.value.isNullOrEmpty()
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@Composable
fun ShowsRow(
    list: List<ScheduleShow>,
    rowTitle: String,
    modifier: Modifier = Modifier,
    onClick: (item: ScheduleShow) -> Unit
) {
    val state = rememberLazyListState()

    Column(modifier = modifier) {
        SectionHeadingText(text = rowTitle)

        LazyRow(
            state = state,
            modifier = Modifier.padding(8.dp)
        ) {
            items(list) { show ->
                ListPosterCard(
                    itemName = show.name,
                    itemUrl = show.originalImage,
                    modifier = Modifier.testTag("show_item")
                ) {
                    onClick(show)
                }
            }
        }
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@ReferenceDevices
@Composable
private fun ShowsRowPreview(@PreviewParameter(ShowsRowPreviewProvider::class) shows: List<ScheduleShow>) {
    ShowsRow(
        list = shows,
        rowTitle = "Test Shows",
        onClick = {}
    )
}

internal class ShowsRowPreviewProvider : PreviewParameterProvider<List<ScheduleShow>> {
    override val values: Sequence<List<ScheduleShow>>
        get() = sequenceOf(
            MutableList(10) { index ->
                ScheduleShow(
                    id = index,
                    originalImage = "",
                    mediumImage = "",
                    language = "",
                    name = "",
                    officialSite = "",
                    premiered = "",
                    runtime = "",
                    status = "",
                    summary = "",
                    type = "",
                    updated = "",
                    url = ""
                )
            }
        )
}
