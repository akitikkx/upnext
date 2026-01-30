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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theupnextapp.R
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.components.SectionHeadingText

@ExperimentalMaterial3Api
@Composable
fun ShowSeasonsScreen(
    viewModel: ShowSeasonsViewModel = hiltViewModel(),
    showDetailArg: ShowDetailArg,
    navController: NavController,
) {
    LaunchedEffect(showDetailArg) {
        viewModel.setSelectedShow(showDetailArg)
    }

    val showSeasonsList = viewModel.showSeasons.observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Box(modifier = Modifier.fillMaxSize()) {
                showSeasonsList.value?.let {
                    ShowSeasons(list = it) { showSeason ->
                        navController.navigate(
                            Destinations.ShowSeasonEpisodes(
                                showId = showDetailArg.showId?.toInt(),
                                seasonNumber = showSeason.seasonNumber,
                                imdbID = showDetailArg.imdbID,
                                isAuthorizedOnTrakt = showDetailArg.isAuthorizedOnTrakt,
                                showTraktId = showDetailArg.showTraktId,
                            ),
                        )
                    }
                }

                if (isLoading.value == true) {
                    LinearProgressIndicator(
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ShowSeasons(
    list: List<ShowSeason>,
    onClick: (item: ShowSeason) -> Unit,
) {
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

@ExperimentalMaterial3Api
@Composable
fun ShowSeasonCard(
    item: ShowSeason,
    onClick: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            item.originalImageUrl?.let { url ->
                PosterImage(
                    url = url,
                    modifier =
                        Modifier
                            .width(dimensionResource(id = R.dimen.compose_search_poster_width))
                            .height(dimensionResource(id = R.dimen.compose_search_poster_height)),
                )
            }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(8.dp),
            ) {
                if (item.seasonNumber.toString().isNotEmpty()) {
                    Text(
                        text =
                            stringResource(
                                R.string.show_detail_season_and_number,
                                item.seasonNumber.toString(),
                            ),
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                val premiereDate = item.premiereDate
                if (!premiereDate.isNullOrEmpty()) {
                    Text(
                        text =
                            stringResource(
                                R.string.show_detail_season_premiere_date,
                                premiereDate,
                            ),
                        modifier = Modifier.padding(2.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                val endDate = item.endDate
                if (!endDate.isNullOrEmpty()) {
                    Text(
                        text =
                            stringResource(
                                R.string.show_detail_season_end_date,
                                endDate,
                            ),
                        modifier = Modifier.padding(2.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (!item.originalImageUrl.isNullOrEmpty()) {
                    Text(
                        text =
                            stringResource(
                                R.string.tv_maze_creative_commons_attribution_text_single,
                            ),
                        modifier =
                            Modifier.padding(
                                start = 2.dp,
                                top = 4.dp,
                                bottom = 2.dp,
                                end = 4.dp,
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
