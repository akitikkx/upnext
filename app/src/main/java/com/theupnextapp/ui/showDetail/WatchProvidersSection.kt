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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.theupnextapp.R
import com.theupnextapp.core.designsystem.ui.components.SectionHeadingText
import com.theupnextapp.domain.TmdbWatchProvider
import com.theupnextapp.domain.TmdbWatchProviders
import com.valentinilk.shimmer.shimmer
import java.util.Locale

@Composable
fun WatchProvidersSection(
    uiState: ShowDetailViewModel.ShowDetailUiState,
    modifier: Modifier = Modifier,
) {
    val watchProviders = uiState.watchProviders
    val isLoading = uiState.isWatchProvidersLoading
    val providers = watchProviders?.providers ?: emptyList()
    val countryCode = watchProviders?.countryCode ?: Locale.getDefault().country.ifBlank { "US" }
    val uriHandler = LocalUriHandler.current

    var selectedTier by remember { mutableStateOf<String?>(null) }

    val filteredProviders =
        when (selectedTier) {
            "Stream" -> watchProviders?.flatrateProviders ?: emptyList()
            "Free" -> watchProviders?.freeProviders ?: emptyList()
            "Buy" -> watchProviders?.buyRentProviders ?: emptyList()
            else -> providers
        }

    if (isLoading) {
        WatchProvidersShimmer(modifier = modifier)
    } else if (providers.isNotEmpty()) {
        Column(
            modifier =
                modifier.padding(
                    top = dimensionResource(id = R.dimen.padding_standard_double),
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            WatchProvidersHeader(
                countryCode = countryCode,
                justWatchLink = watchProviders?.link,
                onJustWatchClick = {
                    watchProviders?.link?.let { uriHandler.openUri(it) }
                },
            )

            WatchProvidersTierFilterRow(
                totalCount = providers.size,
                flatrateCount = watchProviders?.flatrateProviders?.size ?: 0,
                freeCount = watchProviders?.freeProviders?.size ?: 0,
                buyRentCount = watchProviders?.buyRentProviders?.size ?: 0,
                selectedTier = selectedTier,
                onSelectTier = { selectedTier = it },
            )

            WatchProvidersList(
                providers = filteredProviders,
                onProviderClick = {
                    watchProviders?.link?.let { uriHandler.openUri(it) }
                },
            )
        }
    } else if (watchProviders != null) {
        WatchProvidersEmptySection(
            countryCode = countryCode,
            justWatchLink = watchProviders.link,
            onJustWatchClick = {
                watchProviders.link?.let { uriHandler.openUri(it) }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun WatchProvidersHeader(
    countryCode: String,
    justWatchLink: String?,
    onJustWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SectionHeadingText(
            text = stringResource(id = R.string.show_detail_where_to_watch),
            modifier = Modifier.padding(horizontal = 0.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = countryCode.uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }

            if (!justWatchLink.isNullOrBlank()) {
                Text(
                    text = stringResource(id = R.string.watch_providers_powered_by_justwatch),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier =
                        Modifier
                            .clickable { onJustWatchClick() }
                            .padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun WatchProvidersTierFilterRow(
    totalCount: Int,
    flatrateCount: Int,
    freeCount: Int,
    buyRentCount: Int,
    selectedTier: String?,
    onSelectTier: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tierCategoriesPresent =
        (if (flatrateCount > 0) 1 else 0) +
            (if (freeCount > 0) 1 else 0) +
            (if (buyRentCount > 0) 1 else 0)
    if (tierCategoriesPresent <= 1) return

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedTier == null,
            onClick = { onSelectTier(null) },
            label = {
                Text("${stringResource(id = R.string.watch_providers_tier_all)} ($totalCount)")
            },
        )
        if (flatrateCount > 0) {
            FilterChip(
                selected = selectedTier == "Stream",
                onClick = { onSelectTier(if (selectedTier == "Stream") null else "Stream") },
                label = {
                    Text("${stringResource(id = R.string.watch_providers_tier_stream)} ($flatrateCount)")
                },
            )
        }
        if (freeCount > 0) {
            FilterChip(
                selected = selectedTier == "Free",
                onClick = { onSelectTier(if (selectedTier == "Free") null else "Free") },
                label = {
                    Text("${stringResource(id = R.string.watch_providers_tier_free)} ($freeCount)")
                },
            )
        }
        if (buyRentCount > 0) {
            FilterChip(
                selected = selectedTier == "Buy",
                onClick = { onSelectTier(if (selectedTier == "Buy") null else "Buy") },
                label = {
                    Text("${stringResource(id = R.string.watch_providers_tier_buy)} ($buyRentCount)")
                },
            )
        }
    }
}

@Composable
private fun WatchProvidersList(
    providers: List<TmdbWatchProvider>,
    onProviderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("watch_providers_list")
                .padding(
                    horizontal = 16.dp,
                    vertical = dimensionResource(id = R.dimen.padding_standard),
                ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(providers) { provider ->
            WatchProviderCard(
                provider = provider,
                onClick = onProviderClick,
            )
        }
    }
}

@Composable
private fun WatchProvidersEmptySection(
    countryCode: String,
    justWatchLink: String?,
    onJustWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = dimensionResource(id = R.dimen.padding_standard_double)),
    ) {
        SectionHeadingText(
            text = stringResource(id = R.string.show_detail_where_to_watch),
            modifier = Modifier.padding(horizontal = 0.dp),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("watch_providers_empty"),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.watch_providers_not_available, countryCode.uppercase(Locale.ROOT)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!justWatchLink.isNullOrBlank()) {
                        TextButton(
                            onClick = onJustWatchClick,
                            modifier = Modifier.padding(top = 4.dp),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(
                                text = stringResource(id = R.string.watch_providers_check_justwatch),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchProviderCard(
    provider: TmdbWatchProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w200${provider.logoUrl}",
            contentDescription = provider.name,
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = provider.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(2.dp))

        val (badgeBgColor, badgeTextColor) =
            when (provider.tier) {
                "Stream" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                "Free", "Free with Ads" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
            }

        Surface(
            shape = RoundedCornerShape(4.dp),
            color = badgeBgColor,
        ) {
            Text(
                text = provider.tier,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = badgeTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
fun WatchProvidersShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier.padding(
                top = dimensionResource(id = R.dimen.padding_standard_double),
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        SectionHeadingText(text = stringResource(id = R.string.show_detail_where_to_watch))
        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("watch_providers_loading")
                    .padding(
                        horizontal = 16.dp,
                        vertical = dimensionResource(id = R.dimen.padding_standard),
                    ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(5) {
                Column(
                    modifier = Modifier.width(80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmer()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier =
                            Modifier
                                .width(60.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier =
                            Modifier
                                .width(40.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }
}
