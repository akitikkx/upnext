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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ShowSeasonsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavOptionsBuilder
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.RouteOrDirection
import com.theupnextapp.R
import com.theupnextapp.common.utils.getWindowSizeClass
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.domain.TraktShowStats
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.components.SectionHeadingText
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.util.Locale

@ExperimentalMaterial3Api
@Destination<RootGraph>(navArgs = ShowDetailArg::class)
@Composable
fun ShowDetailScreen(
    viewModel: ShowDetailViewModel = hiltViewModel(),
    showDetailArgs: ShowDetailArg,
    navigator: DestinationsNavigator
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavoriteShow.collectAsStateWithLifecycle()
    val showRating by viewModel.showRating.collectAsStateWithLifecycle()
    val showStats by viewModel.showStats.collectAsStateWithLifecycle()
    val isAuthorizedOnTrakt by viewModel.isAuthorizedOnTrakt.collectAsStateWithLifecycle()
    val navigateToSeasons by viewModel.navigateToSeasons.collectAsStateWithLifecycle()
    val isLoadingGlobal by viewModel.isLoading.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = showDetailArgs) {
        viewModel.selectedShow(showDetailArgs)
    }

    LaunchedEffect(navigateToSeasons) {
        if (navigateToSeasons) {
            navigator.navigate(
                ShowSeasonsScreenDestination(
                    ShowDetailArg(
                        showId = showDetailArgs.showId,
                        showTitle = showDetailArgs.showTitle,
                        showImageUrl = showDetailArgs.showImageUrl,
                        showBackgroundUrl = showDetailArgs.showBackgroundUrl,
                        imdbID = showDetailArgs.imdbID,
                        isAuthorizedOnTrakt = isAuthorizedOnTrakt
                    )
                )
            )
            viewModel.onSeasonsNavigationComplete()
        }
    }

    LaunchedEffect(uiState.generalErrorMessage) {
        uiState.generalErrorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
                // viewModel.clearGeneralErrorMessage()
            }
        }
    }

    val showTopLinearProgress = uiState.isLoadingSummary ||
            uiState.isCastLoading ||
            uiState.isNextEpisodeLoading ||
            uiState.isPreviousEpisodeLoading ||
            isLoadingGlobal

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showTopLinearProgress) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            DetailArea(
                uiState = uiState,
                showDetailArgs = showDetailArgs,
                isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                isFavorite = isFavorite,
                showRating = showRating,
                showStats = showStats,
                onSeasonsClick = { viewModel.onSeasonsClick() },
                onFavoriteClick = { viewModel.onAddRemoveFavoriteClick() },
                onCastItemClick = { castItem -> viewModel.onShowCastItemClicked(castItem) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Bottom sheet for cast (if any) - this would typically be handled
        // by a ModalBottomSheetLayout if you were using one, or manage its visibility
        // within this Box.
        val castMemberForBottomSheet by viewModel.showCastBottomSheet.collectAsStateWithLifecycle()
        castMemberForBottomSheet?.let {
            // Your BottomSheet Composable - e.g., A ModalBottomSheetLayout would wrap the content
            // or you'd use a custom Composable here that overlays.
            // For simplicity, if it's a simple overlay:
            // Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(200.dp), elevation = 4.dp) {
            //    Text("Bottom sheet for ${it.name}")
            // }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun DetailArea(
    uiState: ShowDetailViewModel.ShowDetailUiState,
    showDetailArgs: ShowDetailArg,
    isAuthorizedOnTrakt: Boolean,
    isFavorite: Boolean,
    showRating: TraktShowRating?,
    showStats: TraktShowStats?,
    onSeasonsClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCastItemClick: (item: ShowCast) -> Unit
) {
    val scrollState = rememberScrollState()
    val windowSizeClass = getWindowSizeClass()?.widthSizeClass ?: WindowWidthSizeClass.Compact

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        if (uiState.isLoadingSummary && uiState.showSummary == null) { // Show placeholder only if no data yet
            SummaryPlaceholder() // Or a simpler version, or nothing if LinearProgress is enough
        } else if (uiState.summaryErrorMessage != null) {
            ErrorState(message = uiState.summaryErrorMessage) {
                // Retry
            }
        } else if (uiState.showSummary != null) {
            BackdropAndTitle(
                showDetailArgs = showDetailArgs,
                showSummary = uiState.showSummary
            )
            SynopsisArea(
                showSummary = uiState.showSummary,
                widthSizeClass = windowSizeClass
            )
            if (uiState.showSummary.id != -1) {
                ShowDetailButtons(
                    isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                    isFavorite = isFavorite,
                    onSeasonsClick = onSeasonsClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        } else if (showDetailArgs.showImageUrl != null || showDetailArgs.showBackgroundUrl != null) {
            // Fallback for initial state with args but no summary yet
            BackdropAndTitle(showDetailArgs = showDetailArgs, showSummary = null)
        }

        showRating?.let { ratingData ->
            if (ratingData.votes != 0) {
                TraktRatingSummary(ratingData)
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
        showStats?.let { statsData ->
            // Assuming TraktStatsSummary Composable exists
            // TraktStatsSummary(statsData)
            // Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard_double)))
        }
    }
}

@Composable
fun ShowCast(
    uiState: ShowDetailViewModel.ShowDetailUiState,
    onCastItemClick: (item: ShowCast) -> Unit
) {
    if (!uiState.showCast.isNullOrEmpty()) {
        ShowCastList(list = uiState.showCast, onClick = onCastItemClick)
    } else if (uiState.isCastLoading) {
        CastListPlaceholder()
    } else if (uiState.castErrorMessage != null) {
        ErrorState(message = uiState.castErrorMessage)
    }
}

@Composable
fun ShowCastList(
    list: List<ShowCast>,
    onClick: (item: ShowCast) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_standard_double)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_cast_list))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_standard_double),
                    vertical = dimensionResource(id = R.dimen.padding_standard)
                )
        ) {
            items(
                items = list,
                key = { it.id ?: it.name ?: "" }) { item ->
                ShowCastItem(item = item) { showCastItem ->
                    onClick(showCastItem)
                }
            }
        }
    }
}

@Composable
fun ShowCastItem(
    item: ShowCast,
    onClick: (item: ShowCast) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_standard))
            .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
            .clickable { onClick(item) },
        verticalArrangement = Arrangement.Top, // Align content to top
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PosterImage(
            url = item.originalImageUrl ?: "",
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                .height(dimensionResource(id = R.dimen.compose_show_detail_poster_height))
        )

        item.name?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(top = dimensionResource(id = R.dimen.padding_extra_small))
                    .fillMaxWidth(),
                maxLines = 2
            )
        }

        item.characterName?.let { characterName ->
            Text(
                text = "as $characterName",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Thin,
                modifier = Modifier
                    .fillMaxWidth(),
                maxLines = 1
            )
        }
    }
}

@Composable
fun PreviousEpisode(uiState: ShowDetailViewModel.ShowDetailUiState) {
    if (uiState.showPreviousEpisode != null &&
        (uiState.showPreviousEpisode.previousEpisodeName?.isNotBlank() == true ||
                uiState.showPreviousEpisode.previousEpisodeAirdate?.isNotBlank() == true) // Check if there's actual data
    ) {
        PreviousEpisode(showPreviousEpisode = uiState.showPreviousEpisode)
    } else if (uiState.isPreviousEpisodeLoading) {
        EpisodePlaceholder(type = "Previous Episode")
    } else if (uiState.previousEpisodeErrorMessage != null) {
        ErrorState(
            message = uiState.previousEpisodeErrorMessage
        )
    }
}

@Composable
fun PreviousEpisode(showPreviousEpisode: ShowPreviousEpisode) {
    Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_standard_double))) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_previous_episode_heading))

        Text(
            text = stringResource(
                R.string.show_detail_episode_season_info,
                showPreviousEpisode.previousEpisodeSeason ?: "N/A",
                showPreviousEpisode.previousEpisodeNumber ?: "N/A",
                showPreviousEpisode.previousEpisodeName ?: stringResource(R.string.title_unknown)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.padding_standard_double),
                    top = dimensionResource(id = R.dimen.padding_extra_small),
                    end = dimensionResource(id = R.dimen.padding_standard_double),
                    bottom = dimensionResource(id = R.dimen.padding_standard)
                ),
            style = MaterialTheme.typography.titleSmall
        )

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
        (uiState.showNextEpisode.nextEpisodeName?.isNotBlank() == true ||
                uiState.showNextEpisode.nextEpisodeAirdate?.isNotBlank() == true) // Check if there's actual data
    ) {
        NextEpisode(showNextEpisode = uiState.showNextEpisode)
    } else if (uiState.isNextEpisodeLoading) {
        EpisodePlaceholder(type = "Next Episode")
    } else if (uiState.nextEpisodeErrorMessage != null) {
        ErrorState(message = uiState.nextEpisodeErrorMessage)
    }
}

@Composable
private fun NextEpisode(showNextEpisode: ShowNextEpisode) {
    Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_standard_double))) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_next_episode_heading))

        Text(
            text = stringResource(
                R.string.show_detail_episode_season_info,
                showNextEpisode.nextEpisodeSeason ?: "N/A",
                showNextEpisode.nextEpisodeNumber ?: "N/A",
                showNextEpisode.nextEpisodeName ?: stringResource(R.string.title_unknown)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.padding_standard_double),
                    top = dimensionResource(id = R.dimen.padding_extra_small),
                    end = dimensionResource(id = R.dimen.padding_standard_double),
                    bottom = dimensionResource(id = R.dimen.padding_standard)
                ),
            style = MaterialTheme.typography.titleSmall
        )

        showNextEpisode.nextEpisodeSummary?.let { summary ->
            if (summary.isNotBlank()) {
                EpisodeSummary(summary = summary)
            }
        }
    }
}

@Composable
fun EpisodeSummary(summary: String) {
    val cleanedSummary = Jsoup.parse(summary).text()
    Text(
        text = cleanedSummary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(id = R.dimen.padding_standard_double),
                end = dimensionResource(id = R.dimen.padding_standard_double),
                bottom = dimensionResource(id = R.dimen.padding_standard_double)
            )
    )
}

@Composable
fun ShowDetailButtons(
    isAuthorizedOnTrakt: Boolean?,
    isFavorite: Boolean?,
    onSeasonsClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_standard_double)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Seasons",
            modifier = Modifier
                .clickable { onSeasonsClick() }
                .padding(8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        if (isAuthorizedOnTrakt == true) {
            Text(
                text = if (isFavorite == true) "Remove Favorite" else "Add Favorite",
                modifier = Modifier
                    .clickable { onFavoriteClick() }
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun TraktRatingSummary(rating: TraktShowRating) {
    Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_standard_double))) {
        SectionHeadingText(text = stringResource(R.string.show_detail_ratings_heading)) // Corrected string resource
        Text(
            text = "Rating: ${
                String.format(
                    Locale.US,
                    "%.1f",
                    rating.rating ?: 0.0
                )
            }/10 (${rating.votes} votes)",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small)) // Added some top padding
        )
    }
}

@Composable
fun SummaryPlaceholder() {
    Column(modifier = Modifier.shimmer()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(24.dp)
                .padding(horizontal = 16.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(16.dp)
                .padding(horizontal = 16.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp, 40.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .size(100.dp, 40.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CastListPlaceholder() {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer() // Make sure you have the shimmer dependency and import
    ) {
        items(5) {
            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box( // Poster placeholder
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                        .height(dimensionResource(id = R.dimen.compose_show_detail_poster_height))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box( // Name placeholder
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun EpisodePlaceholder(type: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer() // Make sure you have the shimmer dependency and import
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box( // Episode Title placeholder
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column { // Summary lines
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        onRetry?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = it) {
                Text("Retry")
            }
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
            showDetailArgs = ShowDetailArg(
                showId = "1",
                showTitle = "Preview Show Title",
                showImageUrl = "",
                showBackgroundUrl = null
            ),
            navigator = object : DestinationsNavigator {
                override fun navigate(
                    direction: Direction,
                    builder: DestinationsNavOptionsBuilder.() -> Unit
                ) {
                    // No-op for preview
                }

                override fun navigate(
                    direction: Direction,
                    navOptions: NavOptions?,
                    navigatorExtras: Navigator.Extras?
                ) {
                    // No-op for preview
                }

                override fun navigateUp(): Boolean {
                    return false // No-op
                }

                override fun popBackStack(): Boolean {
                    return false // No-op
                }

                override fun popBackStack(
                    route: RouteOrDirection,
                    inclusive: Boolean,
                    saveState: Boolean
                ): Boolean {
                    return false // No-op
                }

                override fun clearBackStack(route: RouteOrDirection): Boolean {
                    return false // No-op
                }

                override fun getBackStackEntry(route: RouteOrDirection): NavBackStackEntry? {
                    return null // No-op
                }
            }
        )
    }
}
