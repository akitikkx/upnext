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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.theupnextapp.R
import com.theupnextapp.core.designsystem.ui.components.PosterImage
import com.theupnextapp.core.designsystem.ui.components.SectionHeadingText
import com.theupnextapp.core.designsystem.ui.getWindowSizeClass
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.TraktCast
import com.theupnextapp.domain.TraktRelatedShows
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.domain.TraktShowStats
import com.theupnextapp.navigation.Destinations
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

// UI Constants
@Suppress("MagicNumber")
private val RatingStarColor = Color(0xFFFFC107) // Gold/Amber

@Suppress("MagicNumber")
private val RatingStarUnselected = Color(0xFFBDBDBD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ShowDetailViewModel = hiltViewModel(),
    showDetailArgs: ShowDetailArg,
    navController: NavController,
) {
    LaunchedEffect(showDetailArgs) {
        viewModel.selectedShow(showDetailArgs)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isWatchlist by viewModel.isWatchlistShow.collectAsStateWithLifecycle()
    val isWatchlistLoading by viewModel.isWatchlistLoading.collectAsStateWithLifecycle()
    val showRating by viewModel.showRating.collectAsStateWithLifecycle()
    val showStats by viewModel.showStats.collectAsStateWithLifecycle()
    val isAuthorizedOnTrakt by viewModel.isAuthorizedOnTrakt.collectAsStateWithLifecycle()
    val navigateToSeasons by viewModel.navigateToSeasons.collectAsStateWithLifecycle()
    val isLoadingGlobal by viewModel.isLoading.collectAsStateWithLifecycle()
    val traktId by viewModel.traktId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(navigateToSeasons) {
        if (navigateToSeasons) {
            navController.navigate(
                Destinations.ShowSeasons(
                    showId = showDetailArgs.showId,
                    showTitle = showDetailArgs.showTitle,
                    showImageUrl = showDetailArgs.showImageUrl,
                    showBackgroundUrl = uiState.showSummary?.originalImageUrl ?: showDetailArgs.showBackgroundUrl,
                    imdbID = showDetailArgs.imdbID,
                    isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                    showTraktId = traktId,
                ),
            )
            viewModel.onSeasonsNavigationComplete()
        }
    }

    LaunchedEffect(uiState.generalErrorMessage) {
        uiState.generalErrorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    LaunchedEffect(uiState.ratingMessage) {
        uiState.ratingMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
            viewModel.clearRatingMessage()
        }
    }

    val showTopLinearProgress =
        uiState.isLoadingSummary ||
            uiState.isCastLoading ||
            uiState.isNextEpisodeLoading ||
            uiState.isPreviousEpisodeLoading ||
            isLoadingGlobal

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) // Use the remembered snackbarHostState
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (showTopLinearProgress) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                DetailArea(
                    uiState = uiState,
                    showDetailArgs = showDetailArgs,
                    isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                    isWatchlist = isWatchlist,
                    isWatchlistLoading = isWatchlistLoading,
                    showRating = showRating,
                    showStats = showStats,
                    onSeasonsClick = { viewModel.onSeasonsClick() },
                    onWatchlistClick = { viewModel.onAddRemoveWatchlistClick() },
                    onRateClick = { rating -> viewModel.onRateShow(rating) },
                    onCastItemClick = { castItem ->
                        val personId = castItem.traktId?.toString()
                        val name = castItem.name
                        if (!personId.isNullOrEmpty() && !name.isNullOrEmpty()) {
                            navController.navigate(
                                Destinations.PersonDetail(
                                    personId = personId,
                                    personName = name,
                                    personImageUrl = castItem.originalImageUrl,
                                ),
                            )
                        }
                    },
                    onSimilarShowClick = { show -> viewModel.onSimilarShowClicked(show) },
                    onRetry = { viewModel.selectedShow(showDetailArgs) },
                    onBack = { navController.navigateUp() },
                )
            }

            val navigateToShowDetail by viewModel.navigateToShowDetail.collectAsStateWithLifecycle()

            LaunchedEffect(navigateToShowDetail) {
                navigateToShowDetail?.let {
                    navController.navigate(
                        Destinations.ShowDetail(
                            showId = it.showId,
                            showTitle = it.showTitle,
                            showImageUrl = it.showImageUrl,
                            showBackgroundUrl = it.showBackgroundUrl,
                            imdbID = it.imdbID,
                            isAuthorizedOnTrakt = it.isAuthorizedOnTrakt,
                            showTraktId = it.showTraktId,
                        ),
                    )
                    viewModel.onShowDetailNavigationComplete()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun DetailArea(
    uiState: ShowDetailViewModel.ShowDetailUiState,
    showDetailArgs: ShowDetailArg,
    isAuthorizedOnTrakt: Boolean,
    isWatchlist: Boolean,
    isWatchlistLoading: Boolean,
    showRating: TraktShowRating?,
    showStats: TraktShowStats?,
    onSeasonsClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onRateClick: (Int) -> Unit,
    onCastItemClick: (item: TraktCast) -> Unit,
    onSimilarShowClick: (item: TraktRelatedShows) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val windowSizeClass = getWindowSizeClass()?.widthSizeClass ?: WindowWidthSizeClass.Compact

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp),
        ) {
            if (uiState.isLoadingSummary && uiState.showSummary == null) { // Show placeholder only if no data yet
                SummaryPlaceholder() // Or a simpler version, or nothing if LinearProgress is enough
            } else if (uiState.summaryErrorMessage != null) {
                ErrorState(message = uiState.summaryErrorMessage) {
                    onRetry()
                }
            } else if (uiState.showSummary != null) {
                BackdropAndTitle(
                    showDetailArgs = showDetailArgs,
                    showSummary = uiState.showSummary,
                    onBack = onBack,
                )
                SynopsisArea(
                    showSummary = uiState.showSummary,
                    widthSizeClass = windowSizeClass,
                )
                if (uiState.showSummary.id != -1) {
                    // ----- Watch Providers Section -----
                    WatchProvidersSection(uiState = uiState)

                    ShowDetailButtons(
                        isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                        isWatchlist = isWatchlist,
                        isLoading = isWatchlistLoading,
                        isRating = uiState.isRating,
                        userRating = uiState.userRating,
                        onSeasonsClick = onSeasonsClick,
                        onWatchlistClick = onWatchlistClick,
                        onRateClick = onRateClick,
                        widthSizeClass = windowSizeClass,
                    )
                }
            } else if (showDetailArgs.showImageUrl != null || showDetailArgs.showBackgroundUrl != null) {
                // Fallback for initial state with args but no summary yet
                BackdropAndTitle(showDetailArgs = showDetailArgs, showSummary = null, onBack = onBack)
            }

            showRating?.let { ratingData ->
                if (ratingData.votes != 0) {
                    TraktRatingSummary(ratingData, userRating = uiState.userRating)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))
                }
            }

            // ----- Show Cast Section -----
            ShowCast(uiState = uiState, onCastItemClick = onCastItemClick)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))

            // ----- Next Episode Section -----
            NextEpisode(uiState = uiState)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))

            // ----- Previous Episode Section -----
            PreviousEpisode(uiState = uiState)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))

            // ----- Trakt Stats Section (Optional) -----
            showStats?.let { statsData -> }

            // ----- Similar Shows Section -----
            SimilarShows(uiState = uiState, onSimilarShowClick = onSimilarShowClick)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))
        }
    }
}

// ... skipped intermediate functions ...

@Composable
fun ShowDetailButtons(
    isAuthorizedOnTrakt: Boolean?,
    isWatchlist: Boolean?,
    isLoading: Boolean,
    isRating: Boolean = false,
    userRating: Int? = null,
    onSeasonsClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onRateClick: (Int) -> Unit = {},
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
) {
    var showRatingSheet by rememberSaveable { mutableStateOf(false) }
    val buttonSpacing = if (widthSizeClass == WindowWidthSizeClass.Expanded) 24.dp else 16.dp

    Row(
        modifier =
            Modifier
                .wrapContentWidth(Alignment.Start)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_standard_double),
                    vertical = dimensionResource(id = R.dimen.padding_standard),
                )
                .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.OutlinedButton(
            onClick = { onSeasonsClick() },
            modifier = Modifier.widthIn(min = 120.dp).fillMaxHeight(),
        ) {
            Text(text = "Seasons")
        }
        if (isAuthorizedOnTrakt == true) {
            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                if (isWatchlist == true) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { onWatchlistClick() },
                        modifier = Modifier.widthIn(min = 120.dp).fillMaxHeight(),
                    ) {
                        Icon(
                            imageVector =
                                androidx.compose.ui.graphics.vector.ImageVector.vectorResource(
                                    id = R.drawable.ic_watchlist_remove,
                                ),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Listed",
                            maxLines = 1,
                        )
                    }
                } else {
                    Button(
                        onClick = { onWatchlistClick() },
                        modifier = Modifier.widthIn(min = 120.dp).fillMaxHeight(),
                    ) {
                        Icon(
                            imageVector = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = R.drawable.ic_watchlist_add),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "List",
                            maxLines = 1,
                        )
                    }
                }
            }

            // Rate button
            if (isRating) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                androidx.compose.material3.OutlinedButton(
                    onClick = { showRatingSheet = true },
                    modifier =
                        Modifier
                            .widthIn(min = 120.dp)
                            .fillMaxHeight()
                            .testTag("rate_show_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = RatingStarColor,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (userRating != null) "$userRating★" else "Rate",
                    )
                }
            }
        }
    }

    if (showRatingSheet) {
        RatingBottomSheet(
            currentRating = userRating,
            onDismiss = { showRatingSheet = false },
            onRate = { selectedRating ->
                showRatingSheet = false
                onRateClick(selectedRating)
            },
        )
    }
}

@Composable
fun WatchProvidersSection(uiState: ShowDetailViewModel.ShowDetailUiState) {
    val providersList = uiState.watchProviders?.providers
    if (!providersList.isNullOrEmpty()) {
        Column(
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_standard_double)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            SectionHeadingText(text = "Where to Watch")

            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = dimensionResource(id = R.dimen.padding_standard),
                        ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(providersList) { provider ->
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w200${provider.logoUrl}",
                        contentDescription = provider.name,
                        modifier =
                            Modifier
                                .size(60.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    } else if (uiState.isWatchProvidersLoading) {
        Column(
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_standard_double)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            SectionHeadingText(text = "Where to Watch")
            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = dimensionResource(id = R.dimen.padding_standard),
                        ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(5) {
                    Box(
                        modifier =
                            Modifier
                                .size(60.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .shimmer()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }
}

@Composable
fun ShowCast(
    uiState: ShowDetailViewModel.ShowDetailUiState,
    onCastItemClick: (item: TraktCast) -> Unit,
) {
    if (!uiState.traktCast.isNullOrEmpty()) {
        ShowCastList(list = uiState.traktCast, onClick = onCastItemClick)
    } else if (uiState.isCastLoading) {
        CastListPlaceholder()
    } else if (uiState.castErrorMessage != null) {
        ErrorState(message = uiState.castErrorMessage)
    }
}

@Composable
fun ShowCastList(
    list: List<TraktCast>,
    onClick: (item: TraktCast) -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_standard_double)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_cast_list))

        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_standard_double),
                        vertical = dimensionResource(id = R.dimen.padding_standard),
                    ),
        ) {
            items(items = list) { item ->
                ShowCastItem(item = item) { showCastItem ->
                    onClick(showCastItem)
                }
            }
        }
    }
}

@Composable
fun ShowCastItem(
    item: TraktCast,
    onClick: (item: TraktCast) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .padding(dimensionResource(id = R.dimen.padding_standard))
                .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                .testTag("cast_list_item")
                .clickable { onClick(item) },
        verticalArrangement = Arrangement.Top, // Align content to top
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PosterImage(
            url = item.originalImageUrl ?: "",
            modifier =
                Modifier
                    .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                    .height(dimensionResource(id = R.dimen.compose_show_detail_poster_height)),
        )

        item.name?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier =
                    Modifier
                        .padding(top = dimensionResource(id = R.dimen.padding_extra_small))
                        .fillMaxWidth(),
                maxLines = 2,
            )
        }

        item.character?.let { characterName ->
            Text(
                text = "as $characterName",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Thin,
                modifier =
                    Modifier
                        .fillMaxWidth(),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun PreviousEpisode(uiState: ShowDetailViewModel.ShowDetailUiState) {
    if (uiState.showPreviousEpisode != null &&
        (
            uiState.showPreviousEpisode.previousEpisodeName?.isNotBlank() == true ||
                uiState.showPreviousEpisode.previousEpisodeAirdate?.isNotBlank() == true
        ) // Check if there's actual data
    ) {
        PreviousEpisode(showPreviousEpisode = uiState.showPreviousEpisode)
    } else if (uiState.isPreviousEpisodeLoading) {
        EpisodePlaceholder()
    } else if (uiState.previousEpisodeErrorMessage != null) {
        ErrorState(
            message = uiState.previousEpisodeErrorMessage,
        )
    }
}

@Composable
fun PreviousEpisode(showPreviousEpisode: ShowPreviousEpisode) {
    val paddingStandardDouble = dimensionResource(id = R.dimen.padding_standard_double)
    val paddingExtraSmall = dimensionResource(id = R.dimen.padding_extra_small)
    val paddingStandard = dimensionResource(id = R.dimen.padding_standard)

    Column(modifier = Modifier.padding(top = paddingStandardDouble)) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_previous_episode_heading))

        Text(
            text =
                stringResource(
                    R.string.show_detail_episode_season_info,
                    showPreviousEpisode.previousEpisodeSeason ?: "N/A",
                    showPreviousEpisode.previousEpisodeNumber ?: "N/A",
                    showPreviousEpisode.previousEpisodeName ?: stringResource(R.string.title_unknown),
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = paddingStandardDouble,
                        top = paddingExtraSmall,
                        end = paddingStandardDouble,
                        bottom = paddingStandard,
                    ),
            style = MaterialTheme.typography.titleSmall,
        )

        val airstamp = showPreviousEpisode.previousEpisodeAirstamp
        if (airstamp != null) {
            val relativeTime = com.theupnextapp.common.utils.DateUtils.getRelativeTimeSpanString(airstamp)
            if (relativeTime != null) {
                Text(
                    text = "Aired $relativeTime",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary, // Different color for past
                    modifier =
                        Modifier.padding(
                            start = paddingStandardDouble,
                            bottom = paddingExtraSmall,
                        ),
                )
            }
        }

        showPreviousEpisode.previousEpisodeSummary?.let { summary ->
            if (summary.isNotBlank()) {
                EpisodeSummary(summary = summary)
            }
        }
    }
}

@Composable
fun NextEpisode(uiState: ShowDetailViewModel.ShowDetailUiState) {
    if (uiState.showNextEpisode != null &&
        (
            uiState.showNextEpisode.nextEpisodeName?.isNotBlank() == true ||
                uiState.showNextEpisode.nextEpisodeAirdate?.isNotBlank() == true
        ) // Check if there's actual data
    ) {
        NextEpisode(showNextEpisode = uiState.showNextEpisode)
    } else if (uiState.isNextEpisodeLoading) {
        EpisodePlaceholder()
    } else if (uiState.nextEpisodeErrorMessage != null) {
        ErrorState(message = uiState.nextEpisodeErrorMessage)
    }
}

@Composable
private fun NextEpisode(showNextEpisode: ShowNextEpisode) {
    val paddingStandardDouble = dimensionResource(id = R.dimen.padding_standard_double)
    val paddingExtraSmall = dimensionResource(id = R.dimen.padding_extra_small)
    val paddingStandard = dimensionResource(id = R.dimen.padding_standard)

    Column(modifier = Modifier.padding(top = paddingStandardDouble)) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_next_episode_heading))

        Text(
            text =
                stringResource(
                    R.string.show_detail_episode_season_info,
                    showNextEpisode.nextEpisodeSeason ?: "N/A",
                    showNextEpisode.nextEpisodeNumber ?: "N/A",
                    showNextEpisode.nextEpisodeName ?: stringResource(R.string.title_unknown),
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = paddingStandardDouble,
                        top = paddingExtraSmall,
                        end = paddingStandardDouble,
                        bottom = paddingStandard,
                    ),
            style = MaterialTheme.typography.titleSmall,
        )

        val airstamp = showNextEpisode.nextEpisodeAirstamp
        if (airstamp != null) {
            val relativeTime = com.theupnextapp.common.utils.DateUtils.getRelativeTimeSpanString(airstamp)
            if (relativeTime != null) {
                Text(
                    text = "Airing $relativeTime",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier.padding(
                            start = paddingStandardDouble,
                            bottom = paddingExtraSmall,
                        ),
                )
            }
        }

        val summary = showNextEpisode.nextEpisodeSummary
        if (!summary.isNullOrBlank()) {
            EpisodeSummary(summary = summary)
        }
    }
}

@Composable
fun EpisodeSummary(summary: String) {
    val paddingStandardDouble = dimensionResource(id = R.dimen.padding_standard_double)
    val cleanedSummary = Jsoup.parse(summary).text()
    Text(
        text = cleanedSummary,
        style = MaterialTheme.typography.bodyMedium,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = paddingStandardDouble,
                    end = paddingStandardDouble,
                    bottom = paddingStandardDouble,
                ),
    )
}

@Composable
fun TraktRatingSummary(
    rating: TraktShowRating,
    userRating: Int? = null,
) {
    val paddingStandardDouble = dimensionResource(id = R.dimen.padding_standard_double)
    val paddingExtraSmall = dimensionResource(id = R.dimen.padding_extra_small)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingStandardDouble),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(top = paddingExtraSmall),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Star,
                    contentDescription = stringResource(id = R.string.show_detail_rating_content_description),
                    tint = RatingStarColor,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(paddingExtraSmall))

                val scaledRating = ((rating.rating ?: 0.0) * 10).toInt()

                Text(
                    text = "$scaledRating%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "User Score",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "(${rating.votes} votes)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                if (userRating != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You: $userRating★",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RatingStarColor,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    currentRating: Int?,
    onDismiss: () -> Unit,
    onRate: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedRating by remember { mutableIntStateOf(currentRating ?: 0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Rate This Show",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedRating > 0) "$selectedRating / 10" else "Tap a star",
                style = MaterialTheme.typography.titleMedium,
                color = if (selectedRating > 0) RatingStarColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Star row — 10 tappable stars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                for (i in 1..10) {
                    IconButton(
                        onClick = { selectedRating = i },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector =
                                if (i <= selectedRating) {
                                    Icons.Filled.Star
                                } else {
                                    Icons.Outlined.Star
                                },
                            contentDescription = "Rate $i",
                            tint = if (i <= selectedRating) RatingStarColor else RatingStarUnselected,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onRate(selectedRating) },
                enabled = selectedRating > 0,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("submit_rating_button"),
            ) {
                Text(text = "Submit Rating")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SummaryPlaceholder() {
    Column(modifier = Modifier.shimmer()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.7f)
                    .height(24.dp)
                    .padding(horizontal = 16.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .padding(horizontal = 16.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            repeat(4) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(100.dp, 40.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
            )
            Box(
                modifier =
                    Modifier
                        .size(100.dp, 40.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CastListPlaceholder() {
    LazyRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shimmer(), // Make sure you have the shimmer dependency and import
    ) {
        items(5) {
            Column(
                modifier =
                    Modifier
                        .padding(end = 8.dp)
                        .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box( // Poster placeholder
                    modifier =
                        Modifier
                            .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                            .height(dimensionResource(id = R.dimen.compose_show_detail_poster_height))
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box( // Name placeholder
                    modifier =
                        Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                )
            }
        }
    }
}

@Composable
fun EpisodePlaceholder() {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shimmer(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box( // Episode Title placeholder
                modifier =
                    Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column { // Summary lines
                repeat(3) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        onRetry?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = it) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun SimilarShows(
    uiState: ShowDetailViewModel.ShowDetailUiState,
    onSimilarShowClick: (item: TraktRelatedShows) -> Unit,
) {
    if (!uiState.similarShows.isNullOrEmpty()) {
        SimilarShowsList(list = uiState.similarShows, onClick = onSimilarShowClick)
    } else if (uiState.isSimilarShowsLoading) {
        CastListPlaceholder() // Use same placeholder for now or create generic one
    }
}

@Composable
fun SimilarShowsList(
    list: List<TraktRelatedShows>,
    onClick: (item: TraktRelatedShows) -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_standard_double)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_similar_shows_heading))

        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_standard_double),
                        vertical = dimensionResource(id = R.dimen.padding_standard),
                    ),
        ) {
            items(items = list) { item ->
                SimilarShowItem(item = item) { showItem ->
                    onClick(showItem)
                }
            }
        }
    }
}

@Composable
fun SimilarShowItem(
    item: TraktRelatedShows,
    onClick: (item: TraktRelatedShows) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .padding(dimensionResource(id = R.dimen.padding_standard))
                .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                .clickable { onClick(item) },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PosterImage(
            url = item.originalImageUrl ?: item.mediumImageUrl ?: "",
            modifier =
                Modifier
                    .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                    .height(dimensionResource(id = R.dimen.compose_show_detail_poster_height)),
        )

        item.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier =
                    Modifier
                        .padding(top = dimensionResource(id = R.dimen.padding_extra_small))
                        .fillMaxWidth(),
                maxLines = 2,
            )
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun ShowDetailScreenPreview() {
    MaterialTheme { // Wrap with your app's theme
        ShowDetailScreen(
            viewModel = hiltViewModel(), // This won't work well in Preview without Hilt setup for previews
            showDetailArgs =
                ShowDetailArg(
                    showId = "1",
                    showTitle = "Preview Show Title",
                    showImageUrl = "",
                    showBackgroundUrl = null,
                ),
            navController = androidx.navigation.compose.rememberNavController(),
        )
    }
}
