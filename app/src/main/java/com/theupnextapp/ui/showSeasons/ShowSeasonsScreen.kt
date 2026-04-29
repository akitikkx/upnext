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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.theupnextapp.R
import com.theupnextapp.core.designsystem.ui.components.PosterImage
import com.theupnextapp.core.designsystem.ui.components.ShimmerSeasons
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.navigation.Destinations

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
                    ShowSeasons(
                        showTitle = showDetailArg.showTitle,
                        showBackgroundUrl = showDetailArg.showBackgroundUrl ?: showDetailArg.showImageUrl,
                        list = it,
                        isAuthorizedOnTrakt = showDetailArg.isAuthorizedOnTrakt == true,
                        onShowTitleClick = {
                            navController.navigate(
                                Destinations.ShowDetail(
                                    source = "seasons",
                                    showId = showDetailArg.showId,
                                    showTitle = showDetailArg.showTitle,
                                    showImageUrl = showDetailArg.showImageUrl,
                                    showBackgroundUrl = showDetailArg.showBackgroundUrl,
                                    imdbID = showDetailArg.imdbID,
                                    isAuthorizedOnTrakt = showDetailArg.isAuthorizedOnTrakt,
                                    showTraktId = showDetailArg.showTraktId,
                                ),
                            )
                        },
                        onBackClick = {
                            navController.navigateUp()
                        },
                        onToggleWatched = { season ->
                            viewModel.onToggleSeasonWatched(season)
                        },
                    ) { showSeason ->
                        navController.navigate(
                            Destinations.ShowSeasonEpisodes(
                                showId = showDetailArg.showId?.toInt(),
                                seasonNumber = showSeason.seasonNumber,
                                imdbID = showDetailArg.imdbID,
                                isAuthorizedOnTrakt = showDetailArg.isAuthorizedOnTrakt ?: false,
                                showTraktId = showDetailArg.showTraktId,
                                showTitle = showDetailArg.showTitle,
                                showImageUrl = showSeason.originalImageUrl ?: showDetailArg.showImageUrl,
                                showBackgroundUrl = showDetailArg.showBackgroundUrl,
                            ),
                        )
                    }
                }

                if (isLoading.value == true) {
                    ShimmerSeasons(modifier = Modifier.padding(top = 70.dp))
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@ExperimentalMaterial3Api
@Composable
fun ShowSeasons(
    showTitle: String? = null,
    showBackgroundUrl: String? = null,
    list: List<ShowSeason>,
    isAuthorizedOnTrakt: Boolean = false,
    onShowTitleClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onToggleWatched: (ShowSeason) -> Unit = {},
    onClick: (item: ShowSeason) -> Unit,
) {
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
                    showBackgroundUrl?.let { url ->
                        Image(
                            painter =
                                rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(url)
                                        .crossfade(true)
                                        .build(),
                                ),
                            contentDescription = showTitle ?: "Show background backdrop",
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
                            text = "Seasons",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }
        items(list) { showSeason ->
            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                ShowSeasonCard(
                    item = showSeason,
                    fallbackImageUrl = showBackgroundUrl,
                    isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                    onToggleWatched = { onToggleWatched(showSeason) },
                ) {
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
    fallbackImageUrl: String? = null,
    isAuthorizedOnTrakt: Boolean = false,
    onToggleWatched: () -> Unit = {},
    onClick: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        modifier =
            Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(4.dp),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.defaultMinSize(minHeight = 120.dp),
        ) {
            val imageUrl = item.originalImageUrl ?: fallbackImageUrl
            imageUrl?.let { url ->
                PosterImage(
                    url = url,
                    modifier =
                        Modifier
                            .width(dimensionResource(id = R.dimen.compose_search_poster_width))
                            .height(dimensionResource(id = R.dimen.compose_search_poster_height)),
                    alignment = Alignment.TopCenter,
                )
            }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(8.dp),
            ) {
                if (item.seasonNumber.toString().isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text =
                                stringResource(
                                    R.string.show_detail_season_and_number,
                                    item.seasonNumber.toString(),
                                ),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (isAuthorizedOnTrakt && item.isWatched == true) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Watched",
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
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

                if (isAuthorizedOnTrakt) {
                    Button(
                        onClick = onToggleWatched,
                        modifier = Modifier.padding(top = 8.dp),
                        colors =
                            if (item.isWatched == true) {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            } else {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            },
                    ) {
                        Text(
                            text = if (item.isWatched == true) "Mark Season Unwatched" else "Mark Season Watched",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}
