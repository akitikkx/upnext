/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 */

package com.theupnextapp.ui.episodeDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.theupnextapp.R
import com.theupnextapp.domain.EpisodeDetailArg
import com.theupnextapp.domain.TraktCast
import com.theupnextapp.domain.TraktCrew
import com.valentinilk.shimmer.shimmer
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("MagicNumber")
@Composable
fun EpisodeDetailScreen(
    episodeDetailArg: EpisodeDetailArg?,
    viewModel: EpisodeDetailViewModel = hiltViewModel(),
    navController: NavController,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.checkInStatus) {
        uiState.checkInStatus?.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearCheckInStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            val backdropUrl = episodeDetailArg?.episodeImageUrl ?: episodeDetailArg?.showBackgroundUrl ?: episodeDetailArg?.showImageUrl
            EpisodeBackdrop(backdropUrl = backdropUrl)

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.error != null -> {
                        Text(
                            text = stringResource(R.string.error_fetching_episode_details),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    uiState.episodeDetail != null -> {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                        ) {
                            Spacer(modifier = Modifier.height(if (!backdropUrl.isNullOrEmpty()) 140.dp else 16.dp))

                            EpisodeSummaryCard(
                                episodeDetailArg = episodeDetailArg,
                                episodeDetail = uiState.episodeDetail,
                                uriHandler = uriHandler,
                                isCheckingIn = uiState.isCheckingIn,
                                isCheckInSuccessful = uiState.isCheckInSuccessful,
                                onCheckInClick = { viewModel.onCheckIn() },
                                onCancelCheckInClick = { viewModel.onCancelCheckIn() },
                            )

                            if (uiState.isPeopleLoading) {
                                PersonPlaceholderRow(title = "Cast")
                                Spacer(modifier = Modifier.height(16.dp))
                                PersonPlaceholderRow(title = "Guest Stars")
                                Spacer(modifier = Modifier.height(16.dp))
                                PersonPlaceholderRow(title = "Crew")
                            } else {
                                uiState.episodePeople?.cast?.let { cast ->
                                    if (cast.isNotEmpty()) {
                                        CastRow(cast = cast)
                                    }
                                }
                                uiState.episodePeople?.guestStars?.let { guestStars ->
                                    if (guestStars.isNotEmpty()) {
                                        GuestStarsRow(guestStars = guestStars)
                                    }
                                }
                                uiState.episodePeople?.crew?.let { crew ->
                                    if (crew.isNotEmpty()) {
                                        CrewRow(crew = crew)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }

            IconButton(
                onClick = { navController.popBackStack() },
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
                    contentDescription = stringResource(id = R.string.back_arrow_content_description),
                    tint = Color.White,
                )
            }
        }
    }
}

private fun formatRelativeDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(dateString) ?: return "Aired: $dateString"
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val formattedDate = formatter.format(date)
        val relativeTime =
            android.text.format.DateUtils.getRelativeTimeSpanString(
                date.time,
                System.currentTimeMillis(),
                android.text.format.DateUtils.DAY_IN_MILLIS,
            ).toString()
        if (relativeTime == formattedDate) {
            "Aired: $formattedDate"
        } else {
            "Aired: $relativeTime ($formattedDate)"
        }
    } catch (e: Exception) {
        "Aired: $dateString"
    }
}

@Composable
fun CastRow(cast: List<TraktCast>) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = "Cast",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(cast) { star ->
                PersonItem(
                    name = star.name,
                    role = star.character,
                    originalImageUrl = star.originalImageUrl,
                )
            }
        }
    }
}

@Composable
fun GuestStarsRow(guestStars: List<TraktCast>) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = "Guest Stars",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(guestStars) { star ->
                PersonItem(
                    name = star.name,
                    role = star.character,
                    originalImageUrl = star.originalImageUrl,
                )
            }
        }
    }
}

@Composable
fun CrewRow(crew: List<TraktCrew>) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = "Crew",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(crew) { member ->
                PersonItem(
                    name = member.name,
                    role = member.job,
                    originalImageUrl = member.originalImageUrl,
                )
            }
        }
    }
}

@Composable
fun PersonItem(
    name: String?,
    role: String?,
    originalImageUrl: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp),
    ) {
        if (!originalImageUrl.isNullOrEmpty()) {
            SubcomposeAsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current)
                        .data("https://image.tmdb.org/t/p/w200$originalImageUrl")
                        .crossfade(true)
                        .build(),
                contentDescription = name,
                modifier =
                    Modifier
                        .height(100.dp)
                        .width(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                contentScale = ContentScale.Crop,
                error = {
                    Box(
                        modifier =
                            Modifier
                                .height(100.dp)
                                .width(100.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxSize(0.5f),
                        )
                    }
                },
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .height(100.dp)
                        .width(100.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxSize(0.5f),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
        role?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Suppress("MagicNumber")
@Composable
fun PersonPlaceholderRow(title: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(4) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(100.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .height(100.dp)
                                .width(100.dp)
                                .shimmer()
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier =
                            Modifier
                                .height(14.dp)
                                .fillMaxWidth(0.8f)
                                .shimmer()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier =
                            Modifier
                                .height(12.dp)
                                .fillMaxWidth(0.6f)
                                .shimmer()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }
}

@Composable
fun EpisodeBackdrop(backdropUrl: String?) {
    if (!backdropUrl.isNullOrEmpty()) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(backdropUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = "Show Backdrop",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(350.dp),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background,
                                ),
                            startY = 100f,
                        ),
                    ),
        )
    }
}

val StarRatingColor = Color(0xFFFFC107)

@Composable
fun EpisodeSummaryCard(
    episodeDetailArg: EpisodeDetailArg?,
    episodeDetail: com.theupnextapp.domain.EpisodeDetail?,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    isCheckingIn: Boolean,
    isCheckInSuccessful: Boolean,
    onCheckInClick: () -> Unit,
    onCancelCheckInClick: () -> Unit,
) {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            episodeDetailArg?.showTitle?.let { showTitle ->
                Text(
                    text = showTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                text = episodeDetail?.title ?: stringResource(id = R.string.title_unknown),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Season ${episodeDetail?.season} • Episode ${episodeDetail?.number}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )

                if (episodeDetail?.rating != null && episodeDetail.rating!! > 0.0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = StarRatingColor,
                            modifier = Modifier.padding(end = 4.dp).height(20.dp),
                        )
                        val scaledRating = ((episodeDetail.rating ?: 0.0) * 10).toInt()
                        val votes = episodeDetail.votes ?: 0
                        val votesString =
                            if (votes > 0) {
                                java.text.NumberFormat.getNumberInstance(
                                    Locale.getDefault(),
                                ).format(votes)
                            } else {
                                "0"
                            }
                        Text(
                            text = "$scaledRating% ($votesString votes)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            episodeDetail?.firstAired?.let { aired ->
                Text(
                    text = formatRelativeDate(aired),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                episodeDetail?.imdbId?.let { imdb ->
                    OutlinedButton(onClick = { uriHandler.openUri("https://www.imdb.com/title/$imdb") }) {
                        Text("IMDB")
                    }
                }
                episodeDetail?.tvdbId?.let { tvdb ->
                    OutlinedButton(onClick = { uriHandler.openUri("https://thetvdb.com/episodes/$tvdb") }) {
                        Text("TVDB")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isCheckInSuccessful) {
                OutlinedButton(
                    onClick = onCancelCheckInClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    if (isCheckingIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp).width(24.dp),
                            color = MaterialTheme.colorScheme.error,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Checked In",
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text("Cancel Check-in")
                        }
                    }
                }
            } else {
                Button(
                    onClick = onCheckInClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isCheckingIn,
                ) {
                    if (isCheckingIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp).width(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Check In to Episode on Trakt")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = episodeDetail?.overview ?: stringResource(id = R.string.no_overview_available),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
