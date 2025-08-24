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

package com.theupnextapp.ui.showSeasonEpisodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.theupnextapp.R
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.components.SectionHeadingText
import org.jsoup.Jsoup

@ExperimentalMaterial3Api
@Destination<RootGraph>(navArgs = ShowSeasonEpisodesArg::class)
@Composable
fun ShowSeasonEpisodesScreen(
    viewModel: ShowSeasonEpisodesViewModel = hiltViewModel(),
    showSeasonEpisodesArg: ShowSeasonEpisodesArg?,
) {
    viewModel.selectedSeason(showSeasonEpisodesArg)

    val seasonNumber = viewModel.seasonNumber.observeAsState()

    val episodeList = viewModel.episodes.observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()

    Surface {
        Column {
            Box(modifier = Modifier.fillMaxSize()) {
                seasonNumber.value?.let { season ->
                    episodeList.value?.let { episodes ->
                        ShowSeasonEpisodes(
                            seasonNumber = season,
                            list = episodes,
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
@Destination<RootGraph>
@Composable
fun ShowSeasonEpisodes(
    seasonNumber: Int,
    list: List<ShowSeasonEpisode>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeadingText(
            text =
                stringResource(
                    R.string.show_detail_show_season_episodes_title_with_number,
                    seasonNumber,
                ),
        )
        LazyColumn(Modifier.padding(8.dp)) {
            items(list) {
                ShowSeasonEpisodeCard(item = it)
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ShowSeasonEpisodeCard(item: ShowSeasonEpisode) {
    Card(
        shape = MaterialTheme.shapes.large,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            item.originalImageUrl?.let { url ->
                PosterImage(
                    url = url,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.show_season_episode_poster_height)),
                )
            }

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(8.dp),
            ) {
                if (item.number.toString().isNotEmpty() && item.season.toString().isNotEmpty()) {
                    Text(
                        text =
                            stringResource(
                                R.string.show_detail_season_and_episode_number,
                                item.season.toString(),
                                item.number.toString(),
                            ),
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                item.name?.let {
                    if (item.name.toString().isNotEmpty()) {
                        Text(
                            text = it,
                            modifier =
                                Modifier
                                    .padding(start = 4.dp)
                                    .fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                item.summary?.let {
                    if (it.isNotEmpty()) {
                        Text(
                            text = Jsoup.parse(it).text(),
                            modifier =
                                Modifier
                                    .padding(
                                        start = 4.dp,
                                        top = 4.dp,
                                        bottom = 2.dp,
                                    )
                                    .fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                if (!item.airstamp.isNullOrEmpty()) {
                    val date = DateUtils.getDisplayDateFromDateStamp(item.airstamp)
                    Text(
                        text =
                            stringResource(
                                R.string.show_detail_air_date_general,
                                date.toString(),
                            ),
                        modifier =
                            Modifier
                                .padding(4.dp)
                                .fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
