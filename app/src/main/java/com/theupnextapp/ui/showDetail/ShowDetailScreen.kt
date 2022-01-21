package com.theupnextapp.ui.showDetail

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
import androidx.compose.material.Card
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.R
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.ui.components.PosterImage
import org.jsoup.Jsoup

@Composable
fun ShowDetailScreen(viewModel: ShowDetailViewModel = hiltViewModel()) {
    val showSummary = viewModel.showSummary.observeAsState()

    val showCast = viewModel.showCast.observeAsState()

    val showDetailArgs = viewModel.showDetailArg.observeAsState()

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
            Buttons()
            showCast.value?.let { ShowCastList(it) }
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
                    .width(dimensionResource(id = R.dimen.compose_detail_poster_width))
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
fun Buttons() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = {},
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.btn_show_detail_remove_from_favorites)
            )
        }
        Button(
            onClick = {},
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.btn_show_detail_seasons)
            )
        }
    }
}

@Composable
fun ShowCastList(list: List<ShowCast>) {
    LazyRow {
        items(list) {
            ShowCastCard(item = it)
        }
    }
}

@Composable
fun ShowCastCard(item: ShowCast) {
    Card {
        item.name?.let { Text(text = it) }
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

@Preview
@Composable
fun ShowDetailScreenPreview() {
    ShowDetailScreen()
}