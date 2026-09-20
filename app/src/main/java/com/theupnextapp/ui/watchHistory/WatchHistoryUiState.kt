package com.theupnextapp.ui.watchHistory

enum class WatchHistoryViewMode {
    EPISODES,
    SHOWS,
}

data class WatchHistoryShowItem(
    val showTraktId: Int,
    val showTvmazeId: Int?,
    val showImdbId: String?,
    val showTitle: String,
    val imageUrl: String?,
    val lastWatchedAt: String,
    val formattedLastWatchedAt: String,
    val episodesWatchedCount: Int,
    val latestSeasonNumber: Int,
    val latestEpisodeNumber: Int,
)

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
    val groupedShows: List<WatchHistoryShowItem> = emptyList(),
    val availableMonthYears: List<String> = emptyList(),
    val selectedMonthFilter: String? = null,
    val collapsedMonths: Set<String> = emptySet(),
    val viewMode: WatchHistoryViewMode = WatchHistoryViewMode.EPISODES,
    val searchQuery: String = "",
    val endOfListReached: Boolean = false,
    val errorMessage: String? = null,
    val totalItemCount: Int? = null,
    val loadedEpisodesCount: Int = 0,
)
