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

        fun fetchAiringSoonForYou(token: String) {
            viewModelScope.launch {
                _isLoadingAiringSoon.value = true
                try {
                    // Get today's date in yyyy-MM-dd format
                    val cal = Calendar.getInstance()
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val today = format.format(cal.time)

                    // Fetch 7 days of schedule
                    val response = traktRepository.getTraktMySchedule("Bearer $token", today, 7)
                    if (response.isSuccess) {
                        val shows = response.getOrNull()
                        _airingSoonShows.value = shows
                        shows?.let { scheduleList ->
                            val deferredImages =
                                scheduleList.mapNotNull { scheduleItem ->
                                    val traktId = scheduleItem.show?.ids?.trakt
                                    val imdbId = scheduleItem.show?.ids?.imdb
                                    if (traktId != null && imdbId != null) {
                                        async {
                                            val (url, tvmazeId) = dashboardRepository.getShowImageAndTvmazeId(imdbId)
                                            traktId to ExtractedTraktInfo(imageUrl = url, tvmazeId = tvmazeId)
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
    }
