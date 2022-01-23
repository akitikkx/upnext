package com.theupnextapp.ui.showSeasonEpisodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.R
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.components.SectionHeadingText
import org.jsoup.Jsoup

@ExperimentalMaterialApi
@Composable
fun ShowSeasonEpisodesScreen(
    viewModel: ShowSeasonEpisodesViewModel = hiltViewModel()
) {
    val seasonNumber = viewModel.seasonNumber.observeAsState()

    val episodeList = viewModel.episodes.observeAsState()

    Surface {
        seasonNumber.value?.let { season ->
            episodeList.value?.let { episodes ->
                ShowSeasonEpisodes(
                    seasonNumber = season,
                    list = episodes
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun ShowSeasonEpisodes(
    seasonNumber: Int,
    list: List<ShowSeasonEpisode>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeadingText(
            text = stringResource(
                R.string.compose_show_detail_show_season_episodes_title_with_number,
                seasonNumber
            )
        )
        LazyColumn(Modifier.padding(8.dp)) {
            items(list) {
                ShowSeasonEpisodeCard(item = it)
            }
        }

    }
}

@ExperimentalMaterialApi
@Composable
fun ShowSeasonEpisodeCard(
    item: ShowSeasonEpisode
) {
    Card(
        elevation = 4.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            item.originalImageUrl?.let { url ->
                PosterImage(
                    url = url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.show_season_episode_poster_height))
                )
            }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(8.dp)
            ) {
                if (item.number.toString().isNotEmpty() && item.season.toString().isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.compose_show_detail_season_and_episode_number,
                            item.season.toString(),
                            item.number.toString()
                        ),
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.h6
                    )
                }

                item.name?.let {
                    if (item.name.toString().isNotEmpty()) {
                        Text(
                            text = it,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item.summary?.let {
                    if (it.isNotEmpty()) {
                        Text(
                            text = Jsoup.parse(it).text(),
                            modifier = Modifier
                                .padding(
                                    start = 4.dp,
                                    top = 2.dp
                                )
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.caption
                        )
                    }
                }

                if (!item.airstamp.isNullOrEmpty()) {
                    val date = DateUtils.getDisplayDateFromDateStamp(item.airstamp)
                    Text(
                        text = stringResource(
                            R.string.show_detail_air_date_general, date.toString()

                        ),
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}