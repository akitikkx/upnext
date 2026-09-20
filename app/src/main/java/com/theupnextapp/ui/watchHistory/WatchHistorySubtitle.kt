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

package com.theupnextapp.ui.watchHistory

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.theupnextapp.R

@Composable
fun WatchHistorySubtitle(
    uiState: WatchHistoryUiState,
    modifier: Modifier = Modifier,
) {
    if (!uiState.isAuthorized || (uiState.loadedEpisodesCount == 0 && uiState.totalItemCount == null)) {
        return
    }

    val subtitleText = when (uiState.viewMode) {
        WatchHistoryViewMode.EPISODES -> {
            val total = uiState.totalItemCount
            val loaded = uiState.loadedEpisodesCount
            val displayed = uiState.items.size
            val isFiltered = uiState.selectedMonthFilter != null || uiState.searchQuery.isNotBlank()

            when {
                total != null && total > loaded -> {
                    if (isFiltered) {
                        stringResource(
                            R.string.watch_history_subtitle_filtered_paginated,
                            displayed,
                            loaded,
                            total,
                        )
                    } else {
                        stringResource(
                            R.string.watch_history_subtitle_paginated,
                            loaded,
                            total,
                        )
                    }
                }
                total != null -> {
                    if (isFiltered) {
                        stringResource(
                            R.string.watch_history_subtitle_filtered_all,
                            displayed,
                            total,
                        )
                    } else {
                        stringResource(
                            R.string.watch_history_subtitle_all,
                            total,
                        )
                    }
                }
                else -> {
                    stringResource(
                        R.string.watch_history_subtitle_loaded,
                        if (isFiltered) displayed else loaded,
                    )
                }
            }
        }
        WatchHistoryViewMode.SHOWS -> {
            val total = uiState.totalItemCount
            val loaded = uiState.loadedEpisodesCount
            val showsCount = uiState.groupedShows.size

            when {
                total != null && total > loaded -> {
                    stringResource(
                        R.string.watch_history_shows_subtitle_paginated,
                        showsCount,
                        loaded,
                        total,
                    )
                }
                total != null -> {
                    stringResource(
                        R.string.watch_history_shows_subtitle_all,
                        showsCount,
                        total,
                    )
                }
                else -> {
                    stringResource(
                        R.string.watch_history_shows_subtitle_loaded,
                        showsCount,
                        loaded,
                    )
                }
            }
        }
    }

    Text(
        text = subtitleText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.testTag("watch_history_subtitle"),
    )
}
