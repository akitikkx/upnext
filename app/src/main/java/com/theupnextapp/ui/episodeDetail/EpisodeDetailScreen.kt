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

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.UriHandler
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
import com.theupnextapp.core.designsystem.ui.components.CastMember
import com.theupnextapp.domain.EpisodeDetail
import com.theupnextapp.domain.EpisodeDetailArg
import com.theupnextapp.domain.TraktCast
import com.theupnextapp.domain.TraktCrew
import com.theupnextapp.navigation.Destinations
import com.valentinilk.shimmer.shimmer
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("MagicNumber")
@Composable
fun EpisodeDetailScreen(
    episodeDetailArg: EpisodeDetailArg?,
    viewModel: EpisodeDetailViewModel = hiltViewModel(),
    navController: NavController,
    onNavigateToShowDetail: (EpisodeDetailArg) -> Unit = {},
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
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(modifier = Modifier.height(if (!backdropUrl.isNullOrEmpty()) 140.dp else 16.dp))

                            EpisodeSummaryCard(
                                episodeDetailArg = episodeDetailArg,
                                episodeDetail = uiState.episodeDetail,
                                uriHandler = uriHandler,
                                isCheckingIn = uiState.isCheckingIn,
                                isCheckInSuccessful = uiState.isCheckInSuccessful,
                                isAuthorizedOnProvider = uiState.isAuthorizedOnProvider,
                                activeProvider = uiState.activeProvider,
                                onCheckInClick = { viewModel.onCheckIn() },
                                onCancelCheckInClick = { viewModel.onCancelCheckIn() },
                                onNavigateToShowDetail = onNavigateToShowDetail,
                            )

                            if (uiState.isPeopleLoading) {
                                PersonPlaceholderRow(title = stringResource(id = R.string.show_detail_cast_list))
                                Spacer(modifier = Modifier.height(16.dp))
                                PersonPlaceholderRow(title = stringResource(id = R.string.episode_detail_guest_stars))
                                Spacer(modifier = Modifier.height(16.dp))
                                PersonPlaceholderRow(title = stringResource(id = R.string.episode_detail_crew))
                            } else {
                                uiState.episodePeople?.cast?.let { cast ->
                                    if (cast.isNotEmpty()) {
                                        CastRow(cast = cast) {
                                            val traktId = it.traktId?.toString()
                                            val name = it.name
                                            if (traktId != null && !name.isNullOrEmpty()) {
                                                navController.navigate(Destinations.PersonDetail(traktId, name, it.originalImageUrl))
                                            }
                                        }
                                    }
                                }
                                uiState.episodePeople?.guestStars?.let { guestStars ->
                                    if (guestStars.isNotEmpty()) {
                                        GuestStarsRow(guestStars = guestStars) {
                                            val traktId = it.traktId?.toString()
                                            val name = it.name
                                            if (traktId != null && !name.isNullOrEmpty()) {
                                                navController.navigate(Destinations.PersonDetail(traktId, name, it.originalImageUrl))
                                            }
                                        }
                                    }
                                }
                                uiState.episodePeople?.crew?.let { crew ->
                                    if (crew.isNotEmpty()) {
                                        CrewRow(crew = crew) {
                                            val traktId = it.traktId?.toString()
                                            val name = it.name
                                            if (traktId != null && !name.isNullOrEmpty()) {
                                                navController.navigate(Destinations.PersonDetail(traktId, name, it.originalImageUrl))
                                            }
                                        }
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

private fun formatRelativeDate(context: android.content.Context, dateString: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val timeMillis = zonedDateTime.toInstant().toEpochMilli()
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val formattedDate = zonedDateTime.format(formatter)
        val relativeTime =
            DateUtils.getRelativeTimeSpanString(
                timeMillis,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS,
            ).toString()
        if (relativeTime == formattedDate) {
            context.getString(R.string.episode_detail_aired_date, formattedDate)
        } else {
            context.getString(R.string.episode_detail_aired_relative_date, relativeTime, formattedDate)
        }
    } catch (e: Exception) {
        context.getString(R.string.episode_detail_aired_date, dateString)
    }
}

@Composable
fun CastRow(cast: List<TraktCast>, onPersonClick: (TraktCast) -> Unit) {
    Column(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = stringResource(id = R.string.show_detail_cast_list),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(cast) { star ->
                CastMember(
                    name = star.name,
                    role = star.character,
                    originalImageUrl = star.originalImageUrl,
                    onClick = { onPersonClick(star) }
                )
            }
        }
    }
}

@Composable
fun GuestStarsRow(guestStars: List<TraktCast>, onPersonClick: (TraktCast) -> Unit) {
    Column(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = stringResource(id = R.string.episode_detail_guest_stars),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(guestStars) { star ->
                CastMember(
                    name = star.name,
                    role = star.character,
                    originalImageUrl = star.originalImageUrl,
                    onClick = { onPersonClick(star) }
                )
            }
        }
    }
}

@Composable
fun CrewRow(crew: List<TraktCrew>, onPersonClick: (TraktCrew) -> Unit) {
    Column(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = stringResource(id = R.string.episode_detail_crew),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(crew) { member ->
                CastMember(
                    name = member.name,
                    role = member.job,
                    originalImageUrl = member.originalImageUrl,
                    onClick = { onPersonClick(member) }
                )
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
fun PersonPlaceholderRow(title: String) {
    Column(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth().padding(bottom = 16.dp)) {
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
            contentDescription = stringResource(id = R.string.episode_detail_show_backdrop),
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
    episodeDetail: EpisodeDetail?,
    uriHandler: UriHandler,
    isCheckingIn: Boolean,
    isCheckInSuccessful: Boolean,
    isAuthorizedOnProvider: Boolean,
    activeProvider: String,
    onCheckInClick: () -> Unit,
    onCancelCheckInClick: () -> Unit,
    onNavigateToShowDetail: (EpisodeDetailArg) -> Unit = {},
) {
    ElevatedCard(
        modifier =
            Modifier
                .widthIn(max = 840.dp)
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
                    modifier =
                        Modifier.clickable {
                            episodeDetailArg.let { arg -> onNavigateToShowDetail(arg) }
                        },
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
                    text = stringResource(id = R.string.episode_detail_season_episode, episodeDetail?.season?.toString() ?: "", episodeDetail?.number?.toString() ?: ""),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
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
                                NumberFormat.getNumberInstance(
                                    Locale.getDefault(),
                                ).format(votes)
                            } else {
                                "0"
                            }
                        Text(
                            text = stringResource(id = R.string.episode_detail_rating_format, scaledRating, votesString),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            episodeDetail?.firstAired?.let { aired ->
                Text(
                    text = formatRelativeDate(LocalContext.current, aired),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                episodeDetail?.imdbId?.let { imdb ->
                    OutlinedButton(onClick = { uriHandler.openUri("https://www.imdb.com/title/$imdb") }) {
                        Text(stringResource(id = R.string.episode_detail_imdb))
                    }
                }
                episodeDetail?.tvdbId?.let { tvdb ->
                    OutlinedButton(onClick = { uriHandler.openUri("https://thetvdb.com/episodes/$tvdb") }) {
                        Text(stringResource(id = R.string.episode_detail_tvdb))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isAuthorizedOnProvider && activeProvider == com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT) {
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
                                    contentDescription = stringResource(id = R.string.show_detail_show_season_episode_trakt_check_in),
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                                Text(stringResource(id = R.string.episode_detail_cancel_checkin))
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
                            Text(stringResource(id = R.string.episode_detail_checkin))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = stringResource(id = R.string.episode_detail_overview),
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
