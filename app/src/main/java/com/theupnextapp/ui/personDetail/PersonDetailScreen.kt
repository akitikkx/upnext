/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
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

package com.theupnextapp.ui.personDetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.theupnextapp.domain.PersonDetailArg
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.ui.personDetail.PersonDetailViewModel.PersonCreditUiModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val HERO_IMAGE_ALPHA = 0.6f
private const val BIO_LENGTH_THRESHOLD = 250

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personDetailArg: PersonDetailArg,
    navController: NavController,
    viewModel: PersonDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateToShowDetail by viewModel.navigateToShowDetail.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(message = errorMsg)
            viewModel.clearErrorMessage()
        }
    }

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

    LaunchedEffect(personDetailArg.personId) {
        if (uiState.personSummary == null) {
            viewModel.getPersonDetails(personDetailArg.personId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back",
                                tint = Color.White,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.Transparent,
                        ),
                )
            },
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(), // Edge to edge Hero rendering
                ) {
                    item {
                        PersonProfileHeader(
                            personDetailArg = personDetailArg,
                            personSummary = uiState.personSummary,
                        )
                    }

                    item {
                        uiState.personSummary?.biography?.takeIf { it.isNotBlank() }?.let { bio ->
                            var isBioExpanded by remember { mutableStateOf(false) }

                            Column(
                                modifier =
                                    Modifier
                                        .padding(16.dp)
                                        .animateContentSize(),
                            ) {
                                Text(
                                    text = "Biography",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )

                                // Dim the explicit Wikipedia boilerplate citation inherently attached to descriptions
                                val parts = bio.split("Description above from the Wikipedia article")

                                Text(
                                    text = parts[0].trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = if (isBioExpanded) Int.MAX_VALUE else 4,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                if (parts.size > 1 && isBioExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Description above from the Wikipedia article${parts[1]}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    )
                                }

                                // If text is somewhat long, show the interaction toggle
                                if (parts[0].length > BIO_LENGTH_THRESHOLD || parts.size > 1) {
                                    Text(
                                        text = if (isBioExpanded) "Show less" else "Read more",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable { isBioExpanded = !isBioExpanded }
                                                .padding(vertical = 12.dp),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.personImages.isNotEmpty()) {
                        item {
                            PersonImageStrip(
                                personImages = uiState.personImages,
                                onImageClick = { index ->
                                    selectedImageIndex = index
                                },
                            )
                        }
                    }

                    if (uiState.personCredits.isNotEmpty()) {
                        item {
                            PersonFilmographyStrip(
                                personCredits = uiState.personCredits,
                                onCreditClicked = { imdbId, title, traktId ->
                                    viewModel.onCreditClicked(
                                        imdbId = imdbId,
                                        title = title,
                                        traktId = traktId,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        PersonImageGalleryOverlay(
            selectedImageIndex = selectedImageIndex,
            personImages = uiState.personImages,
            onClose = { selectedImageIndex = null },
        )
    }
}

@Composable
fun PersonImageGalleryOverlay(
    selectedImageIndex: Int?,
    personImages: List<com.theupnextapp.network.models.tmdb.NetworkTmdbPersonProfile>,
    onClose: () -> Unit,
) {
    val showGallery = selectedImageIndex != null
    AnimatedVisibility(
        visible = showGallery,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
    ) {
        val initialPage = selectedImageIndex ?: 0
        val pagerState =
            rememberPagerState(
                initialPage = initialPage,
            ) {
                personImages.size
            }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f)),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val image = personImages.getOrNull(page)
                val imageUrl = image?.file_path?.let { "https://image.tmdb.org/t/p/w780$it" }
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                    contentDescription = "Person Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            IconButton(
                onClick = onClose,
                modifier =
                    Modifier
                        .padding(top = 48.dp, end = 16.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Gallery",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
fun PersonProfileHeader(
    personDetailArg: PersonDetailArg,
    personSummary: com.theupnextapp.network.models.trakt.NetworkTraktPersonResponse?,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Blurred Background Hero
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(personDetailArg.personImageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .blur(radius = 16.dp),
        )

        // Dark Scrim Overlay
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.background,
                                ),
                        ),
                    ),
        )

        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .padding(horizontal = 16.dp),
        ) {
            // Rectangular Poster Overlay
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier =
                    Modifier
                        .width(160.dp)
                        .aspectRatio(2f / 3f),
            ) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(personDetailArg.personImageUrl)
                            .crossfade(true)
                            .build(),
                    contentDescription = personDetailArg.personName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = personDetailArg.personName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Metadata cluster
            personSummary?.let { summary ->
                Spacer(modifier = Modifier.height(8.dp))

                val primaryMeta =
                    listOfNotNull(
                        summary.known_for_department.takeIf { !it.isNullOrBlank() }?.let { "Known for $it" },
                        summary.birthday?.let {
                            try {
                                val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

                                val date = parseFormat.parse(it)
                                val cal = Calendar.getInstance().apply { date?.let { d -> time = d } }
                                val birthYear = cal.get(Calendar.YEAR)

                                val ageOrLifespan =
                                    if (summary.death != null) {
                                        val deathDate = parseFormat.parse(summary.death!!)
                                        val deathCal = Calendar.getInstance().apply { deathDate?.let { d -> time = d } }
                                        "$birthYear - ${deathCal.get(Calendar.YEAR)}"
                                    } else {
                                        val currentCal = Calendar.getInstance()
                                        var age = currentCal.get(Calendar.YEAR) - birthYear
                                        if (currentCal.get(Calendar.DAY_OF_YEAR) < cal.get(Calendar.DAY_OF_YEAR)) {
                                            age--
                                        }
                                        "Age $age"
                                    }

                                val formattedDate = date?.let { d -> displayFormat.format(d) } ?: it
                                "$formattedDate ($ageOrLifespan)"
                            } catch (e: Exception) {
                                it
                            }
                        },
                    ).joinToString(" • ")

                if (primaryMeta.isNotEmpty()) {
                    Text(
                        text = primaryMeta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                summary.birthplace.takeIf { !it.isNullOrBlank() }?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun PersonImageStrip(
    personImages: List<com.theupnextapp.network.models.tmdb.NetworkTmdbPersonProfile>,
    onImageClick: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
        Text(
            text = "Photos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(personImages.size) { index ->
                val image = personImages[index]
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier =
                        Modifier
                            .width(120.dp)
                            .aspectRatio(2f / 3f)
                            .clickable { onImageClick(index) },
                ) {
                    val imageUrl = image.file_path?.let { "https://image.tmdb.org/t/p/w500$it" }
                    AsyncImage(
                        model =
                            ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                        contentDescription = "Person Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
fun PersonFilmographyStrip(
    personCredits: List<PersonCreditUiModel>,
    onCreditClicked: (String?, String, Int?) -> Unit,
) {
    Text(
        text = "Filmography",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        items(personCredits) { credit ->
            val isClickable = !credit.imdbId.isNullOrEmpty()

            Column(
                modifier =
                    Modifier
                        .width(130.dp)
                        .alpha(if (isClickable) 1f else 0.5f)
                        .clickable(enabled = isClickable) {
                            onCreditClicked(
                                credit.imdbId,
                                credit.title,
                                credit.traktId,
                            )
                        },
            ) {
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f),
                ) {
                    if (!credit.posterUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model =
                                ImageRequest.Builder(LocalContext.current)
                                    .data(credit.posterUrl)
                                    .crossfade(true)
                                    .build(),
                            contentDescription = credit.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = credit.title,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp),
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = credit.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                val subtitle =
                    listOfNotNull(
                        credit.year.takeIf { it.isNotBlank() },
                        credit.character.takeIf { it.isNotBlank() },
                    ).joinToString(" • ")

                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(48.dp))
}
