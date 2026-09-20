package com.theupnextapp.ui.watchHistory

data class WatchHistoryUiItem(
    val historyId: Long,
    val watchedAt: String,
    val formattedWatchedAt: String,
    val monthYearHeader: String,
    val showTraktId: Int,
    val showTvmazeId: Int?,
    val showImdbId: String?,
    val showTitle: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeTitle: String,
    val imageUrl: String?,
    val isWatched: Boolean = true,
)

data class WatchHistoryUiState(
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val isAuthorized: Boolean = true,
    val items: List<WatchHistoryUiItem> = emptyList(),
    val groupedItems: Map<String, List<WatchHistoryUiItem>> = emptyMap(),
    val searchQuery: String = "",
    val endOfListReached: Boolean = false,
    val errorMessage: String? = null,
)
