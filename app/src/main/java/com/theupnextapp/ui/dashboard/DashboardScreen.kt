package com.theupnextapp.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theupnextapp.core.designsystem.ui.widgets.UpNextEpisodeCard

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val token by viewModel.traktAccessToken.collectAsState()
    val airingSoonShows by viewModel.airingSoonShows.collectAsState()
    val isLoadingAiringSoon by viewModel.isLoadingAiringSoon.collectAsState()

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
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Connect your Trakt account to view your personalized dashboard.")
                }
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
