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

package com.theupnextapp.ui.showSeasons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.R
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.components.SectionHeadingText

@ExperimentalMaterialApi
@Composable
fun ShowSeasonsScreen(
    viewModel: ShowSeasonsViewModel = hiltViewModel(),
    showDetailArg: ShowDetailArg? = null,
    onSeasonClick: (item: ShowSeason) -> Unit
) {
    viewModel.selectedShow(showDetailArg)

    val showSeasonsList = viewModel.showSeasons.observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Box(modifier = Modifier.fillMaxSize()) {
                showSeasonsList.value?.let {
                    ShowSeasons(list = it) { showSeason ->
                        onSeasonClick(showSeason)
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

@ExperimentalMaterialApi
@Composable
fun ShowSeasons(list: List<ShowSeason>, onClick: (item: ShowSeason) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeadingText(text = stringResource(id = R.string.title_seasons))
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            items(list) { showSeason ->
                ShowSeasonCard(item = showSeason) {
                    onClick(showSeason)
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun ShowSeasonCard(
    item: ShowSeason,
    onClick: () -> Unit
) {
    Card(
        elevation = 4.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            item.originalImageUrl?.let { url ->
                PosterImage(
                    url = url,
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.compose_search_poster_width))
                        .height(dimensionResource(id = R.dimen.compose_search_poster_height))
                )
            }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(8.dp)
            ) {
                if (item.seasonNumber.toString().isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.show_detail_season_and_number,
                            item.seasonNumber.toString()
                        ),
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.h6
                    )
                }

                if (!item.premiereDate.isNullOrEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.show_detail_season_premiere_date,
                            item.premiereDate
                        ),
                        modifier = Modifier.padding(2.dp),
                        style = MaterialTheme.typography.caption
                    )
                }

                if (!item.endDate.isNullOrEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.show_detail_season_end_date,
                            item.endDate
                        ),
                        modifier = Modifier.padding(2.dp),
                        style = MaterialTheme.typography.caption
                    )
                }

                if (!item.originalImageUrl.isNullOrEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.tv_maze_creative_commons_attribution_text_single
                        ),
                        modifier = Modifier.padding(
                            start = 2.dp,
                            top = 4.dp,
                            bottom = 2.dp,
                            end = 4.dp
                        ),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}