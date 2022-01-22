package com.theupnextapp.ui.showDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.R
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.network.models.trakt.Distribution
import com.theupnextapp.ui.components.PosterImage
import org.jsoup.Jsoup

@Composable
fun ShowDetailScreen(
    viewModel: ShowDetailViewModel = hiltViewModel(),
    onSeasonsClick: () -> Unit,
    onCastItemClick: (item: ShowCast) -> Unit
) {
    val showSummary = viewModel.showSummary.observeAsState()

    val showCast = viewModel.showCast.observeAsState()

    val showDetailArgs = viewModel.showDetailArg.observeAsState()

    val showRating = viewModel.showRating.observeAsState()

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            BackdropAndTitle(
                showDetailArgs = showDetailArgs.value,
                showSummary = showSummary.value
            )
            PosterAndMetadata(showSummary = showSummary.value)
            showSummary.value?.summary?.let {
                Text(
                    text = Jsoup.parse(it).text(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            top = 4.dp,
                            end = 16.dp,
                        ),
                    style = MaterialTheme.typography.body2
                )
            }
            Buttons {
                onSeasonsClick()
            }

            showCast.value?.let {
                ShowCastList(it) { showCastItem ->
                    onCastItemClick(showCastItem)
                }
            }

            showRating.value?.let { ratingData ->
                TraktRatingSummary(ratingData)
            }
        }
    }
}

@Composable
fun BackdropAndTitle(showDetailArgs: ShowDetailArg?, showSummary: ShowDetailSummary?) {
    (showDetailArgs?.showBackgroundUrl ?: showDetailArgs?.showImageUrl)?.let {
        PosterImage(
            url = it,
            height = dimensionResource(id = R.dimen.show_backdrop_height)
        )
    } ?: run {
        showSummary?.originalImageUrl?.let {
            PosterImage(
                url = it,
                height = dimensionResource(id = R.dimen.show_backdrop_height)
            )
        }
    }

    showSummary?.name?.let { name ->
        Text(
            text = name,
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                )
        )
    }

    showSummary?.status?.let { status ->
        Text(
            text = status,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 4.dp,
                    end = 16.dp,
                    bottom = 4.dp
                )
        )
    }
}

@Composable
fun PosterAndMetadata(showSummary: ShowDetailSummary?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        showSummary?.originalImageUrl?.let {
            PosterImage(
                url = it,
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                    .height(
                        dimensionResource(id = R.dimen.compose_show_detail_poster_height)
                    )
            )
        }
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(8.dp)
        ) {
            showSummary?.airDays?.let {
                TitleAndHeadingText(
                    title = it, heading = stringResource(
                        id = R.string.show_detail_air_days_heading
                    )
                )
            }

            showSummary?.genres?.let {
                TitleAndHeadingText(
                    title = it, heading = stringResource(
                        id = R.string.show_detail_genres_heading
                    )
                )
            }

            Text(
                text = stringResource(id = R.string.tv_maze_creative_commons_attribution_text_multiple),
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun Buttons(
    onSeasonsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { onSeasonsClick() },
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.btn_show_detail_seasons)
            )
        }
        Button(
            onClick = {},
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.btn_show_detail_remove_from_favorites)
            )
        }
    }
}

@Composable
fun ShowCastList(
    list: List<ShowCast>,
    onClick: (item: ShowCast) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        items(list) {
            ShowCast(item = it) { showCastItem ->
                onClick(showCastItem)

            }
        }
    }
}

@Composable
fun ShowCast(
    item: ShowCast,
    onClick: (item: ShowCast) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
            .clickable { onClick(item) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item.originalImageUrl?.let {
            PosterImage(
                url = it,
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                    .height(
                        dimensionResource(id = R.dimen.compose_show_detail_poster_height)
                    )
            )
        }
        item.name?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        }
        item.characterName?.let { characterName ->
            Text(
                text = characterName,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Thin,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun TraktRatingSummary(ratingData: TraktShowRating) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        ) {
            Text(
                text = stringResource(
                    id = R.string.compose_show_detail_rating_numerator,
                    ratingData.rating.toString()
                ),
                style = MaterialTheme.typography.h3
            )
            Text(
                text = stringResource(
                    R.string.compose_show_detail_rating_votes,
                    ratingData.votes.toString()
                ),
                style = MaterialTheme.typography.caption
            )
        }
        TraktRatingVisual(ratingData = ratingData)
    }
}

@Composable
fun TraktRatingVisual(ratingData: TraktShowRating) {
    val distributionList = mutableListOf<Distribution>()

    ratingData.distribution?.forEach { (key, value) ->
        val distribution = Distribution(
            score = key,
            value = value
        )
        distributionList.add(distribution)
    }

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
    ) {
        distributionList.asReversed().forEach { distribution ->
            val progressValue =
                (distribution.value.toFloat() / (ratingData.votes?.toFloat() ?: 0f)) * 100.0f

            LinearProgress(
                ratingLevel = distribution.score,
                progress = progressValue,
            )
        }
    }
}

@Composable
fun LinearProgress(
    ratingLevel: String,
    progress: Float
) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .width(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = ratingLevel)
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Preview
@Composable
fun LinearProgressPreview() {
    Row {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "7.0", style = MaterialTheme.typography.h4)
            Text(text = "618 votes", style = MaterialTheme.typography.caption)
        }

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgress(
                ratingLevel = "10",
                progress = 0.43f,
            )
            LinearProgress(
                ratingLevel = "9",
                progress = 0.43f,
            )
            LinearProgress(
                ratingLevel = "8",
                progress = 0.43f,
            )
            LinearProgress(
                ratingLevel = "7",
                progress = 0.43f,
            )
            LinearProgress(
                ratingLevel = "6",
                progress = 0.43f,
            )
            LinearProgress(
                ratingLevel = "5",
                progress = 0.43f,
            )
            LinearProgress(
                ratingLevel = "4",
                progress = 0.43f,
            )
            LinearProgress(
                ratingLevel = "3",
                progress = 0.43f,
            )
            LinearProgress(
                ratingLevel = "2",
                progress = 0.43f,
            )
            LinearProgress(
                ratingLevel = "1",
                progress = 0.43f,
            )
        }
    }
}

@Composable
fun TitleAndHeadingText(
    title: String,
    heading: String
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = heading.uppercase(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.caption
        )
        Text(
            text = title,
            style = MaterialTheme.typography.body2
        )
    }
}