package com.theupnextapp.ui.showDetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.theupnextapp.ui.components.PosterImage

@Composable
fun ShowDetailScreen(viewModel: ShowDetailViewModel = hiltViewModel()) {
    val showSummary = viewModel.showSummary.observeAsState()

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
            (showDetailArgs.value?.showBackgroundUrl ?: showDetailArgs.value?.showImageUrl)?.let {
                PosterImage(
                    url = it,
                    height = dimensionResource(id = R.dimen.show_backdrop_height)
                )
            } ?: run {
                showSummary.value?.originalImageUrl?.let {
                    PosterImage(
                        url = it,
                        height = dimensionResource(id = R.dimen.show_backdrop_height)
                    )
                }
            }

            showSummary.value?.name?.let { name ->
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
                            bottom = 4.dp
                        )
                )
            }

            showSummary.value?.status?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp,
                            bottom = 4.dp
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                showSummary.value?.originalImageUrl?.let {
                    PosterImage(
                        url = it,
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.detail_poster_width))
                            .height(
                                dimensionResource(id = R.dimen.show_detail_poster_height)
                            )
                    )
                }
                Column() {
                    showSummary.value?.airDays?.let {
                        TitleAndHeadingText(
                            title = it, heading = stringResource(
                                id = R.string.show_detail_air_days_heading
                            )
                        )
                    }

                }
            }
            showSummary.value?.summary?.let {
                Text(
                    text = it,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }
}

@Composable
fun TitleAndHeadingText(
    title: String,
    heading: String
) {
    Column() {
        Text(text = heading)
        Text(text = title)
    }

}

@Preview
@Composable
fun ShowDetailScreenPreview() {
    ShowDetailScreen()
}