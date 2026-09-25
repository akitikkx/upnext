package com.theupnextapp.ui.watchHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.ExtractedTraktInfo
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.TraktRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
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

    private var currentPage = 1
    private var fetchImagesJob: Job? = null

    private val _historyRawItems = MutableStateFlow<List<NetworkTraktHistoryResponse>>(emptyList())
    private val _historyImages = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
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
            _historyRawItems,
            _historyImages,
            filterStateFlow,
            statusStateFlow,
            _totalItemCount,
        ) { rawItems, images, filter, status, totalCount ->
            val allUiItems = rawItems.mapNotNull { item ->
                val traktId = item.show?.ids?.trakt ?: return@mapNotNull null
                val season = item.episode?.season ?: 0
                val number = item.episode?.number ?: 0
                val uniqueKey = "$traktId-$season-$number"
                val extractedInfo = images[uniqueKey]
                val (monthYearHeader, formattedDate) = formatWatchedDate(item.watchedAt)
                WatchHistoryUiItem(
                    historyId = item.id ?: 0L,
                    watchedAt = item.watchedAt.orEmpty(),
                    formattedWatchedAt = formattedDate,
                    monthYearHeader = monthYearHeader,
                    showTraktId = traktId,
                    showTvmazeId = extractedInfo?.tvmazeId,
                    showImdbId = item.show?.ids?.imdb,
                    showTitle = item.show?.title.orEmpty(),
                    seasonNumber = season,
                    episodeNumber = number,
                    episodeTitle = item.episode?.title.orEmpty(),
                    imageUrl = extractedInfo?.imageUrl,
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
                    val showPoster = images["$showTraktId-show"]?.imageUrl ?: latest.imageUrl
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
                totalItemCount = totalCount,
                loadedEpisodesCount = rawItems.size,
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
        currentPage = 1
        _endOfListReached.value = false
        _errorMessage.value = null
        fetchHistory(page = 1, isNextPage = false)
    }

    fun loadNextPage() {
        if (_isLoading.value || _isLoadingNextPage.value || _endOfListReached.value) return
        val nextPage = currentPage + 1
        fetchHistory(page = nextPage, isNextPage = true)
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

    private fun fetchHistory(page: Int, isNextPage: Boolean) {
        viewModelScope.launch {
            if (isNextPage) {
                _isLoadingNextPage.value = true
            } else {
                _isLoading.value = true
            }
            try {
                val token = traktRepository.traktAccessToken.firstOrNull()?.access_token
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Not authorized with Trakt"
                    return@launch
                }
                val response = traktRepository.getTraktRecentHistory(
                    token = token,
                    page = page,
                    limit = PAGE_LIMIT,
                )
                if (response.isSuccess) {
                    val historyPage = response.getOrNull()
                    val newItems = historyPage?.items.orEmpty()
                    historyPage?.totalItemCount?.let { _totalItemCount.value = it }
                    if (newItems.isEmpty() || newItems.size < PAGE_LIMIT) {
                        _endOfListReached.value = true
                    }
                    if (isNextPage) {
                        _historyRawItems.value = _historyRawItems.value + newItems
                    } else {
                        _historyRawItems.value = newItems
                    }
                    currentPage = page
                    firebaseAnalytics.logEvent("watch_history_page_loaded") {
                        param("page", page.toLong())
                        param("item_count", newItems.size.toLong())
                    }
                    fetchImages(newItems)
                } else {
                    _errorMessage.value =
                        response.exceptionOrNull()?.message ?: "Failed to load watch history"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load watch history"
            } finally {
                if (isNextPage) {
                    _isLoadingNextPage.value = false
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun fetchImages(items: List<NetworkTraktHistoryResponse>) {
        fetchImagesJob?.cancel()
        fetchImagesJob = viewModelScope.launch(Dispatchers.IO) {
            val distinctShows =
                items.mapNotNull { item ->
                    val traktId = item.show?.ids?.trakt
                    val imdbId = item.show?.ids?.imdb
                    if (traktId != null && imdbId != null) traktId to imdbId else null
                }.distinctBy { it.first }

            val deferredShowImages =
                distinctShows.map { (traktId, imdbId) ->
                    async(Dispatchers.IO.limitedParallelism(2)) {
                        try {
                            val (url, tvmazeId) = dashboardRepository.getShowImageAndTvmazeId(imdbId)
                            "$traktId-show" to ExtractedTraktInfo(
                                imageUrl = url,
                                tvmazeId = tvmazeId,
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

            val showImages = deferredShowImages.awaitAll().filterNotNull().toMap()
            _historyImages.value = _historyImages.value + showImages

            val deferredEpisodeImages =
                items.mapNotNull { item ->
                    val traktId = item.show?.ids?.trakt
                    val imdbId = item.show?.ids?.imdb
                    val season = item.episode?.season
                    val number = item.episode?.number
                    if (traktId != null && imdbId != null) {
                        async(Dispatchers.IO.limitedParallelism(2)) {
                            try {
                                val (url, tvmazeId) =
                                    if (season != null && number != null) {
                                        dashboardRepository.getEpisodeImageAndTvmazeId(
                                            imdbId,
                                            season,
                                            number,
                                        )
                                    } else {
                                        dashboardRepository.getShowImageAndTvmazeId(
                                            imdbId,
                                        )
                                    }
                                val uniqueKey = "$traktId-${season ?: 0}-${number ?: 0}"
                                uniqueKey to ExtractedTraktInfo(
                                    imageUrl = url,
                                    tvmazeId = tvmazeId,
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                    } else {
                        null
                    }
                }

            val episodeImages = deferredEpisodeImages.awaitAll().filterNotNull().toMap()
            _historyImages.value = _historyImages.value + episodeImages
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

    override fun onCleared() {
        super.onCleared()
        fetchImagesJob?.cancel()
    }

    companion object {
        const val PAGE_LIMIT = 30
    }
}
