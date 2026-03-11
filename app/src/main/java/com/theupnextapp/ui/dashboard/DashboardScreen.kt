package com.theupnextapp.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theupnextapp.core.designsystem.ui.widgets.ListPosterCard
import com.theupnextapp.core.designsystem.ui.widgets.UpNextEpisodeCard
import com.theupnextapp.navigation.Destinations
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val token by viewModel.traktAccessToken.collectAsState()
    val airingSoonShows by viewModel.airingSoonShows.collectAsState()
    val isLoadingAiringSoon by viewModel.isLoadingAiringSoon.collectAsState()

    val todayShows by viewModel.todayShows.collectAsState()
    val mostAnticipatedShows by viewModel.mostAnticipatedShows.collectAsState()
    val isLoadingTodayShows by viewModel.isLoadingTodayShows.observeAsState(false)
    val isLoadingMostAnticipated by viewModel.isLoadingMostAnticipated.collectAsState()

    LaunchedEffect(token) {
        token?.access_token?.let {
            viewModel.fetchAiringSoonForYou(it)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        if (token != null) {
            item {
                Text(
                    text = "My Upnext",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }

        if (token == null) {
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
                        contentPadding = PaddingValues(horizontal = 48.dp),
                        modifier = Modifier.fillMaxWidth().height(400.dp)
                    ) { page ->
                        val show = todayShows!![page]
                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val scale = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                        )
                        val alphaOffset = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = alphaOffset
                                }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = MaterialTheme.shapes.extraLarge,
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                                    .clickable {
                                        val direction = Destinations.ShowDetail(
                                            source = "today",
                                            showId = show.id.toString(),
                                            showTitle = show.name,
                                            showImageUrl = show.originalImage,
                                            showBackgroundUrl = show.mediumImage,
                                        )
                                        navController.navigate(direction)
                                    }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(show.originalImage ?: show.mediumImage)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = show.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                                )
                                            )
                                    )
                                    Text(
                                        text = show.name ?: "",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AccountBox,
                                contentDescription = "Connect Trakt",
                                modifier = Modifier.padding(bottom = 8.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Unlock your personal TV tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Connect your Trakt account to track your progress and get personalized recommendations.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            Button(onClick = { navController.navigate(Destinations.TraktAccount()) }) {
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
                                    val direction = Destinations.ShowDetail(
                                        source = "anticipated",
                                        showId = show.id.toString(),
                                        showTitle = show.title,
                                        showImageUrl = show.originalImageUrl,
                                        showBackgroundUrl = show.mediumImageUrl,
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
                    LazyRow {
                        items(airingSoonShows!!) { showResponse ->
                            UpNextEpisodeCard(
                                showTitle = showResponse.show?.title ?: "Unknown",
                                episodeSummary = "S${showResponse.episode?.season ?: 0} E${showResponse.episode?.number ?: 0} • ${showResponse.episode?.title ?: "TBA"}",
                                imageUrl = null, // Trakt schedule doesn't include images directly
                                modifier = Modifier.padding(end = 16.dp),
                                onCardClick = { /* Handle card click here to go to Show Detail */ },
                                onMarkAsWatchedClick = { /* Mark episode watched logic */ },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
