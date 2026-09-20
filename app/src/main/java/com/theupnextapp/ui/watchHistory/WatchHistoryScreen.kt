@file:OptIn(ExperimentalFoundationApi::class)

package com.theupnextapp.ui.watchHistory

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.theupnextapp.R
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.ui.components.EmptyState

@Composable
fun WatchHistoryScreen(
    viewModel: WatchHistoryViewModel = hiltViewModel(),
    onNavigate: (Destinations) -> Unit,
    @Suppress("UnusedParameter") onBack: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WatchHistoryContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onViewModeChange = viewModel::onViewModeChange,
        onMonthFilterChange = viewModel::onMonthFilterChange,
        onToggleMonthCollapse = viewModel::onToggleMonthCollapse,
        onLoadNextPage = viewModel::loadNextPage,
        onRetry = viewModel::loadFirstPage,
        onItemClick = { item ->
            val direction =
                Destinations.EpisodeDetail(
                    showTraktId = item.showTraktId,
                    seasonNumber = item.seasonNumber,
                    episodeNumber = item.episodeNumber,
                    showTitle = item.showTitle,
                    showId = item.showTvmazeId,
                    imdbID = item.showImdbId,
                    isAuthorizedOnTrakt = true,
                    showImageUrl = item.imageUrl,
                    episodeImageUrl = item.imageUrl,
                    isWatched = true,
                )
            onNavigate(direction)
        },
        onShowClick = { show ->
            val direction =
                Destinations.ShowDetail(
                    showId = show.showTvmazeId?.toString(),
                    showTitle = show.showTitle,
                    showImageUrl = show.imageUrl,
                    showBackgroundUrl = show.imageUrl,
                    imdbID = show.showImdbId,
                    isAuthorizedOnTrakt = true,
                    showTraktId = show.showTraktId,
                )
            onNavigate(direction)
        },
        modifier = Modifier.padding(contentPadding),
    )
}

@Composable
fun WatchHistoryContent(
    uiState: WatchHistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onViewModeChange: (WatchHistoryViewMode) -> Unit = {},
    onMonthFilterChange: (String?) -> Unit = {},
    onToggleMonthCollapse: (String) -> Unit = {},
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onItemClick: (WatchHistoryUiItem) -> Unit,
    onShowClick: (WatchHistoryShowItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, uiState.items.size, uiState.groupedShows.size, uiState.viewMode) {
        val totalCount =
            if (uiState.viewMode == WatchHistoryViewMode.EPISODES) {
                uiState.items.size
            } else {
                uiState.groupedShows.size
            }
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= totalCount - 4) {
                    onLoadNextPage()
                }
            }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
    ) {
        WatchHistoryTopControls(
            uiState = uiState,
            onSearchQueryChange = onSearchQueryChange,
            onViewModeChange = onViewModeChange,
            onMonthFilterChange = onMonthFilterChange,
        )

        val isListEmpty =
            if (uiState.viewMode == WatchHistoryViewMode.EPISODES) {
                uiState.items.isEmpty()
            } else {
                uiState.groupedShows.isEmpty()
            }

        if (!uiState.isAuthorized) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.dashboard_unlock_personal_tracker),
                    message = stringResource(R.string.trakt_connect_benefits),
                    modifier = Modifier.padding(16.dp).testTag("unauthorized_state"),
                )
            }
        } else if (uiState.isLoading && isListEmpty) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.testTag("history_loading_indicator"))
            }
        } else if (uiState.errorMessage != null && isListEmpty) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.explore_empty_retry_button))
                    }
                }
            }
        } else if (isListEmpty && uiState.searchQuery.isNotBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.watch_history_no_search_results, uiState.searchQuery),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp).testTag("no_search_results_text"),
                )
            }
        } else if (isListEmpty) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.watch_history_empty_title),
                    message = stringResource(R.string.watch_history_empty_subtitle),
                    modifier = Modifier.padding(16.dp).testTag("empty_history_state"),
                )
            }
        } else if (uiState.viewMode == WatchHistoryViewMode.SHOWS) {
            WatchHistoryShowsList(
                uiState = uiState,
                listState = listState,
                onShowClick = onShowClick,
            )
        } else {
            WatchHistoryEpisodesList(
                uiState = uiState,
                listState = listState,
                onItemClick = onItemClick,
                onToggleMonthCollapse = onToggleMonthCollapse,
            )
        }
    }
}

@Composable
private fun WatchHistoryTopControls(
    uiState: WatchHistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onViewModeChange: (WatchHistoryViewMode) -> Unit,
    onMonthFilterChange: (String?) -> Unit,
) {
    OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
            Text(stringResource(R.string.watch_history_search_placeholder))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.nav_title_search),
            )
        },
        trailingIcon = {
            if (uiState.searchQuery.isNotBlank()) {
                IconButton(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.testTag("clear_search_button"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp)
                .testTag("watch_history_search_input"),
    )

    SingleChoiceSegmentedButtonRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("watch_history_view_mode_toggle"),
    ) {
        SegmentedButton(
            selected = uiState.viewMode == WatchHistoryViewMode.EPISODES,
            onClick = { onViewModeChange(WatchHistoryViewMode.EPISODES) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = {
                SegmentedButtonDefaults.Icon(active = uiState.viewMode == WatchHistoryViewMode.EPISODES) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                    )
                }
            },
            label = { Text(stringResource(R.string.watch_history_view_episodes)) },
            modifier = Modifier.testTag("view_mode_episodes"),
        )
        SegmentedButton(
            selected = uiState.viewMode == WatchHistoryViewMode.SHOWS,
            onClick = { onViewModeChange(WatchHistoryViewMode.SHOWS) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = {
                SegmentedButtonDefaults.Icon(active = uiState.viewMode == WatchHistoryViewMode.SHOWS) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                    )
                }
            },
            label = { Text(stringResource(R.string.watch_history_view_shows)) },
            modifier = Modifier.testTag("view_mode_shows"),
        )
    }

    if (uiState.availableMonthYears.isNotEmpty()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState())
                    .testTag("watch_history_month_chips"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = uiState.selectedMonthFilter == null,
                onClick = { onMonthFilterChange(null) },
                label = { Text(stringResource(R.string.watchlist_filter_all)) },
                modifier = Modifier.testTag("month_chip_all"),
            )
            uiState.availableMonthYears.forEach { monthYear ->
                FilterChip(
                    selected = uiState.selectedMonthFilter == monthYear,
                    onClick = {
                        onMonthFilterChange(if (uiState.selectedMonthFilter == monthYear) null else monthYear)
                    },
                    label = { Text(monthYear) },
                    modifier = Modifier.testTag("month_chip_$monthYear"),
                )
            }
        }
    }
}

@Composable
private fun WatchHistoryShowsList(
    uiState: WatchHistoryUiState,
    listState: LazyListState,
    onShowClick: (WatchHistoryShowItem) -> Unit,
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().testTag("watch_history_shows_list"),
    ) {
        items(
            items = uiState.groupedShows,
            key = { it.showTraktId },
        ) { show ->
            WatchHistoryShowCard(
                item = show,
                onClick = { onShowClick(show) },
                modifier = Modifier.testTag("watch_history_show_${show.showTraktId}"),
            )
        }

        if (uiState.isLoadingNextPage) {
            item {
                WatchHistoryLoadingIndicator()
            }
        }
    }
}

@Composable
private fun WatchHistoryEpisodesList(
    uiState: WatchHistoryUiState,
    listState: LazyListState,
    onItemClick: (WatchHistoryUiItem) -> Unit,
    onToggleMonthCollapse: (String) -> Unit,
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().testTag("watch_history_list"),
    ) {
        uiState.groupedItems.forEach { (monthYearHeader, groupItems) ->
            val isCollapsed = uiState.collapsedMonths.contains(monthYearHeader)
            stickyHeader(key = monthYearHeader) {
                WatchHistoryMonthHeader(
                    title = monthYearHeader,
                    itemCount = groupItems.size,
                    isCollapsed = isCollapsed,
                    onToggleCollapse = { onToggleMonthCollapse(monthYearHeader) },
                    modifier = Modifier.testTag("month_header_$monthYearHeader"),
                )
            }

            if (!isCollapsed) {
                items(
                    items = groupItems,
                    key = { "${it.historyId}_${it.showTraktId}_${it.seasonNumber}_${it.episodeNumber}" },
                ) { item ->
                    WatchHistoryItemCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.testTag("watch_history_item_${item.historyId}"),
                    )
                }
            }
        }

        if (uiState.isLoadingNextPage) {
            item {
                WatchHistoryLoadingIndicator()
            }
        }
    }
}

@Composable
private fun WatchHistoryLoadingIndicator() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.watch_history_load_more),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun WatchHistoryMonthHeader(
    title: String,
    modifier: Modifier = Modifier,
    itemCount: Int? = null,
    isCollapsed: Boolean = false,
    onToggleCollapse: (() -> Unit)? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (onToggleCollapse != null) {
                        Modifier.clickable { onToggleCollapse() }
                    } else {
                        Modifier
                    },
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (itemCount != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = itemCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            if (onToggleCollapse != null) {
                Icon(
                    imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun WatchHistoryItemCard(
    item: WatchHistoryUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(44.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(6.dp)),
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.showTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.showTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                val epText =
                    if (item.episodeTitle.isNotBlank()) {
                        "S${item.seasonNumber}E${item.episodeNumber} • ${item.episodeTitle}"
                    } else {
                        "S${item.seasonNumber}E${item.episodeNumber}"
                    }
                Text(
                    text = epText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.formattedWatchedAt.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.formattedWatchedAt,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WatchHistoryShowCard(
    item: WatchHistoryShowItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(48.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(6.dp)),
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.showTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.showTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val countText =
                    if (item.episodesWatchedCount == 1) {
                        stringResource(R.string.watch_history_single_episode_count)
                    } else {
                        stringResource(R.string.watch_history_episodes_count, item.episodesWatchedCount)
                    }
                Text(
                    text = countText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.formattedLastWatchedAt.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.watch_history_last_watched, item.formattedLastWatchedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
