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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.theupnextapp.R
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.core.designsystem.ui.components.CastMember
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
    LaunchedEffect(showDetailArgs.showId) {
        viewModel.selectedShow(showDetailArgs)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isWatchlist by viewModel.isWatchlistShow.collectAsStateWithLifecycle()
    val isWatchlistLoading by viewModel.isWatchlistLoading.collectAsStateWithLifecycle()
    val showRating by viewModel.showRating.collectAsStateWithLifecycle()
    val showStats by viewModel.showStats.collectAsStateWithLifecycle()
    val isAuthorizedOnProvider by viewModel.isAuthorizedOnProvider.collectAsStateWithLifecycle()
    val activeProvider by viewModel.activeProvider.collectAsStateWithLifecycle()
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
                    isAuthorizedOnTrakt = isAuthorizedOnProvider, // rename below
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
            modifier = Modifier.fillMaxSize(),
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
                    isAuthorizedOnTrakt = isAuthorizedOnProvider,
                    activeProvider = activeProvider,
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
                    contentPadding = innerPadding,
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
                            isAuthorizedOnTrakt = it.isAuthorizedOnTrakt, // wait, this is from the arg? Yes, I will leave it
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
    activeProvider: String,
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
    contentPadding: PaddingValues,
) {
    val windowSizeClass = getWindowSizeClass()?.widthSizeClass ?: WindowWidthSizeClass.Compact

    if (windowSizeClass == WindowWidthSizeClass.Expanded) {
        ExpandedDetailArea(
            uiState = uiState,
            showDetailArgs = showDetailArgs,
            isAuthorizedOnTrakt = isAuthorizedOnTrakt,
            activeProvider = activeProvider,
            isWatchlist = isWatchlist,
            isWatchlistLoading = isWatchlistLoading,
            showRating = showRating,
            showStats = showStats,
            onSeasonsClick = onSeasonsClick,
            onWatchlistClick = onWatchlistClick,
            onRateClick = onRateClick,
            onCastItemClick = onCastItemClick,
            onSimilarShowClick = onSimilarShowClick,
            onRetry = onRetry,
            onBack = onBack,
            contentPadding = contentPadding,
            windowSizeClass = windowSizeClass,
        )
    } else {
        CompactDetailArea(
            uiState = uiState,
            showDetailArgs = showDetailArgs,
            isAuthorizedOnTrakt = isAuthorizedOnTrakt,
            activeProvider = activeProvider,
            isWatchlist = isWatchlist,
            isWatchlistLoading = isWatchlistLoading,
            showRating = showRating,
            showStats = showStats,
            onSeasonsClick = onSeasonsClick,
            onWatchlistClick = onWatchlistClick,
            onRateClick = onRateClick,
            onCastItemClick = onCastItemClick,
            onSimilarShowClick = onSimilarShowClick,
            onRetry = onRetry,
            onBack = onBack,
            contentPadding = contentPadding,
            windowSizeClass = windowSizeClass,
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun CompactDetailArea(
    uiState: ShowDetailViewModel.ShowDetailUiState,
    showDetailArgs: ShowDetailArg,
    isAuthorizedOnTrakt: Boolean,
    activeProvider: String,
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
    contentPadding: PaddingValues,
    windowSizeClass: WindowWidthSizeClass,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.isLoadingSummary && uiState.showSummary == null) {
                Column(modifier = Modifier.shimmer(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth(0.7f)
                            .height(24.dp)
                            .padding(horizontal = 16.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth(0.5f)
                            .height(16.dp)
                            .padding(horizontal = 16.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(Modifier.widthIn(max = 600.dp).padding(horizontal = 16.dp)) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .background(Color.LightGray.copy(alpha = 0.3f)),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp, 40.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                        )
                        Box(
                            modifier = Modifier
                                .size(100.dp, 40.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else if (uiState.summaryErrorMessage != null) {
                ErrorState(message = uiState.summaryErrorMessage) {
                    onRetry()
                }
            } else if (uiState.showSummary != null) {
                BackdropAndTitle(
                    showDetailArgs = showDetailArgs,
                    showSummary = uiState.showSummary,
                    certification = uiState.certification,
                    onBack = onBack,
                )
                Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                    SynopsisArea(
                        showSummary = uiState.showSummary,
                        widthSizeClass = windowSizeClass,
                    )
                }
                if (uiState.showSummary.id != -1) {
                    Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                        WatchProvidersSection(uiState = uiState)
                    }

                    Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                        ShowDetailButtons(
                            isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                            activeProvider = activeProvider,
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
                }
            } else if (showDetailArgs.showImageUrl != null || showDetailArgs.showBackgroundUrl != null) {
                BackdropAndTitle(showDetailArgs = showDetailArgs, showSummary = null, onBack = onBack)
            }

            showRating?.let { ratingData ->
                if (ratingData.votes != 0) {
                    Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                        TraktRatingSummary(ratingData, userRating = uiState.userRating)
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))
                }
            }

            Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                ShowCast(uiState = uiState, onCastItemClick = onCastItemClick)
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))

            Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                NextEpisode(uiState = uiState)
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))

            Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                PreviousEpisode(uiState = uiState)
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))

            showStats?.let { statsData -> }

            Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                SimilarShows(uiState = uiState, onSimilarShowClick = onSimilarShowClick)
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))
            Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding()))
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun ExpandedDetailArea(
    uiState: ShowDetailViewModel.ShowDetailUiState,
    showDetailArgs: ShowDetailArg,
    isAuthorizedOnTrakt: Boolean,
    activeProvider: String,
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
    contentPadding: PaddingValues,
    windowSizeClass: WindowWidthSizeClass,
) {
    val scrollState = rememberScrollState()

    val leftWeight = 0.35f
    val rightWeight = 0.65f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Left Column: Poster and Action Buttons
            Column(
                modifier = Modifier
                    .weight(leftWeight)
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val posterUrl = uiState.showSummary?.originalImageUrl
                    ?: uiState.showSummary?.mediumImageUrl
                    ?: showDetailArgs.showImageUrl

                Box(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                    if (posterUrl != null) {
                        PosterImage(
                            url = posterUrl,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(
                                    RoundedCornerShape(
                                        bottomEnd = 24.dp, topEnd = 24.dp
                                    )
                                )
                        )
                    }

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.showSummary?.id != -1 && uiState.showSummary != null) {
                    ShowDetailButtons(
                        isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                        activeProvider = activeProvider,
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

                showRating?.let { ratingData ->
                    if (ratingData.votes != 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TraktRatingSummary(ratingData, userRating = uiState.userRating)
                    }
                }
            }

            // Right Column: Title, Synopsis, Providers
            Column(
                modifier = Modifier
                    .weight(rightWeight)
                    .padding(end = 24.dp, start = 16.dp)
            ) {
                if (uiState.isLoadingSummary && uiState.showSummary == null) {
                    SummaryPlaceholder()
                } else if (uiState.summaryErrorMessage != null) {
                    ErrorState(message = uiState.summaryErrorMessage) { onRetry() }
                } else if (uiState.showSummary != null) {
                    // Hero Title
                    uiState.showSummary.name?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        uiState.showSummary.status?.let { status ->
                            Text(
                                text = status,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }

                        if (!uiState.showSummary.status.isNullOrEmpty() && !uiState.certification.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        uiState.certification?.let { cert ->
                            Text(
                                text = cert,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }

                    SynopsisArea(
                        showSummary = uiState.showSummary,
                        widthSizeClass = windowSizeClass,
                        modifier = Modifier.padding(top = 8.dp),
                    )

                    if (uiState.showSummary.id != -1) {
                        WatchProvidersSection(uiState = uiState)
                    }
                }
            }
        } // End of top Row

        // Full width lists below
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = contentPadding.calculateBottomPadding())
        ) {
            ShowCast(uiState = uiState, onCastItemClick = onCastItemClick)

            Spacer(modifier = Modifier.height(32.dp))
            NextEpisode(uiState = uiState)

            Spacer(modifier = Modifier.height(32.dp))
            PreviousEpisode(uiState = uiState)

            Spacer(modifier = Modifier.height(32.dp))
            SimilarShows(uiState = uiState, onSimilarShowClick = onSimilarShowClick)
        }
    }
}

// ... skipped intermediate functions ...

@Composable
fun ShowDetailButtons(
    isAuthorizedOnTrakt: Boolean?,
    activeProvider: String,
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
    val isExpanded = widthSizeClass == WindowWidthSizeClass.Expanded || widthSizeClass == WindowWidthSizeClass.Medium
    val buttonSpacing = if (isExpanded) 12.dp else 16.dp

    if (isExpanded) {
        ShowDetailButtonsExpanded(
            isAuthorizedOnTrakt = isAuthorizedOnTrakt,
            activeProvider = activeProvider,
            isWatchlist = isWatchlist,
            isLoading = isLoading,
            isRating = isRating,
            userRating = userRating,
            onSeasonsClick = onSeasonsClick,
            onWatchlistClick = onWatchlistClick,
            onRateClick = { showRatingSheet = true },
            buttonSpacing = buttonSpacing,
        )
    } else {
        ShowDetailButtonsCompact(
            isAuthorizedOnTrakt = isAuthorizedOnTrakt,
            activeProvider = activeProvider,
            isWatchlist = isWatchlist,
            isLoading = isLoading,
            isRating = isRating,
            userRating = userRating,
            onSeasonsClick = onSeasonsClick,
            onWatchlistClick = onWatchlistClick,
            onRateClick = { showRatingSheet = true },
            buttonSpacing = buttonSpacing,
        )
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
private fun ShowDetailButtonsExpanded(
    isAuthorizedOnTrakt: Boolean?,
    activeProvider: String,
    isWatchlist: Boolean?,
    isLoading: Boolean,
    isRating: Boolean,
    userRating: Int?,
    onSeasonsClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onRateClick: () -> Unit,
    buttonSpacing: Dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.padding_standard_double),
                vertical = dimensionResource(id = R.dimen.padding_standard),
            ),
        verticalArrangement = Arrangement.spacedBy(buttonSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedButton(
            onClick = onSeasonsClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text(text = stringResource(id = R.string.btn_show_detail_seasons))
        }
        if (isAuthorizedOnTrakt == true) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                if (isWatchlist == true) {
                    OutlinedButton(
                        onClick = onWatchlistClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Icon(
                            imageVector =
                                ImageVector.vectorResource(
                                    id = R.drawable.ic_watchlist_remove,
                                ),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.btn_show_detail_remove_from_favorites),
                            maxLines = 1,
                        )
                    }
                } else {
                    Button(
                        onClick = onWatchlistClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_watchlist_add),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.btn_show_detail_add_to_favorites),
                            maxLines = 1,
                        )
                    }
                }
            }

            // Rate button
            if (isRating) {
                Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onRateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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
                        text = if (userRating != null) "$userRating★" else stringResource(id = R.string.show_detail_rating_heading),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowDetailButtonsCompact(
    isAuthorizedOnTrakt: Boolean?,
    activeProvider: String,
    isWatchlist: Boolean?,
    isLoading: Boolean,
    isRating: Boolean,
    userRating: Int?,
    onSeasonsClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onRateClick: () -> Unit,
    buttonSpacing: Dp,
) {
    Row(
        modifier =
            Modifier
                .wrapContentWidth(Alignment.Start)
                .horizontalScroll(rememberScrollState())
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_standard_double),
                    vertical = dimensionResource(id = R.dimen.padding_standard),
                )
                .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onSeasonsClick,
            modifier = Modifier.widthIn(min = 120.dp),
        ) {
            Text(text = stringResource(id = R.string.btn_show_detail_seasons))
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
                    OutlinedButton(
                        onClick = onWatchlistClick,
                        modifier = Modifier.widthIn(min = 120.dp).fillMaxHeight(),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_watchlist_remove),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.btn_show_detail_remove_from_favorites),
                            maxLines = 1,
                        )
                    }
                } else {
                    Button(
                        onClick = onWatchlistClick,
                        modifier = Modifier.widthIn(min = 120.dp).fillMaxHeight(),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_watchlist_add),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.btn_show_detail_add_to_favorites),
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
                OutlinedButton(
                    onClick = onRateClick,
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
                        text = if (userRating != null) "$userRating★" else stringResource(id = R.string.show_detail_rating_heading),
                    )
                }
            }
        }
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
            SectionHeadingText(text = stringResource(id = R.string.show_detail_where_to_watch))

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
                                .clip(RoundedCornerShape(12.dp)),
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
            SectionHeadingText(text = stringResource(id = R.string.show_detail_where_to_watch))
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
                                .testTag("watch_providers_loading")
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
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
                CastMember(
                    name = item.name,
                    role = item.character,
                    originalImageUrl = item.originalImageUrl,
                    modifier = Modifier.padding(16.dp).testTag("cast_list_item"),
                    onClick = { onClick(item) }
                )
            }
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
            val relativeTime = DateUtils.getRelativeTimeSpanString(airstamp)
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
            val relativeTime = DateUtils.getRelativeTimeSpanString(airstamp)
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
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(top = paddingExtraSmall),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
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
                Text(text = stringResource(id = R.string.btn_show_detail_submit_rating))
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                Text(text = stringResource(id = R.string.load_state_retry_button))
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
