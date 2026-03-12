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

        private val _airingSoonImages = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
        val airingSoonImages: StateFlow<Map<String, ExtractedTraktInfo>> = _airingSoonImages.asStateFlow()

        private val _isLoadingAiringSoon = MutableStateFlow(false)
        val isLoadingAiringSoon: StateFlow<Boolean> = _isLoadingAiringSoon.asStateFlow()

        private val _recentHistory =
            MutableStateFlow<List<com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse>?>(null)
        val recentHistory: StateFlow<List<com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse>?> = _recentHistory.asStateFlow()

        private val _historyImages = MutableStateFlow<Map<String, ExtractedTraktInfo>>(emptyMap())
        val historyImages: StateFlow<Map<String, ExtractedTraktInfo>> = _historyImages.asStateFlow()

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
                                    val season = scheduleItem.episode?.season
                                    val number = scheduleItem.episode?.number
                                    if (traktId != null && imdbId != null) {
                                        async {
                                            try {
                                                val (url, tvmazeId) = dashboardRepository.getShowImageAndTvmazeId(imdbId)
                                                val uniqueKey = "$traktId-${season ?: 0}-${number ?: 0}"
                                                uniqueKey to ExtractedTraktInfo(imageUrl = url, tvmazeId = tvmazeId)
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
                                                val uniqueKey = "$traktId-${season ?: 0}-${number ?: 0}"
                                                uniqueKey to ExtractedTraktInfo(imageUrl = url, tvmazeId = tvmazeId)
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
