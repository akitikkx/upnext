/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.traktAccount

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.theupnextapp.R
import com.theupnextapp.core.designsystem.ui.components.SectionHeadingText
import com.theupnextapp.domain.TraktUserListItem
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.foundation.rememberScrollState as rememberHorizontalScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun WatchlistListContent(
    watchlistItems: List<TraktUserListItem>,
    watchlistSearchQuery: String,
    watchlistSortOption: WatchlistSortOption,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (WatchlistSortOption) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    statusFilter: String? = null,
    availableStatuses: List<String> = emptyList(),
    totalWatchlistCount: Int = 0,
    onStatusFilterChange: (String?) -> Unit = {},
    header: @Composable () -> Unit = {},
    onItemClick: (item: TraktUserListItem) -> Unit,
    onRemoveItem: (item: TraktUserListItem) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var isSearchVisible by remember { mutableStateOf(false) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 5
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight().widthIn(max = 600.dp).testTag("watchlist_column"),
            state = lazyListState,
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                end = contentPadding.calculateEndPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                header()
            }

            item {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        SectionHeadingText(
                            modifier = Modifier.weight(1f),
                            text =
                                if (statusFilter != null || watchlistSearchQuery.isNotBlank()) {
                                    "${stringResource(id = R.string.title_favorites_list)} (${watchlistItems.size} of $totalWatchlistCount)"
                                } else {
                                    stringResource(id = R.string.title_favorites_list)
                                },
                        )
                        Row(modifier = Modifier.padding(end = 16.dp)) {
                            IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Watchlist",
                                )
                            }
                            Box {
                                IconButton(onClick = { isSortMenuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Sort/Filter Watchlist",
                                    )
                                }
                                DropdownMenu(
                                    expanded = isSortMenuExpanded,
                                    onDismissRequest = { isSortMenuExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(id = R.string.watchlist_sort_recently_added),
                                                fontWeight = if (watchlistSortOption == WatchlistSortOption.ADDED) FontWeight.Bold else null,
                                            )
                                        },
                                        onClick = {
                                            onSortOptionChange(WatchlistSortOption.ADDED)
                                            isSortMenuExpanded = false
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(id = R.string.watchlist_sort_title),
                                                fontWeight = if (watchlistSortOption == WatchlistSortOption.TITLE) FontWeight.Bold else null,
                                            )
                                        },
                                        onClick = {
                                            onSortOptionChange(WatchlistSortOption.TITLE)
                                            isSortMenuExpanded = false
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(id = R.string.watchlist_sort_release_year),
                                                fontWeight = if (watchlistSortOption == WatchlistSortOption.RELEASE_YEAR) FontWeight.Bold else null,
                                            )
                                        },
                                        onClick = {
                                            onSortOptionChange(WatchlistSortOption.RELEASE_YEAR)
                                            isSortMenuExpanded = false
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(id = R.string.watchlist_sort_rating),
                                                fontWeight = if (watchlistSortOption == WatchlistSortOption.RATING) FontWeight.Bold else null,
                                            )
                                        },
                                        onClick = {
                                            onSortOptionChange(WatchlistSortOption.RATING)
                                            isSortMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.watchlist_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp),
                    )

                    AnimatedVisibility(visible = isSearchVisible) {
                        OutlinedTextField(
                            value = watchlistSearchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 12.dp),
                            placeholder = { Text(stringResource(id = R.string.watchlist_search_placeholder)) },
                            singleLine = true,
                            trailingIcon = {
                                if (watchlistSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                    }
                                }
                            },
                        )
                    }

                    // Status filter chips
                    if (availableStatuses.isNotEmpty()) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 8.dp)
                                    .horizontalScroll(rememberHorizontalScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = statusFilter == null,
                                onClick = { onStatusFilterChange(null) },
                                label = { Text(stringResource(id = R.string.watchlist_filter_all)) },
                            )
                            availableStatuses.forEach { status ->
                                FilterChip(
                                    selected = statusFilter == status,
                                    onClick = {
                                        onStatusFilterChange(
                                            if (statusFilter == status) null else status,
                                        )
                                    },
                                    label = { Text(status) },
                                )
                            }
                        }
                    }
                }
            }

            itemsIndexed(
                items = watchlistItems,
                key = { _, item -> item.traktID ?: item.id ?: item.hashCode() },
            ) { index, watchlistItem ->
                val dismissState =
                    rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                onRemoveItem(watchlistItem)
                                true
                            } else {
                                false
                            }
                        },
                    )

                // Educational Peek Animation for the first item
                val peekDelayMillis = 800L
                val peekSlideOffset = -80f
                val peekReturnDelayMillis = 600L
                var peekOffset by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
                val animatedPeekOffset by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = peekOffset,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 400),
                    label = "peekAnimation",
                )

                if (index == 0) {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(peekDelayMillis)
                        peekOffset = peekSlideOffset // Slide left
                        kotlinx.coroutines.delay(peekReturnDelayMillis)
                        peekOffset = 0f // Slide back
                    }
                }

                SwipeToDismissBox(
                    modifier = Modifier.animateItem().fillMaxWidth().padding(horizontal = 16.dp),
                    state = dismissState,
                    backgroundContent = {
                        val isPeeking = animatedPeekOffset < -10f
                        val color =
                            when {
                                dismissState.targetValue != SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.error
                                isPeeking -> MaterialTheme.colorScheme.error
                                else -> Color.Transparent
                            }
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color)
                                    .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (dismissState.targetValue != SwipeToDismissBoxValue.Settled || isPeeking) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.watchlist_remove_content_desc),
                                    tint = MaterialTheme.colorScheme.onError,
                                )
                            }
                        }
                    },
                ) {
                    Box(modifier = Modifier.offset(x = animatedPeekOffset.dp)) {
                        WatchlistListItemCard(
                            item = watchlistItem,
                            onClick = { onItemClick(watchlistItem) },
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp + contentPadding.calculateBottomPadding())) }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = stringResource(id = R.string.scroll_to_top),
                )
            }
        }
    }
}

@Composable
fun WatchlistListItemCard(
    item: TraktUserListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .height(140.dp)
                .minimumInteractiveComponentSize()
                .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Poster Image
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(Color.DarkGray),
            ) {
                if (item.originalImageUrl != null) {
                    AsyncImage(
                        model =
                            ImageRequest.Builder(LocalContext.current)
                                .data(item.originalImageUrl)
                                .crossfade(true)
                                .build(),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = stringResource(id = R.string.watchlist_no_image_content_desc),
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(32.dp),
                        tint = Color.LightGray,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Show Details
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.title ?: stringResource(id = R.string.title_unknown),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))

                val yearText = if (item.year != null && item.year != "0") item.year.toString() else ""
                val networkText = item.network ?: ""

                val topMeta = listOf(yearText, networkText).filter { it.isNotEmpty() }.joinToString(" • ")
                if (topMeta.isNotEmpty()) {
                    Text(
                        text = topMeta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val statusText = item.status ?: ""
                val rating = item.rating
                val ratingText = if (rating != null && rating > 0.0) String.format(Locale.getDefault(), "★ %.1f", rating) else ""

                val bottomMeta = listOf(statusText, ratingText).filter { it.isNotEmpty() }.joinToString(" • ")
                if (bottomMeta.isNotEmpty()) {
                    Text(
                        text = bottomMeta,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
