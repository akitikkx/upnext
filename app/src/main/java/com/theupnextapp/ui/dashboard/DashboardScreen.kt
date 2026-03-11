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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val token by viewModel.traktAccessToken.collectAsState()
    val airingSoonShows by viewModel.airingSoonShows.collectAsState()
    val isLoadingAiringSoon by viewModel.isLoadingAiringSoon.collectAsState()

    val trendingShows by viewModel.trendingShows.collectAsState()
    val popularShows by viewModel.popularShows.collectAsState()
    val isLoadingTrending by viewModel.isLoadingTrending.collectAsState()
    val isLoadingPopular by viewModel.isLoadingPopular.collectAsState()

    LaunchedEffect(token) {
        token?.access_token?.let {
            viewModel.fetchAiringSoonForYou(it)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                text = "My Upnext",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        if (token == null) {
            item {
                Text(
                    text = "Trending Shows",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                if (isLoadingTrending) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!trendingShows.isNullOrEmpty()) {
                    LazyRow {
                        items(trendingShows!!) { show ->
                            ListPosterCard(
                                itemName = show.title,
                                itemUrl = show.originalImageUrl ?: show.mediumImageUrl,
                                onClick = {
                                    val direction = Destinations.ShowDetail(
                                        source = "trending",
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
                    text = "Popular Shows",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                if (isLoadingPopular) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!popularShows.isNullOrEmpty()) {
                    LazyRow {
                        items(popularShows!!) { show ->
                            ListPosterCard(
                                itemName = show.title,
                                itemUrl = show.originalImageUrl ?: show.mediumImageUrl,
                                onClick = {
                                    val direction = Destinations.ShowDetail(
                                        source = "popular",
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
