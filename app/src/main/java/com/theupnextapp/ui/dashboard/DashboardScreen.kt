package com.theupnextapp.ui.dashboard

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
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
import com.theupnextapp.common.utils.TraktConstants
import com.theupnextapp.core.designsystem.ui.widgets.ContinueWatchingCard
import com.theupnextapp.core.designsystem.ui.widgets.ListPosterCard
import com.theupnextapp.core.designsystem.ui.widgets.UpNextEpisodeCard
import com.theupnextapp.navigation.Destinations
import kotlin.math.absoluteValue

@Suppress("LongMethod", "CyclomaticComplexMethod", "MagicNumber")
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val traktAccessToken by viewModel.traktAccessToken.collectAsStateWithLifecycle()
    val airingSoonShows by viewModel.airingSoonShows.collectAsStateWithLifecycle()
    val airingSoonImages by viewModel.airingSoonImages.collectAsStateWithLifecycle()
    val isLoadingAiringSoon by viewModel.isLoadingAiringSoon.collectAsStateWithLifecycle()

    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val playbackImages by viewModel.playbackImages.collectAsStateWithLifecycle()
    val isLoadingPlayback by viewModel.isLoadingPlayback.collectAsStateWithLifecycle()

    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val historyImages by viewModel.historyImages.collectAsStateWithLifecycle()
    val isLoadingHistory by viewModel.isLoadingHistory.collectAsStateWithLifecycle()

    val todayShows by viewModel.todayShows.collectAsState()
    val mostAnticipatedShows by viewModel.mostAnticipatedShows.collectAsState()
    val isLoadingTodayShows by viewModel.isLoadingTodayShows.observeAsState(false)
    val isLoadingMostAnticipated by viewModel.isLoadingMostAnticipated.collectAsState()

    LaunchedEffect(traktAccessToken) {
        traktAccessToken?.access_token?.let {
            viewModel.fetchDashboardData(it)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        if (traktAccessToken != null) {
            item {
                Text(
                    text = "My Upnext",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }

        if (traktAccessToken == null) {
            item {
                Text(
                    text = "Tonight on TV",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                if (isLoadingTodayShows) {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!todayShows.isNullOrEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { todayShows!!.size })
                    HorizontalPager(
                        state = pagerState,
                        pageSize = androidx.compose.foundation.pager.PageSize.Fixed(260.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        val show = todayShows!![page]
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
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AccountBox,
                                contentDescription = "Connect Trakt",
                                modifier = Modifier.padding(bottom = 8.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                "Unlock your personal TV tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                "Connect your Trakt account to track your progress and get personalized recommendations.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                            )
                            Button(onClick = {
                                val builder = CustomTabsIntent.Builder()
                                val customTabsIntent = builder.build()
                                customTabsIntent.launchUrl(context, Uri.parse(TraktConstants.TRAKT_AUTH_URL))
                            }) {
                                Text("Connect Trakt")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Most Anticipated",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                if (isLoadingMostAnticipated) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!mostAnticipatedShows.isNullOrEmpty()) {
                    LazyRow {
                        items(mostAnticipatedShows!!) { show ->
                            ListPosterCard(
                                itemName = show.title,
                                itemUrl = show.originalImageUrl ?: show.mediumImageUrl,
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
            // Continue Watching Section
            item {
                Text(
                    text = "Continue Watching",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                if (isLoadingPlayback) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (playbackProgress.isNullOrEmpty()) {
                    Text(
                        "No in-progress shows found.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                } else {
                    val pagerState = rememberPagerState(pageCount = { playbackProgress!!.size })
                    HorizontalPager(
                        state = pagerState,
                        pageSize = androidx.compose.foundation.pager.PageSize.Fixed(280.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        val item = playbackProgress!![page]
                        val traktId = item.show?.ids?.trakt
                        val imdbId = item.show?.ids?.imdb
                        val season = item.episode?.season
                        val number = item.episode?.number
                        val uniqueKey = "$traktId-${season ?: 0}-${number ?: 0}"
                        val extractedInfo = traktId?.let { playbackImages[uniqueKey] }
                        val imageUrl = extractedInfo?.imageUrl
                        val tvmazeId = extractedInfo?.tvmazeId

                        val progressVal = (item.progress ?: 0f) / 100f

                        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                            ContinueWatchingCard(
                                showTitle = item.show?.title ?: "Unknown",
                                episodeTitle = item.episode?.title ?: "Episode ${item.episode?.number ?: 0}",
                                seasonNumber = item.episode?.season ?: 0,
                                episodeNumber = item.episode?.number ?: 0,
                                imageUrl = imageUrl,
                                progressPercentage = progressVal,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f),
                                onClick = {
                                    val direction =
                                        Destinations.ShowDetail(
                                            source = "continue_watching",
                                            showId = tvmazeId?.toString(),
                                            showTitle = item.show?.title,
                                            showImageUrl = imageUrl,
                                            showBackgroundUrl = null,
                                            imdbID = imdbId,
                                            isAuthorizedOnTrakt = true,
                                            showTraktId = traktId,
                                        )
                                    navController.navigate(direction)
                                },
                            )
                            Text(
                                text = "${item.show?.title ?: "Unknown"} • S${item.episode?.season ?: 0} E${item.episode?.number ?: 0}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Airing Soon Section
            item {
                Text(
                    text = "Airing Soon",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                if (isLoadingAiringSoon) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (airingSoonShows.isNullOrEmpty()) {
                    Text("No shows airing soon.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    val pagerState = rememberPagerState(pageCount = { airingSoonShows!!.size })
                    HorizontalPager(
                        state = pagerState,
                        pageSize = androidx.compose.foundation.pager.PageSize.Fixed(260.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        val showResponse = airingSoonShows!!.toList()[page]
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
                                    val format =
                                        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
                                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                                        }
                                    val parsed = format.parse(it)
                                    val timeMillis = parsed?.time ?: System.currentTimeMillis()
                                    android.text.format.DateUtils.getRelativeTimeSpanString(
                                        timeMillis,
                                        System.currentTimeMillis(),
                                        android.text.format.DateUtils.MINUTE_IN_MILLIS,
                                        android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE,
                                    ).toString()
                                } ?: "TBA"
                            } catch (e: Exception) {
                                "TBA"
                            }

                        val episodeInfoText = "S${showResponse.episode?.season ?: 0} E${showResponse.episode?.number ?: 0} • ${showResponse.episode?.title ?: "TBA"}"

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
                                onMarkAsWatchedClick = { /* Mark episode watched logic */ },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent Activity Section
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                if (isLoadingHistory) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (recentHistory.isNullOrEmpty()) {
                    Text(
                        "No recent activity found.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
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
                        items(recentHistory!!.take(5)) { historyItem ->
                            val traktId = historyItem.show?.ids?.trakt
                            val imdbId = historyItem.show?.ids?.imdb
                            val season = historyItem.episode?.season
                            val number = historyItem.episode?.number
                            val uniqueKey = "$traktId-${season ?: 0}-${number ?: 0}"
                            val extractedInfo = traktId?.let { historyImages[uniqueKey] }
                            val imageUrl = extractedInfo?.imageUrl
                            val tvmazeId = extractedInfo?.tvmazeId

                            Column(
                                modifier =
                                    Modifier
                                        .width(160.dp)
                                        .clickable {
                                            val direction =
                                                Destinations.ShowSeasonEpisodes(
                                                    showId = tvmazeId,
                                                    seasonNumber = historyItem.episode?.season,
                                                    imdbID = imdbId,
                                                    isAuthorizedOnTrakt = true,
                                                    showTraktId = traktId,
                                                )
                                            navController.navigate(direction)
                                        },
                            ) {
                                AsyncImage(
                                    model =
                                        ImageRequest.Builder(LocalContext.current)
                                            .data(imageUrl)
                                            .crossfade(true)
                                            .build(),
                                    contentDescription = historyItem.show?.title ?: "Show Poster",
                                    contentScale = ContentScale.Crop,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f / 9f)
                                            .clip(RoundedCornerShape(8.dp)),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = historyItem.show?.title ?: "Unknown Show",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "Season ${historyItem.episode?.season ?: 0} • Episode ${historyItem.episode?.number ?: 0}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "Watched",
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
        }
    }
}
