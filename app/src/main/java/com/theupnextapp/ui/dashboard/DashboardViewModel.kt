package com.theupnextapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.theupnextapp.domain.DashboardHistoryItem
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.domain.TrendingShow
import com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponse
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.ProviderManager
import com.theupnextapp.repository.SimklAuthManager
import com.theupnextapp.repository.SimklRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import com.theupnextapp.work.SimklSyncWorker
import com.theupnextapp.work.SyncWatchProgressWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ExtractedTraktInfo(
    val imageUrl: String?,
    val tvmazeId: Int?,
)

@HiltViewModel
class DashboardViewModel
@Inject
constructor(
    private val traktRepository: TraktRepository,
    private val dashboardRepository: DashboardRepository,
    private val watchProgressRepository: WatchProgressRepository,
    private val simklRepository: SimklRepository,
    private val providerManager: ProviderManager,
    private val simklAuthManager: SimklAuthManager,
    private val localWorkManager: WorkManager,
    private val firebaseAnalytics: FirebaseAnalytics,
) : ViewModel() {
    val traktAccessToken: StateFlow<TraktAccessToken?> =
        traktRepository.traktAccessToken
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val simklAccessToken: StateFlow<com.theupnextapp.domain.SimklAccessToken?> =
        simklAuthManager.simklAccessToken
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val activeProvider: StateFlow<String?> =
        providerManager.activeProvider
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val isAuthorizedOnProvider: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        providerManager.activeProvider,
        traktRepository.traktAccessToken,
        simklAuthManager.simklAccessToken
    ) { provider, traktToken, simklToken ->
        if (provider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
            simklToken != null
        } else {
            traktToken != null
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        false
    )

    private val _airingSoonShows = MutableStateFlow<NetworkTraktMyScheduleResponse?>(null)
    val airingSoonShows: StateFlow<NetworkTraktMyScheduleResponse?> = _airingSoonShows.asStateFlow()

    private val _airingSoonImages = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
    val airingSoonImages: StateFlow<Map<String, ExtractedTraktInfo>> =
        _airingSoonImages.asStateFlow()

    private val _isLoadingAiringSoon = MutableStateFlow(false)
    val isLoadingAiringSoon: StateFlow<Boolean> = _isLoadingAiringSoon.asStateFlow()

    val simklPremieres: StateFlow<List<TrendingShow>?> = simklRepository.premieresShows
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isLoadingSimklPremieres: StateFlow<Boolean> = simklRepository.isLoadingPremieres
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _recentHistory =
        MutableStateFlow<List<DashboardHistoryItem>?>(null)
    val recentHistory: StateFlow<List<DashboardHistoryItem>?> = _recentHistory.asStateFlow()

    private val _historyImages = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
    val historyImages: StateFlow<Map<String, ExtractedTraktInfo>> = _historyImages.asStateFlow()

    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory.asStateFlow()

    private val _recommendedShows =
        MutableStateFlow<NetworkTraktRecommendationsResponse?>(null)
    val recommendedShows: StateFlow<NetworkTraktRecommendationsResponse?> =
        _recommendedShows.asStateFlow()

    private val _recommendedShowsImages =
        MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
    val recommendedShowsImages: StateFlow<Map<String, ExtractedTraktInfo>> =
        _recommendedShowsImages.asStateFlow()

    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations.asStateFlow()

    val todayShows: StateFlow<List<ScheduleShow>?> =
        dashboardRepository.todayShows
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val mostAnticipatedShows: StateFlow<List<TraktMostAnticipated>?> =
        traktRepository.traktMostAnticipatedShows
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val isLoadingTodayShows = dashboardRepository.isLoadingTodayShows
    val isLoadingMostAnticipated: StateFlow<Boolean> = traktRepository.isLoadingTraktMostAnticipated

    private val _regionalTrendingShows = MutableStateFlow<List<TraktTrendingShows>?>(null)
    val regionalTrendingShows: StateFlow<List<TraktTrendingShows>?> =
        _regionalTrendingShows.asStateFlow()

    private val _regionalTrendingShowsImages =
        MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
    val regionalTrendingShowsImages: StateFlow<Map<String, ExtractedTraktInfo>> =
        _regionalTrendingShowsImages.asStateFlow()

    private val _isLoadingRegionalTrending = MutableStateFlow(false)
    val isLoadingRegionalTrending: StateFlow<Boolean> = _isLoadingRegionalTrending.asStateFlow()

    init {
        viewModelScope.launch {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "Dashboard")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "DashboardScreen")
            }

            traktRepository.refreshTraktMostAnticipatedShows(forceRefresh = false)
            dashboardRepository.refreshTodayShows(
                countryCode = "US",
                date = null,
            )
            fetchRegionalTrendingShows()
        }

        viewModelScope.launch {
            var wasSyncing = false
            watchProgressRepository.isSyncing.collect { isSyncing ->
                if (wasSyncing && !isSyncing) {
                    val provider = providerManager.activeProvider.firstOrNull() ?: com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT
                    if (provider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                        simklAuthManager.simklAccessToken.firstOrNull()?.accessToken?.let { token ->
                            fetchRecentHistory(token, provider)
                        }
                    } else {
                        traktRepository.traktAccessToken.firstOrNull()?.access_token?.let { token ->
                            fetchRecentHistory(token, provider)
                        }
                    }
                }
                wasSyncing = isSyncing
            }
        }
    }

    fun fetchDashboardData(token: String) {
        // Keeping this for backward compatibility or Trakt specific fetches
        if (_airingSoonShows.value == null && !_isLoadingAiringSoon.value) {
            fetchAiringSoonShows(token)
        }
        if (_recommendedShows.value == null && !_isLoadingRecommendations.value) {
            fetchRecommendations(token)
        }
    }

    fun fetchSimklDashboardData() {
        viewModelScope.launch {
            if (simklPremieres.value.isNullOrEmpty() && !isLoadingSimklPremieres.value) {
                simklRepository.refreshPremieres()
            }
        }
    }

    fun fetchDashboardHistoryData() {
        viewModelScope.launch {
            val provider = providerManager.activeProvider.firstOrNull() ?: com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT
            if (provider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                val token = simklAuthManager.simklAccessToken.firstOrNull()?.accessToken
                if (token != null && _recentHistory.value == null && !_isLoadingHistory.value) {
                    fetchRecentHistory(token, provider)
                }
            } else {
                val token = traktRepository.traktAccessToken.firstOrNull()?.access_token
                if (token != null && _recentHistory.value == null && !_isLoadingHistory.value) {
                    fetchRecentHistory(token, provider)
                }
            }
        }
    }

    private fun fetchAiringSoonShows(bearerToken: String) {
        viewModelScope.launch {
            _isLoadingAiringSoon.value = true
            try {
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

                val response = traktRepository.getTraktMySchedule("Bearer $bearerToken", today, 14)
                if (response.isSuccess) {
                    val shows =
                        response.getOrNull()?.let { responseList ->
                            val filtered = responseList.filter { it.episode?.season != 0 }
                            NetworkTraktMyScheduleResponse().apply {
                                addAll(filtered)
                            }
                        }
                    _airingSoonShows.value = shows
                    shows?.let { scheduleList ->
                        val deferredImages =
                            scheduleList.mapNotNull { scheduleItem ->
                                val traktId = scheduleItem.show?.ids?.trakt
                                val imdbId = scheduleItem.show?.ids?.imdb
                                val tmdbId = scheduleItem.show?.ids?.tmdb
                                val season = scheduleItem.episode?.season
                                val number = scheduleItem.episode?.number
                                if (traktId != null && imdbId != null) {
                                    async(Dispatchers.IO.limitedParallelism(5)) {
                                        try {
                                            val (url, tvmazeId) = dashboardRepository.getShowImageAndTvmazeId(
                                                imdbId = imdbId,
                                                tmdbId = tmdbId,
                                            )
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
                        val newImages = deferredImages.awaitAll().filterNotNull().toMap()
                        _airingSoonImages.value = newImages
                    }
                } else {
                    _airingSoonShows.value = null
                }
            } catch (e: Exception) {
                _airingSoonShows.value = null
            } finally {
                _isLoadingAiringSoon.value = false
            }
        }
    }

    private fun fetchRecommendations(bearerToken: String) {
        viewModelScope.launch {
            _isLoadingRecommendations.value = true
            try {
                val response = traktRepository.getTraktRecommendations(bearerToken)
                if (response.isSuccess) {
                    val shows = response.getOrNull()
                    _recommendedShows.value = shows

                    shows?.let { recommendedList ->
                        val deferredImages =
                            recommendedList.mapNotNull { item ->
                                val traktId = item.ids?.trakt
                                val imdbId = item.ids?.imdb
                                if (traktId != null && imdbId != null) {
                                    async(Dispatchers.IO.limitedParallelism(5)) {
                                        try {
                                            val (url, tvmazeId) = dashboardRepository.getShowImageAndTvmazeId(
                                                imdbId = imdbId,
                                                tmdbId = item.ids?.tmdb,
                                            )
                                            val uniqueKey = traktId.toString()
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
                        val newImages = deferredImages.awaitAll().filterNotNull().toMap()
                        _recommendedShowsImages.value = newImages
                    }
                } else {
                    _recommendedShows.value = null
                }
            } catch (e: Exception) {
                _recommendedShows.value = null
            } finally {
                _isLoadingRecommendations.value = false
            }
        }
    }

    private fun fetchRecentHistory(token: String, provider: String) {
        viewModelScope.launch {
            _isLoadingHistory.value = true
            try {
                if (provider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                    val response = simklRepository.getWatchedEpisodes(token)
                    if (response.isSuccess) {
                        val items = response.getOrNull()
                        val mappedItems = items?.flatMap { historyRes ->
                            // SIMKL doesn't return Trakt ID or TvMaze ID in its base IDs
                            val showImdbId = historyRes.show?.ids?.imdbId
                            val showTitle = historyRes.show?.title

                            historyRes.episodes?.map { ep ->
                                DashboardHistoryItem(
                                    showTraktId = null,
                                    showImdbId = showImdbId,
                                    showTvMazeId = null,
                                    season = ep.season,
                                    number = ep.episode,
                                    watchedAt = ep.watchedAt,
                                    showTitle = showTitle
                                )
                            } ?: emptyList()
                        }

                        _recentHistory.value = mappedItems
                        mappedItems?.let { historyList ->
                            val deferredImages = historyList.mapNotNull { item ->
                                val imdbId = item.showImdbId
                                if (imdbId != null) {
                                    async(Dispatchers.IO.limitedParallelism(5)) {
                                        try {
                                            val season = item.season
                                            val number = item.number
                                            val (url, tvmazeId) = if (season != null && number != null) {
                                                dashboardRepository.getEpisodeImageAndTvmazeId(imdbId, season, number)
                                            } else {
                                                dashboardRepository.getShowImageAndTvmazeId(imdbId = imdbId, tmdbId = item.showImdbId?.toIntOrNull()) // wait, showImdbId is string.
                                                // SIMKL History doesn't have tmdbId.
                                                dashboardRepository.getShowImageAndTvmazeId(imdbId, null)
                                            }
                                            val uniqueKey = "null-${season ?: 0}-${number ?: 0}"
                                            uniqueKey to ExtractedTraktInfo(imageUrl = url, tvmazeId = tvmazeId)
                                        } catch (e: Exception) { null }
                                    }
                                } else { null }
                            }
                            val newImages = deferredImages.awaitAll().filterNotNull().toMap()
                            _historyImages.value = newImages
                        }
                    } else {
                        _recentHistory.value = null
                    }
                } else {
                    val response = traktRepository.getTraktRecentHistory(token)
                    if (response.isSuccess) {
                        val items = response.getOrNull()
                        val mappedItems = items?.map {
                            DashboardHistoryItem(
                                showTraktId = it.show?.ids?.trakt,
                                showImdbId = it.show?.ids?.imdb,
                                showTvMazeId = null,
                                season = it.episode?.season,
                                number = it.episode?.number,
                                watchedAt = it.watchedAt,
                                showTitle = it.show?.title
                            )
                        }
                        _recentHistory.value = mappedItems
                        items?.let { historyList ->
                            val deferredImages =
                                historyList.mapNotNull { item ->
                                    val traktId = item.show?.ids?.trakt
                                    val imdbId = item.show?.ids?.imdb
                                    if (traktId != null && imdbId != null) {
                                        async(Dispatchers.IO.limitedParallelism(5)) {
                                            try {
                                                val season = item.episode?.season
                                                val number = item.episode?.number
                                                val (url, tvmazeId) =
                                                    if (season != null && number != null) {
                                                        dashboardRepository.getEpisodeImageAndTvmazeId(
                                                            imdbId,
                                                            season,
                                                            number,
                                                        )
                                                    } else {
                                                        dashboardRepository.getShowImageAndTvmazeId(
                                                            imdbId = imdbId,
                                                            tmdbId = item.show?.ids?.tmdb,
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
                            val newImages = deferredImages.awaitAll().filterNotNull().toMap()
                            _historyImages.value = newImages
                        }
                    } else {
                        _recentHistory.value = null
                    }
                }
            } catch (e: Exception) {
                _recentHistory.value = null
            } finally {
                _isLoadingHistory.value = false
            }
        }
    }

    private fun fetchRegionalTrendingShows() {
        viewModelScope.launch {
            _isLoadingRegionalTrending.value = true
            val countryCode = java.util.Locale.getDefault().country
            traktRepository.getRegionalTrendingShows(countryCode)
                .onSuccess { response ->
                    _regionalTrendingShows.value = response

                    val deferredImages =
                        response.mapNotNull { item ->
                            val traktId = item.traktID
                            val imdbId = item.imdbID
                            if (traktId != null && imdbId != null) {
                                async(Dispatchers.IO.limitedParallelism(5)) {
                                    try {
                                        val (url, tvmazeId) = dashboardRepository.getShowImageAndTvmazeId(
                                            imdbId = imdbId,
                                            tmdbId = item.tmdbID,
                                        )
                                        val uniqueKey = traktId.toString()
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
                    val newImages = deferredImages.awaitAll().filterNotNull().toMap()
                    _regionalTrendingShowsImages.value = newImages
                }
                .onFailure {
                    _regionalTrendingShows.value = emptyList()
                }
            _isLoadingRegionalTrending.value = false
        }
    }

    fun onMarkEpisodeWatched(
        showTvMazeId: Int?,
        imdbId: String?,
        showTraktId: Int?,
        season: Int,
        number: Int,
    ) {
        val validTraktId = showTraktId ?: return

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "episode")
            param(FirebaseAnalytics.Param.ITEM_ID, validTraktId.toString())
            param("season", season.toLong())
            param("episode", number.toLong())
        }

        viewModelScope.launch {
            watchProgressRepository.markEpisodeWatched(
                showTraktId = validTraktId,
                showTvMazeId = showTvMazeId,
                showImdbId = imdbId,
                seasonNumber = season,
                episodeNumber = number,
            )
            triggerSyncIfAuthenticated()
        }
    }

    private fun triggerSyncIfAuthenticated() {
        viewModelScope.launch {
            val provider = providerManager.activeProvider.firstOrNull() ?: com.theupnextapp.repository.ProviderManager.PROVIDER_TRAKT
            if (provider == com.theupnextapp.repository.ProviderManager.PROVIDER_SIMKL) {
                simklAuthManager.simklAccessToken.firstOrNull()?.accessToken?.let { token ->
                    val syncWork =
                        OneTimeWorkRequestBuilder<SimklSyncWorker>()
                            .setInputData(
                                Data
                                    .Builder()
                                    .putString(SimklSyncWorker.ARG_TOKEN, token)
                                    .build(),
                            ).build()
                    localWorkManager.enqueue(syncWork)
                }
            } else {
                traktRepository.traktAccessToken.firstOrNull()?.access_token?.let { token ->
                    val syncWork =
                        OneTimeWorkRequestBuilder<SyncWatchProgressWorker>()
                            .setInputData(
                                Data
                                    .Builder()
                                    .putString(SyncWatchProgressWorker.ARG_TOKEN, token)
                                    .build(),
                            ).build()
                    localWorkManager.enqueue(syncWork)
                }
            }
        }
    }
}
