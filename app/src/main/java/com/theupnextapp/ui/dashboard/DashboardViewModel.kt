package com.theupnextapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
    ) : ViewModel() {
        val traktAccessToken: StateFlow<TraktAccessToken?> =
            traktRepository.traktAccessToken
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null,
                )

        private val _airingSoonShows = MutableStateFlow<NetworkTraktMyScheduleResponse?>(null)
        val airingSoonShows: StateFlow<NetworkTraktMyScheduleResponse?> = _airingSoonShows.asStateFlow()

        private val _airingSoonImages = MutableStateFlow<Map<Int, ExtractedTraktInfo>>(emptyMap())
        val airingSoonImages: StateFlow<Map<Int, ExtractedTraktInfo>> = _airingSoonImages.asStateFlow()

        private val _isLoadingAiringSoon = MutableStateFlow(false)
        val isLoadingAiringSoon: StateFlow<Boolean> = _isLoadingAiringSoon.asStateFlow()

        private val _playbackProgress =
            MutableStateFlow<List<com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse>?>(null)
        val playbackProgress: StateFlow<List<com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse>?> = _playbackProgress.asStateFlow()

        private val _playbackImages = MutableStateFlow<Map<Int, ExtractedTraktInfo>>(emptyMap())
        val playbackImages: StateFlow<Map<Int, ExtractedTraktInfo>> = _playbackImages.asStateFlow()

        private val _isLoadingPlayback = MutableStateFlow(false)
        val isLoadingPlayback: StateFlow<Boolean> = _isLoadingPlayback.asStateFlow()

        private val _recentHistory =
            MutableStateFlow<List<com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse>?>(null)
        val recentHistory: StateFlow<List<com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse>?> = _recentHistory.asStateFlow()

        private val _historyImages = MutableStateFlow<Map<Int, ExtractedTraktInfo>>(emptyMap())
        val historyImages: StateFlow<Map<Int, ExtractedTraktInfo>> = _historyImages.asStateFlow()

        private val _isLoadingHistory = MutableStateFlow(false)
        val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory.asStateFlow()

        val todayShows: StateFlow<List<com.theupnextapp.domain.ScheduleShow>?> =
            dashboardRepository.todayShows
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null,
                )

        val mostAnticipatedShows: StateFlow<List<com.theupnextapp.domain.TraktMostAnticipated>?> =
            traktRepository.traktMostAnticipatedShows
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null,
                )

        val isLoadingTodayShows = dashboardRepository.isLoadingTodayShows
        val isLoadingMostAnticipated: StateFlow<Boolean> = traktRepository.isLoadingTraktMostAnticipated

        init {
            viewModelScope.launch {
                traktRepository.refreshTraktMostAnticipatedShows(forceRefresh = false)
                dashboardRepository.refreshTodayShows(
                    countryCode = "US",
                    date = null,
                )
            }
        }

        fun fetchDashboardData(token: String) {
            val bearerToken = token
            fetchAiringSoonShows(bearerToken)
            fetchContinueWatching(bearerToken)
            fetchRecentHistory(bearerToken)
        }

        private fun fetchAiringSoonShows(bearerToken: String) {
            viewModelScope.launch {
                _isLoadingAiringSoon.value = true
                try {
                    val cal = Calendar.getInstance()
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val today = format.format(cal.time)

                    val response = traktRepository.getTraktMySchedule("Bearer $bearerToken", today, 14)
                    if (response.isSuccess) {
                        val shows =
                            response.getOrNull()?.let { responseList ->
                                val filtered = responseList.filter { it.episode?.season != 0 }
                                com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse().apply {
                                    addAll(filtered)
                                }
                            }
                        _airingSoonShows.value = shows
                        shows?.let { scheduleList ->
                            val deferredImages =
                                scheduleList.mapNotNull { scheduleItem ->
                                    val traktId = scheduleItem.show?.ids?.trakt
                                    val imdbId = scheduleItem.show?.ids?.imdb
                                    if (traktId != null && imdbId != null) {
                                        async {
                                            try {
                                                val (url, tvmazeId) = dashboardRepository.getShowImageAndTvmazeId(imdbId)
                                                traktId to ExtractedTraktInfo(imageUrl = url, tvmazeId = tvmazeId)
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

        private fun fetchContinueWatching(bearerToken: String) {
            viewModelScope.launch {
                _isLoadingPlayback.value = true
                try {
                    val historyResponse = traktRepository.getTraktRecentHistory(bearerToken)
                    if (historyResponse.isSuccess) {
                        val historyItems = historyResponse.getOrNull() ?: emptyList()
                        val uniqueShows = historyItems.distinctBy { it.show?.ids?.trakt }.take(10)

                        val deferredProgress =
                            uniqueShows.mapNotNull { showItem ->
                                showItem.show?.ids?.trakt?.let { traktId ->
                                    async {
                                        try {
                                            val progressResult = traktRepository.getTraktShowProgress(bearerToken, traktId.toString())
                                            val progress = progressResult.getOrNull()

                                            if (progress != null && progress.nextEpisode != null) {
                                                val computedProgress =
                                                    if ((progress.aired ?: 0) > 0 && progress.completed != null) {
                                                        (progress.completed!!.toFloat() / progress.aired!!.toFloat()) * 100f
                                                    } else {
                                                        0f
                                                    }

                                                val nextEp = progress.nextEpisode
                                                val playbackItem =
                                                    com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse(
                                                        progress = computedProgress,
                                                        action = "watch",
                                                        type = "episode",
                                                        show = showItem.show,
                                                        episode = nextEp,
                                                    )
                                                Pair(playbackItem, Pair(showItem.show?.ids?.imdb, nextEp))
                                            } else {
                                                null
                                            }
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                }
                            }

                        val progressResults = deferredProgress.awaitAll().filterNotNull()
                        val playbackItems = progressResults.map { it.first }
                        _playbackProgress.value = playbackItems

                        val deferredImages =
                            progressResults.mapNotNull { (playbackItem, info) ->
                                val traktId = playbackItem.show?.ids?.trakt
                                val imdbId = info.first
                                val nextEp = info.second
                                if (traktId != null && imdbId != null) {
                                    async {
                                        try {
                                            val season = nextEp?.season
                                            val number = nextEp?.number
                                            val (url, tvmazeId) =
                                                if (season != null && number != null) {
                                                    dashboardRepository.getEpisodeImageAndTvmazeId(imdbId, season, number)
                                                } else {
                                                    dashboardRepository.getShowImageAndTvmazeId(imdbId)
                                                }
                                            traktId to ExtractedTraktInfo(imageUrl = url, tvmazeId = tvmazeId)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                } else {
                                    null
                                }
                            }

                        _playbackImages.value = deferredImages.awaitAll().filterNotNull().toMap()
                    } else {
                        _playbackProgress.value = null
                    }
                } catch (e: Exception) {
                    _playbackProgress.value = null
                } finally {
                    _isLoadingPlayback.value = false
                }
            }
        }

        private fun fetchRecentHistory(bearerToken: String) {
            viewModelScope.launch {
                _isLoadingHistory.value = true
                try {
                    val response = traktRepository.getTraktRecentHistory(bearerToken)
                    if (response.isSuccess) {
                        val items = response.getOrNull()
                        _recentHistory.value = items
                        items?.let { historyList ->
                            val deferredImages =
                                historyList.mapNotNull { item ->
                                    val traktId = item.show?.ids?.trakt
                                    val imdbId = item.show?.ids?.imdb
                                    if (traktId != null && imdbId != null) {
                                        async {
                                            try {
                                                val season = item.episode?.season
                                                val number = item.episode?.number
                                                val (url, tvmazeId) =
                                                    if (season != null && number != null) {
                                                        dashboardRepository.getEpisodeImageAndTvmazeId(imdbId, season, number)
                                                    } else {
                                                        dashboardRepository.getShowImageAndTvmazeId(imdbId)
                                                    }
                                                traktId to ExtractedTraktInfo(imageUrl = url, tvmazeId = tvmazeId)
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
                } catch (e: Exception) {
                    _recentHistory.value = null
                } finally {
                    _isLoadingHistory.value = false
                }
            }
        }
    }
