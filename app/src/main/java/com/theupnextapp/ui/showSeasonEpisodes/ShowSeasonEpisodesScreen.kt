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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.theupnextapp.R
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.core.designsystem.ui.components.PosterImage
import com.theupnextapp.core.designsystem.ui.components.ShimmerSeasonEpisodes
import com.theupnextapp.core.designsystem.ui.modifiers.bounceClick
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.navigation.Destinations
import org.jsoup.Jsoup

@ExperimentalMaterial3Api
@Composable
fun ShowSeasonEpisodesScreen(
    viewModel: ShowSeasonEpisodesViewModel = hiltViewModel(),
    showSeasonEpisodesArg: ShowSeasonEpisodesArg,
    navController: NavController,
) {
    LaunchedEffect(showSeasonEpisodesArg) {
        viewModel.selectedSeason(showSeasonEpisodesArg)
    }

    val seasonNumber = viewModel.seasonNumber.observeAsState()

    val episodeList = viewModel.episodes.observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()

    val isAuthorizedOnTrakt = viewModel.isAuthorizedOnTrakt.collectAsStateWithLifecycle()

    Surface {
        Column {
            Box(modifier = Modifier.fillMaxSize()) {
                seasonNumber.value?.let { season ->
                    episodeList.value?.let { episodes ->
                        ShowSeasonEpisodes(
                            showTitle = showSeasonEpisodesArg.showTitle,
                            showImageUrl = showSeasonEpisodesArg.showImageUrl,
                            seasonNumber = season,
                            list = episodes,
                            onToggleWatched = { episode ->
                                viewModel.onToggleWatched(episode)
                            },
                            onMarkSeasonWatched = {
                                viewModel.markSeasonAsWatched()
                            },
                            onMarkSeasonUnwatched = {
                                viewModel.markSeasonAsUnwatched()
                            },
                            isAuthorizedOnTrakt = isAuthorizedOnTrakt.value,
                            onEpisodeClick = { episode ->
                                val showTraktId = showSeasonEpisodesArg.showTraktId
                                val season = episode.season
                                val number = episode.number
                                if (showTraktId != null && season != null && number != null) {
                                    navController.navigate(
                                        Destinations.EpisodeDetail(
                                            showTraktId = showTraktId,
                                            seasonNumber = season,
                                            episodeNumber = number,
                                            showTitle = showSeasonEpisodesArg.showTitle,
                                            showId = showSeasonEpisodesArg.showId,
                                            imdbID = showSeasonEpisodesArg.imdbID,
                                            isAuthorizedOnTrakt = showSeasonEpisodesArg.isAuthorizedOnTrakt,
                                            showImageUrl = showSeasonEpisodesArg.showImageUrl,
                                            showBackgroundUrl = showSeasonEpisodesArg.showBackgroundUrl,
                                            episodeImageUrl = episode.originalImageUrl, // Inject specific episode image
                                        ),
                                    )
                                }
                            },
                            onShowTitleClick = {
                                navController.navigate(
                                    Destinations.ShowDetail(
                                        source = "season_episodes",
                                        showId = showSeasonEpisodesArg.showId?.toString(),
                                        showTitle = showSeasonEpisodesArg.showTitle,
                                        showImageUrl = showSeasonEpisodesArg.showImageUrl,
                                        showBackgroundUrl = showSeasonEpisodesArg.showBackgroundUrl,
                                        imdbID = showSeasonEpisodesArg.imdbID,
                                        isAuthorizedOnTrakt = showSeasonEpisodesArg.isAuthorizedOnTrakt,
                                        showTraktId = showSeasonEpisodesArg.showTraktId,
                                    ),
                                )
                            },
                            onBackClick = {
                                navController.navigateUp()
                            },
                        )
                    }
                }

                if (isLoading.value == true) {
                    ShimmerSeasonEpisodes(modifier = Modifier.padding(top = 70.dp))
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@ExperimentalMaterial3Api
@Composable
fun ShowSeasonEpisodes(
    showTitle: String? = null,
    showImageUrl: String? = null,
    seasonNumber: Int,
    list: List<ShowSeasonEpisode>,
    onToggleWatched: (ShowSeasonEpisode) -> Unit = {},
    onEpisodeClick: (ShowSeasonEpisode) -> Unit = {},
    onMarkSeasonWatched: () -> Unit = {},
    onMarkSeasonUnwatched: () -> Unit = {},
    isAuthorizedOnTrakt: Boolean = false,
    onShowTitleClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter =
                        fadeIn(animationSpec = tween(700)) +
                            slideInVertically(
                                initialOffsetY = { -50 },
                                animationSpec = tween(700),
                            ),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                    ) {
                        showImageUrl?.let { url ->
                            Image(
                                painter =
                                    rememberAsyncImagePainter(
                                        ImageRequest.Builder(LocalContext.current)
                                            .data(url)
                                            .crossfade(true)
                                            .build(),
                                    ),
                                contentDescription = showTitle,
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.TopCenter,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors =
                                                listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                                    MaterialTheme.colorScheme.background,
                                                ),
                                        ),
                                    ),
                        )
                        IconButton(
                            onClick = onBackClick,
                            modifier =
                                Modifier
                                    .padding(
                                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                                        start = 16.dp,
                                    )
                                    .background(color = Color.Black.copy(alpha = 0.5f), shape = CircleShape),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }
                        Column(
                            modifier =
                                Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                        ) {
                            showTitle?.let { title ->
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.minimumInteractiveComponentSize().clickable { onShowTitleClick() },
                                )
                            }
                            Text(
                                text = "Season $seasonNumber",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }

            if (isAuthorizedOnTrakt && list.isNotEmpty()) {
                item {
                    val allWatched = list.all { it.isWatched }
                    val buttonText = if (allWatched) "Mark Season Unwatched" else "Mark Season Watched"
                    val buttonIcon = if (allWatched) Icons.Outlined.CheckCircle else Icons.Filled.CheckCircle

                    FilledTonalButton(
                        onClick = {
                            if (allWatched) {
                                onMarkSeasonUnwatched()
                            } else {
                                onMarkSeasonWatched()
                            }
                        },
                        modifier =
                            Modifier
                                .widthIn(max = 600.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Icon(
                            imageVector = buttonIcon,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(text = buttonText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(list) { episode ->
                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                    ShowSeasonEpisodeCard(
                        item = episode,
                        onToggleWatched = onToggleWatched,
                        onEpisodeClick = onEpisodeClick,
                        isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ShowSeasonEpisodeCard(
    item: ShowSeasonEpisode,
    onToggleWatched: (ShowSeasonEpisode) -> Unit = {},
    onEpisodeClick: (ShowSeasonEpisode) -> Unit = {},
    isAuthorizedOnTrakt: Boolean = false,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = Color.Transparent,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp,
            ),
        modifier =
            Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(8.dp)
                .bounceClick { onEpisodeClick(item) },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.number.toString().isNotEmpty()) {
                        Text(
                            text = "Episode ${item.number}",
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    if (isAuthorizedOnTrakt) {
                        IconButton(onClick = { onToggleWatched(item) }) {
                            Icon(
                                imageVector =
                                    if (item.isWatched) {
                                        Icons.Filled.CheckCircle
                                    } else {
                                        Icons.Outlined.CheckCircle
                                    },
                                contentDescription =
                                    if (item.isWatched) {
                                        stringResource(R.string.episode_mark_unwatched)
                                    } else {
                                        stringResource(R.string.episode_mark_watched)
                                    },
                                tint =
                                    if (item.isWatched) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }
                }

                item.name?.let {
                    if (item.name.toString().isNotEmpty()) {
                        Text(
                            text = it,
                            modifier =
                                Modifier
                                    .padding(start = 4.dp)
                                    .fillMaxWidth(),
                            style = MaterialTheme.typography.titleMedium,
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

                val airstamp = item.airstamp
                if (!airstamp.isNullOrEmpty()) {
                    val date = DateUtils.getDisplayDate(airstamp)
                    if (date != null) {
                        Text(
                            text =
                                stringResource(
                                    R.string.show_detail_air_date_general,
                                    date,
                                ),
                            modifier =
                                Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                if (isAuthorizedOnTrakt && item.isWatched) {
                    Text(
                        text = stringResource(R.string.episode_watched),
                        modifier =
                            Modifier
                                .padding(4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
