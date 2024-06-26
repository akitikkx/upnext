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

package com.theupnextapp.ui.showDetail

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ShowSeasonsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theupnextapp.R
import com.theupnextapp.common.utils.getWindowSizeClass
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.network.models.trakt.Distribution
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.components.SectionHeadingText
import org.jsoup.Jsoup

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@Destination<RootGraph>(navArgs = ShowDetailArg::class)
@Composable
fun ShowDetailScreen(
    viewModel: ShowDetailViewModel = hiltViewModel(),
    showDetailArgs: ShowDetailArg?,
    navigator: DestinationsNavigator
) {

    viewModel.selectedShow(showDetailArgs)

    val showSummary = viewModel.showSummary.observeAsState()

    val showCast = viewModel.showCast.observeAsState()

    val showRating = viewModel.showRating.observeAsState()

    val showPreviousEpisode = viewModel.showPreviousEpisode.observeAsState()

    val showNextEpisode = viewModel.showNextEpisode.observeAsState()

    val isAuthorizedOnTrakt = viewModel.isAuthorizedOnTrakt.observeAsState()

    val isFavorite = viewModel.isFavoriteShow.observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column {
            Box(modifier = Modifier.fillMaxSize()) {
                DetailArea(
                    showSummary = showSummary.value,
                    showDetailArgs = showDetailArgs,
                    isAuthorizedOnTrakt = isAuthorizedOnTrakt.value,
                    isFavorite = isFavorite.value,
                    showCast = showCast.value,
                    showNextEpisode = showNextEpisode.value,
                    showPreviousEpisode = showPreviousEpisode.value,
                    showRating = showRating.value,
                    onSeasonsClick = {
                        navigator.navigate(
                            ShowSeasonsScreenDestination(
                                ShowDetailArg(
                                    showId = showDetailArgs?.showId,
                                    showTitle = showDetailArgs?.showTitle,
                                    showImageUrl = showDetailArgs?.showImageUrl,
                                    showBackgroundUrl = showDetailArgs?.showBackgroundUrl,
                                    imdbID = showDetailArgs?.imdbID,
                                    isAuthorizedOnTrakt = isAuthorizedOnTrakt.value
                                )
                            )
                        )
                    },
                    onCastItemClick = {
                        // TODO trigger bottom sheet
                    },
                    onFavoriteClick = { viewModel.onAddRemoveFavoriteClick() }
                )

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

@ExperimentalMaterial3WindowSizeClassApi
@Composable
fun DetailArea(
    showSummary: ShowDetailSummary?,
    showDetailArgs: ShowDetailArg?,
    isAuthorizedOnTrakt: Boolean?,
    isFavorite: Boolean?,
    showCast: List<ShowCast>?,
    showRating: TraktShowRating?,
    onSeasonsClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCastItemClick: (item: ShowCast) -> Unit,
    showNextEpisode: ShowNextEpisode?,
    showPreviousEpisode: ShowPreviousEpisode?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        BackdropAndTitle(
            showDetailArgs = showDetailArgs,
            showSummary = showSummary
        )

        SynopsisArea(
            showSummary = showSummary,
            widthSizeClass = getWindowSizeClass()?.widthSizeClass
        )

        if (showSummary?.id != -1) {
            ShowDetailButtons(
                isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                isFavorite = isFavorite,
                onSeasonsClick = { onSeasonsClick() },
                onFavoriteClick = { onFavoriteClick() }
            )
        }

        showCast?.let {
            if (it.isNotEmpty()) {
                ShowCastList(it) { showCastItem ->
                    onCastItemClick(showCastItem)
                }
            }
        }

        if (!showNextEpisode?.nextEpisodeSummary.isNullOrEmpty()) {
            showNextEpisode?.let {
                NextEpisode(showNextEpisode = it)
            }
        }

        if (!showPreviousEpisode?.previousEpisodeSummary.isNullOrEmpty()) {
            showPreviousEpisode?.let {
                PreviousEpisode(showPreviousEpisode = it)
            }
        }

        showRating?.let { ratingData ->
            if (ratingData.votes != 0) {
                TraktRatingSummary(ratingData)
            }
        }
    }
}

@Composable
fun ShowCastList(
    list: List<ShowCast>,
    onClick: (item: ShowCast) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_cast_list))

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
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        }

        item.characterName?.let { characterName ->
            Text(
                text = characterName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Thin,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun PreviousEpisode(showPreviousEpisode: ShowPreviousEpisode) {
    Column {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_previous_episode_heading))

        Text(
            text = stringResource(
                R.string.show_detail_episode_season_info,
                showPreviousEpisode.previousEpisodeSeason.toString(),
                showPreviousEpisode.previousEpisodeNumber.toString(),
                showPreviousEpisode.previousEpisodeName.toString()
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 2.dp,
                    end = 16.dp,
                    bottom = 4.dp
                ),
            style = MaterialTheme.typography.labelMedium
        )

        showPreviousEpisode.previousEpisodeSummary?.let {
            EpisodeSummary(summary = it)
        }
    }
}

@Composable
fun NextEpisode(showNextEpisode: ShowNextEpisode) {
    Column {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_next_episode_heading))

        Text(
            text = stringResource(
                R.string.show_detail_episode_season_info,
                showNextEpisode.nextEpisodeSeason.toString(),
                showNextEpisode.nextEpisodeNumber.toString(),
                showNextEpisode.nextEpisodeName.toString()
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 2.dp,
                    end = 16.dp,
                    bottom = 4.dp
                ),
            style = MaterialTheme.typography.labelMedium
        )

        showNextEpisode.nextEpisodeSummary?.let {
            EpisodeSummary(summary = it)
        }
    }
}

@Composable
fun EpisodeSummary(summary: String) {
    Text(
        text = Jsoup.parse(summary).text(),
        modifier = Modifier
            .padding(
                start = 16.dp,
                top = 4.dp,
                end = 16.dp,
                bottom = 8.dp
            )
            .fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun TraktRatingSummary(ratingData: TraktShowRating) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_ratings_heading))

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
                        id = R.string.show_detail_rating_numerator,
                        ratingData.rating.toString()
                    ),
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = stringResource(
                        R.string.show_detail_rating_votes,
                        ratingData.votes.toString()
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            TraktRatingVisual(ratingData = ratingData)
        }
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
                (distribution.value.toFloat() / (ratingData.votes?.toFloat() ?: 0f))

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
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
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
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "7.0", style = MaterialTheme.typography.headlineSmall)
            Text(text = "618 votes", style = MaterialTheme.typography.labelMedium)
        }

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            LinearProgress(
                ratingLevel = "10",
                progress = 0.63f,
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
fun HeadingAndItemText(
    item: String,
    heading: String
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = heading.uppercase(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = item,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

internal class ShowDetailSummaryPreviewProvider : PreviewParameterProvider<ShowDetailSummary> {
    override val values: Sequence<ShowDetailSummary>
        get() = sequenceOf(
            ShowDetailSummary(
                airDays = "Sunday",
                averageRating = "",
                id = (0..1000).random(),
                imdbID = "tt0903747",
                genres = "Drama, Crime, Thriller",
                language = "English",
                mediumImageUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/0/2400.jpg",
                name = "Breaking Bad",
                originalImageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/0/2400.jpg",
                summary = "<p><b>Breaking Bad</b> follows protagonist Walter White, a chemistry " +
                        "teacher who lives in New Mexico with his wife and teenage son who has " +
                        "cerebral palsy. White is diagnosed with Stage III cancer and given a " +
                        "prognosis of two years left to live. With a new sense of fearlessness " +
                        "based on his medical prognosis, and a desire to secure his family's " +
                        "financial security, White chooses to enter a dangerous world of drugs " +
                        "and crime and ascends to power in this world. The series explores how a " +
                        "fatal diagnosis such as White's releases a typical man from the daily " +
                        "concerns and constraints of normal society and follows his " +
                        "transformation from mild family man to a kingpin of the drug trade.</p>",
                time = "",
                status = "Ended",
                previousEpisodeHref = "",
                nextEpisodeHref = "",
                nextEpisodeLinkedId = 1,
                previousEpisodeLinkedId = 2,
            )
        )
}
