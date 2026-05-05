package com.theupnextapp.ui.dashboard

import android.net.Uri
import android.text.format.DateUtils
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.theupnextapp.R
import com.theupnextapp.common.utils.TraktConstants
import com.theupnextapp.core.designsystem.ui.components.ShimmerAiringSoon
import com.theupnextapp.core.designsystem.ui.components.ShimmerPosterCardRow
import com.theupnextapp.core.designsystem.ui.components.ShimmerRecommended
import com.theupnextapp.core.designsystem.ui.widgets.ListPosterCard
import com.theupnextapp.core.designsystem.ui.widgets.UpNextEpisodeCard
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.ui.components.EmptyState
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import kotlin.math.absoluteValue

@Suppress("LongMethod", "CyclomaticComplexMethod", "MagicNumber")
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val context = LocalContext.current
    val traktAccessToken by viewModel.traktAccessToken.collectAsStateWithLifecycle()
    val airingSoonShows by viewModel.airingSoonShows.collectAsStateWithLifecycle()
    val airingSoonImages by viewModel.airingSoonImages.collectAsStateWithLifecycle()
    val isLoadingAiringSoon by viewModel.isLoadingAiringSoon.collectAsStateWithLifecycle()

    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val historyImages by viewModel.historyImages.collectAsStateWithLifecycle()
    val isLoadingHistory by viewModel.isLoadingHistory.collectAsStateWithLifecycle()

    val recommendedShows by viewModel.recommendedShows.collectAsStateWithLifecycle()
    val recommendedShowsImages by viewModel.recommendedShowsImages.collectAsStateWithLifecycle()
    val isLoadingRecommendations by viewModel.isLoadingRecommendations.collectAsStateWithLifecycle()

    val todayShows by viewModel.todayShows.collectAsState()
    val mostAnticipatedShows by viewModel.mostAnticipatedShows.collectAsState()
    val isLoadingTodayShows by viewModel.isLoadingTodayShows.observeAsState(false)
    val isLoadingMostAnticipated by viewModel.isLoadingMostAnticipated.collectAsState()

    val regionalTrendingShows by viewModel.regionalTrendingShows.collectAsStateWithLifecycle()
    val regionalTrendingShowsImages by viewModel.regionalTrendingShowsImages.collectAsStateWithLifecycle()
    val isLoadingRegionalTrending by viewModel.isLoadingRegionalTrending.collectAsStateWithLifecycle()

    val activeProvider by viewModel.activeProvider.collectAsStateWithLifecycle()
    val simklAccessToken by viewModel.simklAccessToken.collectAsStateWithLifecycle()
    val simklPremieres by viewModel.simklPremieres.collectAsStateWithLifecycle()
    val isLoadingSimklPremieres by viewModel.isLoadingSimklPremieres.collectAsStateWithLifecycle()

    val isAuthorizedOnProvider by viewModel.isAuthorizedOnProvider.collectAsStateWithLifecycle()

    LaunchedEffect(traktAccessToken) {
        traktAccessToken?.access_token?.let {
            viewModel.fetchDashboardData(it)
        }
    }

    LaunchedEffect(traktAccessToken, simklAccessToken, activeProvider) {
        viewModel.fetchDashboardHistoryData()

        if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL && simklAccessToken != null) {
            viewModel.fetchSimklDashboardData()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompactPane = maxWidth < 600.dp
        val carouselPageSize = if (isCompactPane) 260.dp else 340.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp + contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = 16.dp + contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                top = 16.dp + contentPadding.calculateTopPadding(),
                bottom = 16.dp + contentPadding.calculateBottomPadding()
            ),
        ) {
            if (isAuthorizedOnProvider) {
                item {
                    Text(
                        text = stringResource(id = R.string.dashboard_my_upnext),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
            }

            if (!isAuthorizedOnProvider) {
                item {
                    Text(
                        text = stringResource(id = R.string.dashboard_tonight_on_tv),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    if (isLoadingTodayShows) {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (!todayShows.isNullOrEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { todayShows.orEmpty().size })
                        HorizontalPager(
                            state = pagerState,
                            pageSize = PageSize.Fixed(carouselPageSize),
                            pageSpacing = 16.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) { page ->
                            val show = todayShows.orEmpty().getOrNull(page) ?: return@HorizontalPager
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            val scale =
                                lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                                )
                            val alphaOffset =
                                lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                                )

                            Box(
                                modifier =
                                    Modifier
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            alpha = alphaOffset
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                Card(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(2f / 3f)
                                            .testTag("dashboard_show_card")
                                            .clickable {
                                                val direction =
                                                    Destinations.ShowDetail(
                                                        source = "today",
                                                        showId = show.showId.toString(),
                                                        showTitle = show.name,
                                                        showImageUrl = show.originalImage,
                                                        showBackgroundUrl = show.mediumImage,
                                                    )
                                                navController.navigate(direction)
                                            },
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model =
                                                ImageRequest.Builder(LocalContext.current)
                                                    .data(show.originalImage ?: show.mediumImage)
                                                    .crossfade(true)
                                                    .build(),
                                            contentDescription = show.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                                    .align(Alignment.BottomCenter)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                                        ),
                                                    ),
                                        )
                                        Text(
                                            text = show.name ?: "",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier =
                                                Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        EmptyState(
                            icon = Icons.Default.Tv,
                            title = stringResource(id = R.string.dashboard_no_tv_schedule),
                            message = stringResource(id = R.string.dashboard_no_tv_schedule_desc),
                            modifier = Modifier.fillMaxWidth().height(250.dp).padding(16.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(if (isCompactPane) 16.dp else 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (!isCompactPane) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBox,
                                        contentDescription = if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) stringResource(id = R.string.dashboard_connect_simkl_desc) else stringResource(id = R.string.dashboard_connect_trakt_desc),
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                                Text(
                                    stringResource(id = R.string.dashboard_unlock_personal_tracker),
                                    style = if (isCompactPane) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) stringResource(id = R.string.dashboard_connect_simkl_body) else stringResource(id = R.string.dashboard_connect_trakt_body),
                                    style = if (isCompactPane) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                                )
                                Button(onClick = {
                                    val builder = CustomTabsIntent.Builder()
                                    val customTabsIntent = builder.build()
                                    if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                                        customTabsIntent.launchUrl(context, Uri.parse(com.theupnextapp.common.utils.SimklConstants.SIMKL_AUTH_URL))
                                    } else {
                                        customTabsIntent.launchUrl(context, Uri.parse(TraktConstants.TRAKT_AUTH_URL))
                                    }
                                }) {
                                    Text(if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) stringResource(id = R.string.dashboard_connect_simkl_desc) else stringResource(id = R.string.dashboard_connect_trakt_desc))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(id = R.string.dashboard_most_anticipated),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    if (isLoadingMostAnticipated) {
                        ShimmerPosterCardRow()
                    } else if (!mostAnticipatedShows.isNullOrEmpty()) {
                        LazyRow {
                            items(mostAnticipatedShows.orEmpty()) { show ->
                                ListPosterCard(
                                    itemName = show.title,
                                    itemUrl = show.originalImageUrl ?: show.mediumImageUrl,
                                    modifier = Modifier.testTag("anticipated_show_card"),
                                    onClick = {
                                        val direction =
                                            Destinations.ShowDetail(
                                                source = "anticipated",
                                                showId = show.tvMazeID?.toString(),
                                                showTitle = show.title,
                                                showImageUrl = show.originalImageUrl,
                                                showBackgroundUrl = show.mediumImageUrl,
                                                imdbID = show.imdbID,
                                                isAuthorizedOnTrakt = traktAccessToken != null,
                                                showTraktId = show.traktID,
                                            )
                                        navController.navigate(direction)
                                    },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT) {
                    // Airing Soon Section
                    item {
                    Text(
                        text = stringResource(id = R.string.dashboard_airing_soon),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    if (isLoadingAiringSoon) {
                        ShimmerAiringSoon()
                    } else if (airingSoonShows.isNullOrEmpty()) {
                        EmptyState(
                            icon = Icons.Default.EventNote,
                            title = stringResource(id = R.string.dashboard_nothing_airing_soon),
                            message = stringResource(id = R.string.dashboard_nothing_airing_soon_desc),
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                        )
                    } else {
                        val pagerState = rememberPagerState(pageCount = { airingSoonShows.orEmpty().size })
                        HorizontalPager(
                            state = pagerState,
                            pageSize = PageSize.Fixed(carouselPageSize),
                            pageSpacing = 16.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) { page ->
                            val showResponse = airingSoonShows.orEmpty().toList().getOrNull(page) ?: return@HorizontalPager
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            val scale =
                                lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                                )
                            val alphaOffset =
                                lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                                )

                            val imdbId = showResponse.show?.ids?.imdb
                            val traktId = showResponse.show?.ids?.trakt
                            val season = showResponse.episode?.season
                            val number = showResponse.episode?.number
                            val uniqueKey = "$traktId-${season ?: 0}-${number ?: 0}"
                            val extractedInfo = traktId?.let { airingSoonImages[uniqueKey] }
                            val imageUrl = extractedInfo?.imageUrl
                            val tvmazeId = extractedInfo?.tvmazeId

                            val airDateTxt =
                                try {
                                    showResponse.first_aired?.let {
                                        val zonedDateTime = ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                                        val timeMillis = zonedDateTime.toInstant().toEpochMilli()
                                        DateUtils.getRelativeTimeSpanString(
                                            timeMillis,
                                            System.currentTimeMillis(),
                                            DateUtils.MINUTE_IN_MILLIS,
                                            DateUtils.FORMAT_ABBREV_RELATIVE,
                                        ).toString()
                                    } ?: "TBA"
                                } catch (e: Exception) {
                                    "TBA"
                                }

                            val episodeInfoText = "${stringResource(id = R.string.dashboard_season_episode, showResponse.episode?.season ?: 0, showResponse.episode?.number ?: 0)} • ${showResponse.episode?.title ?: stringResource(id = R.string.dashboard_tba)}"

                            Box(
                                modifier =
                                    Modifier
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            alpha = alphaOffset
                                        }
                                        .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                UpNextEpisodeCard(
                                    showTitle = showResponse.show?.title ?: "Unknown",
                                    episodeInfo = episodeInfoText,
                                    airDateRibbon = airDateTxt,
                                    imageUrl = imageUrl,
                                    modifier = Modifier.fillMaxWidth(),
                                    onCardClick = {
                                        val direction =
                                            Destinations.ShowDetail(
                                                source = "airing_soon",
                                                showId = tvmazeId?.toString(),
                                                showTitle = showResponse.show?.title,
                                                showImageUrl = imageUrl,
                                                showBackgroundUrl = null,
                                                imdbID = imdbId,
                                                isAuthorizedOnTrakt = traktAccessToken != null,
                                                showTraktId = traktId,
                                            )
                                        navController.navigate(direction)
                                    },
                                    onMarkAsWatchedClick = {
                                        if (traktAccessToken != null) {
                                            viewModel.onMarkEpisodeWatched(
                                                showTvMazeId = tvmazeId,
                                                imdbId = imdbId,
                                                showTraktId = traktId,
                                                season = season ?: 0,
                                                number = number ?: 0,
                                            )
                                        } else {
                                            // User needs to be connected to Trakt to mark as watched.
                                        }
                                    },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } // ends item {
                } else if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                    item {
                        Text(
                            text = stringResource(id = R.string.dashboard_simkl_premieres_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )

                        if (isLoadingSimklPremieres) {
                            ShimmerAiringSoon()
                        } else if (simklPremieres.isNullOrEmpty()) {
                            EmptyState(
                                icon = Icons.Default.EventNote,
                                title = stringResource(id = R.string.dashboard_simkl_upcoming_empty_title),
                                message = stringResource(id = R.string.dashboard_simkl_upcoming_empty_desc),
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                            )
                        } else {
                            val pagerState = rememberPagerState(pageCount = { simklPremieres.orEmpty().size })
                            HorizontalPager(
                                state = pagerState,
                                pageSize = PageSize.Fixed(carouselPageSize),
                                pageSpacing = 16.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) { page ->
                                val showResponse = simklPremieres.orEmpty().toList().getOrNull(page) ?: return@HorizontalPager
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                val scale =
                                    lerp(
                                        start = 0.85f,
                                        stop = 1f,
                                        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                                    )
                                val alphaOffset =
                                    lerp(
                                        start = 0.5f,
                                        stop = 1f,
                                        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                                    )

                                val simklId = showResponse.id
                                val imdbId = showResponse.imdbID
                                val imageUrl = showResponse.originalImageUrl ?: showResponse.mediumImageUrl
                                val tvmazeId = showResponse.tvMazeID

                                val airDateTxt = stringResource(id = R.string.dashboard_new_premiere)
                                val episodeInfoText = showResponse.year?.let {
                                    stringResource(id = R.string.dashboard_series_premiere) + " ($it)"
                                } ?: stringResource(id = R.string.dashboard_series_premiere)

                                Box(
                                    modifier =
                                        Modifier
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                                alpha = alphaOffset
                                            }
                                            .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    UpNextEpisodeCard(
                                        showTitle = showResponse.title ?: "Unknown",
                                        episodeInfo = episodeInfoText,
                                        airDateRibbon = airDateTxt,
                                        imageUrl = imageUrl,
                                        modifier = Modifier.fillMaxWidth(),
                                        onCardClick = {
                                            val direction =
                                                Destinations.ShowDetail(
                                                    source = "simkl_premieres",
                                                    showId = tvmazeId?.toString(),
                                                    showTitle = showResponse.title,
                                                    showImageUrl = imageUrl,
                                                    showBackgroundUrl = null,
                                                    imdbID = imdbId,
                                                    isAuthorizedOnTrakt = traktAccessToken != null,
                                                    showTraktId = null,
                                                    tvdbID = showResponse.tvdbID,
                                                    simklID = simklId?.toInt(),
                                                )
                                            navController.navigate(direction)
                                        },
                                        onMarkAsWatchedClick = {
                                            // Handle marking as watched for SIMKL later
                                        },
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } // ends if (activeProvider == PROVIDER_TRAKT) else if (...)

                // Recent Activity Section
                item {
                    Text(
                        text = stringResource(id = R.string.dashboard_recent_activity),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    if (isLoadingHistory) {
                        ShimmerPosterCardRow()
                    } else if (recentHistory.isNullOrEmpty()) {
                        EmptyState(
                            icon = Icons.Default.History,
                            title = stringResource(id = R.string.dashboard_no_recent_activity),
                            message = if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                                stringResource(id = R.string.dashboard_no_recent_activity_desc_simkl)
                            } else {
                                stringResource(id = R.string.dashboard_no_recent_activity_desc_trakt)
                            },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                        )
                    }
                }

                if (!recentHistory.isNullOrEmpty()) {
                    item {
                        // Recent Activity Carousel
                        LazyRow(
                            contentPadding = PaddingValues(end = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        ) {
                            items(recentHistory.orEmpty().take(5)) { historyItem ->
                                val traktId = historyItem.showTraktId
                                val imdbId = historyItem.showImdbId
                                val season = historyItem.season
                                val number = historyItem.number

                                val imageKey = "$traktId-${season ?: 0}-${number ?: 0}"
                                val extractedInfo = traktId?.let { historyImages[imageKey] }
                                val imageUrl = extractedInfo?.imageUrl
                                val tvmazeId = extractedInfo?.tvmazeId

                                Column(
                                    modifier =
                                        Modifier
                                            .width(160.dp)
                                            .clickable {
                                                val direction =
                                                    Destinations.EpisodeDetail(
                                                        showTraktId = traktId ?: 0,
                                                        seasonNumber = season ?: 0,
                                                        episodeNumber = number ?: 0,
                                                        showTitle = historyItem.showTitle,
                                                        showId = tvmazeId,
                                                        imdbID = imdbId,
                                                        isAuthorizedOnTrakt = true,
                                                        showImageUrl = imageUrl,
                                                        episodeImageUrl = imageUrl,
                                                    )
                                                navController.navigate(direction)
                                            },
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(MaterialTheme.shapes.medium),
                                    ) {
                                        if (imageUrl != null) {
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = historyItem.showTitle ?: stringResource(id = R.string.dashboard_show_poster_desc),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        } else {
                                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                                Icon(imageVector = Icons.Default.ImageNotSupported, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = historyItem.showTitle ?: "Unknown Show",
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                    Text(
                                        text = stringResource(id = R.string.dashboard_season_episode, historyItem.season ?: 0, historyItem.number ?: 0),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = stringResource(id = R.string.dashboard_watched),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT) {
                    // Recommended for You Section
                    item {
                    Text(
                        text = stringResource(id = R.string.dashboard_recommended_for_you),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    if (isLoadingRecommendations) {
                        ShimmerRecommended()
                    } else if (!recommendedShows.isNullOrEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { recommendedShows.orEmpty().size })
                        HorizontalPager(
                            state = pagerState,
                            pageSize = PageSize.Fixed(carouselPageSize),
                            pageSpacing = 16.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) { page ->
                            val show = recommendedShows.orEmpty().getOrNull(page) ?: return@HorizontalPager
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            val scale =
                                lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                                )
                            val alphaOffset =
                                lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                                )

                            val traktId = show?.ids?.trakt
                            val imdbId = show?.ids?.imdb
                            val extractedInfo = traktId?.let { recommendedShowsImages[it.toString()] }
                            val imageUrl = extractedInfo?.imageUrl
                            val tvmazeId = extractedInfo?.tvmazeId

                            Box(
                                modifier =
                                    Modifier
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            alpha = alphaOffset
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                Card(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(2f / 3f)
                                            .clickable {
                                                val direction =
                                                    Destinations.ShowDetail(
                                                        source = "recommended",
                                                        showId = tvmazeId?.toString(),
                                                        showTitle = show?.title,
                                                        showImageUrl = imageUrl,
                                                        showBackgroundUrl = null,
                                                        imdbID = imdbId,
                                                        isAuthorizedOnTrakt = true,
                                                        showTraktId = traktId,
                                                    )
                                                navController.navigate(direction)
                                            },
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model =
                                                ImageRequest.Builder(LocalContext.current)
                                                    .data(imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                            contentDescription = show?.title ?: stringResource(id = R.string.dashboard_show_poster_desc),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                                    .align(Alignment.BottomCenter)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                                        ),
                                                    ),
                                        )
                                        Text(
                                            text = show?.title ?: "",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier =
                                                Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                } // ends item {
                } // ends if (activeProvider == PROVIDER_TRAKT) {
            }

            if (!regionalTrendingShows.isNullOrEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.dashboard_trending_near_you),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    LazyRow {
                        items(regionalTrendingShows.orEmpty()) { show ->
                            val extractedInfo = show.traktID?.let { regionalTrendingShowsImages[it.toString()] }
                            val imageUrl = extractedInfo?.imageUrl ?: show.originalImageUrl ?: show.mediumImageUrl
                            val tvmazeId = extractedInfo?.tvmazeId ?: show.tvMazeID

                            ListPosterCard(
                                itemName = show.title,
                                itemUrl = imageUrl,
                                modifier = Modifier.testTag("regional_trending_show_card"),
                                onClick = {
                                    val direction =
                                        Destinations.ShowDetail(
                                            source = "trending_near_you",
                                            showId = tvmazeId?.toString(),
                                            showTitle = show.title,
                                            showImageUrl = imageUrl,
                                            showBackgroundUrl = show.mediumImageUrl,
                                            imdbID = show.imdbID,
                                            isAuthorizedOnTrakt = traktAccessToken != null,
                                            showTraktId = show.traktID,
                                        )
                                    navController.navigate(direction)
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
