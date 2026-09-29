package com.theupnextapp.ui.watchHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.domain.WatchHistoryItem
import com.theupnextapp.domain.WatchHistorySyncResult
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.TraktRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WatchHistoryViewModel
@Inject
constructor(
    private val traktRepository: TraktRepository,
    private val dashboardRepository: DashboardRepository,
    private val traktAuthManager: TraktAuthManager,
    private val firebaseAnalytics: FirebaseAnalytics,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _viewMode = MutableStateFlow(WatchHistoryViewMode.EPISODES)
    val viewMode: StateFlow<WatchHistoryViewMode> = _viewMode.asStateFlow()

    private val _selectedMonthFilter = MutableStateFlow<String?>(null)
    val selectedMonthFilter: StateFlow<String?> = _selectedMonthFilter.asStateFlow()

    private val _collapsedMonths = MutableStateFlow<Set<String>>(emptySet())
    val collapsedMonths: StateFlow<Set<String>> = _collapsedMonths.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingNextPage = MutableStateFlow(false)
    val isLoadingNextPage: StateFlow<Boolean> = _isLoadingNextPage.asStateFlow()

    private val _endOfListReached = MutableStateFlow(false)
    val endOfListReached: StateFlow<Boolean> = _endOfListReached.asStateFlow()

    private val _totalItemCount = MutableStateFlow<Int?>(null)
    val totalItemCount: StateFlow<Int?> = _totalItemCount.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isAuthorized: StateFlow<Boolean> =
        traktAuthManager.traktAuthState
            .map { it == TraktAuthState.LoggedIn }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

    private data class StatusState(
        val isLoading: Boolean,
        val isLoadingNextPage: Boolean,
        val isAuthorized: Boolean,
        val endOfListReached: Boolean,
        val errorMessage: String?,
    )

    private val statusStateFlow =
        combine(
            _isLoading,
            _isLoadingNextPage,
            isAuthorized,
            _endOfListReached,
            _errorMessage,
        ) { loading, loadingNext, authorized, endReached, error ->
            StatusState(
                isLoading = loading,
                isLoadingNextPage = loadingNext,
                isAuthorized = authorized,
                endOfListReached = endReached,
                errorMessage = error,
            )
        }

    private data class FilterState(
        val query: String,
        val viewMode: WatchHistoryViewMode,
        val selectedMonth: String?,
        val collapsedMonths: Set<String>,
    )

    private val filterStateFlow =
        combine(
            _searchQuery,
            _viewMode,
            _selectedMonthFilter,
            _collapsedMonths,
        ) { query, viewMode, selectedMonth, collapsed ->
            FilterState(
                query = query,
                viewMode = viewMode,
                selectedMonth = selectedMonth,
                collapsedMonths = collapsed,
            )
        }

    val uiState: StateFlow<WatchHistoryUiState> =
        combine(
            traktRepository.watchHistory,
            filterStateFlow,
            statusStateFlow,
            _totalItemCount,
        ) { historyItems, filter, status, totalCount ->
            val allUiItems = historyItems.map { item ->
                val (monthYearHeader, formattedDate) = formatWatchedDate(item.watchedAt)
                WatchHistoryUiItem(
                    historyId = item.historyId,
                    watchedAt = item.watchedAt,
                    formattedWatchedAt = formattedDate,
                    monthYearHeader = monthYearHeader,
                    showTraktId = item.showTraktId,
                    showTvmazeId = item.showTvmazeId,
                    showImdbId = item.showImdbId,
                    showTitle = item.showTitle,
                    seasonNumber = item.seasonNumber,
                    episodeNumber = item.episodeNumber,
                    episodeTitle = item.episodeTitle,
                    imageUrl = item.episodeImageUrl ?: item.showPosterUrl,
                    isWatched = true,
                )
            }

            val availableMonths = allUiItems.map { it.monthYearHeader }.distinct()

            val monthFilteredEpisodes = if (filter.selectedMonth != null) {
                allUiItems.filter { it.monthYearHeader == filter.selectedMonth }
            } else {
                allUiItems
            }

            val searchFilteredEpisodes = if (filter.query.isNotBlank()) {
                monthFilteredEpisodes.filter {
                    it.showTitle.contains(filter.query, ignoreCase = true) ||
                        it.episodeTitle.contains(filter.query, ignoreCase = true)
                }
            } else {
                monthFilteredEpisodes
            }

            val groupedEpisodes = searchFilteredEpisodes.groupBy { it.monthYearHeader }

            val showsBaseEpisodes = if (filter.selectedMonth != null) {
                allUiItems.filter { it.monthYearHeader == filter.selectedMonth }
            } else {
                allUiItems
            }

            val groupedShowsList = showsBaseEpisodes
                .groupBy { it.showTraktId }
                .mapNotNull { (showTraktId, episodes) ->
                    val latest = episodes.maxByOrNull { it.watchedAt } ?: return@mapNotNull null
                    val showPoster = latest.imageUrl
                    WatchHistoryShowItem(
                        showTraktId = showTraktId,
                        showTvmazeId = latest.showTvmazeId,
                        showImdbId = latest.showImdbId,
                        showTitle = latest.showTitle,
                        imageUrl = showPoster,
                        lastWatchedAt = latest.watchedAt,
                        formattedLastWatchedAt = latest.formattedWatchedAt,
                        episodesWatchedCount = episodes.size,
                        latestSeasonNumber = latest.seasonNumber,
                        latestEpisodeNumber = latest.episodeNumber,
                    )
                }
                .sortedByDescending { it.lastWatchedAt }

            val searchFilteredShows = if (filter.query.isNotBlank()) {
                groupedShowsList.filter {
                    it.showTitle.contains(filter.query, ignoreCase = true)
                }
            } else {
                groupedShowsList
            }

            WatchHistoryUiState(
                isLoading = status.isLoading,
                isLoadingNextPage = status.isLoadingNextPage,
                isAuthorized = status.isAuthorized,
                items = searchFilteredEpisodes,
                groupedItems = groupedEpisodes,
                groupedShows = searchFilteredShows,
                availableMonthYears = availableMonths,
                selectedMonthFilter = filter.selectedMonth,
                collapsedMonths = filter.collapsedMonths,
                viewMode = filter.viewMode,
                searchQuery = filter.query,
                endOfListReached = status.endOfListReached,
                errorMessage = status.errorMessage,
                totalItemCount = totalCount ?: historyItems.size,
                loadedEpisodesCount = historyItems.size,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WatchHistoryUiState(),
        )

    init {
        viewModelScope.launch {
            traktAuthManager.traktAuthState.collect { state ->
                if (state == TraktAuthState.LoggedIn) {
                    loadFirstPage()
                }
            }
        }
    }

    fun loadFirstPage() {
        if (_isLoading.value) return
        _endOfListReached.value = false
        _errorMessage.value = null
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = traktRepository.syncWatchHistory(forceFull = false)
                if (result.isSuccess) {
                    val syncResult = result.getOrNull()
                    syncResult?.totalItemCount?.let { _totalItemCount.value = it }
                    if (syncResult != null && syncResult.itemsFetchedCount < PAGE_LIMIT) {
                        _endOfListReached.value = true
                    }
                    firebaseAnalytics.logEvent("watch_history_page_loaded") {
                        param("page", 1L)
                        param("item_count", (syncResult?.itemsFetchedCount ?: 0).toLong())
                    }
                } else {
                    _errorMessage.value =
                        result.exceptionOrNull()?.message ?: "Failed to load watch history"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load watch history"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        if (_isLoading.value || _isLoadingNextPage.value || _endOfListReached.value) return
        viewModelScope.launch {
            _isLoadingNextPage.value = true
            try {
                val result = traktRepository.loadOlderWatchHistory()
                if (result.isSuccess) {
                    val syncResult = result.getOrNull()
                    syncResult?.totalItemCount?.let { _totalItemCount.value = it }
                    if (syncResult != null && (syncResult.itemsFetchedCount == 0 || syncResult.itemsFetchedCount < PAGE_LIMIT)) {
                        _endOfListReached.value = true
                    }
                } else {
                    _errorMessage.value =
                        result.exceptionOrNull()?.message ?: "Failed to load older watch history"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load older watch history"
            } finally {
                _isLoadingNextPage.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            firebaseAnalytics.logEvent("watch_history_search") {
                param(FirebaseAnalytics.Param.SEARCH_TERM, query)
            }
        }
    }

    fun onViewModeChange(mode: WatchHistoryViewMode) {
        _viewMode.value = mode
        firebaseAnalytics.logEvent("watch_history_view_mode_changed") {
            param("view_mode", mode.name)
        }
    }

    fun onMonthFilterChange(month: String?) {
        _selectedMonthFilter.value = month
        if (month != null) {
            firebaseAnalytics.logEvent("watch_history_month_filter_selected") {
                param("month", month)
            }
        }
    }

    fun onToggleMonthCollapse(month: String) {
        _collapsedMonths.update { current ->
            if (current.contains(month)) {
                current - month
            } else {
                current + month
            }
        }
    }

    private fun formatWatchedDate(dateString: String?): Pair<String, String> {
        if (dateString.isNullOrEmpty()) {
            return Pair("Unknown Date", "")
        }
        return try {
            val zonedDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
            val header = zonedDateTime.format(monthYearFormatter)
            val formatted = zonedDateTime.format(dateTimeFormatter)
            Pair(header, formatted)
        } catch (e: Exception) {
            Pair("Unknown Date", dateString)
        }
    }

    companion object {
        const val PAGE_LIMIT = 30
    }
}
