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

package com.theupnextapp.ui.episodeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.theupnextapp.domain.EpisodeDetail
import com.theupnextapp.domain.EpisodePeople
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.TraktCheckInStatus
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import com.theupnextapp.work.SyncWatchProgressWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EpisodeDetailViewModel.Factory::class)
class EpisodeDetailViewModel
    @AssistedInject
    constructor(
        @Assisted val route: Destinations.EpisodeDetail,
        private val showDetailRepository: ShowDetailRepository,
        private val traktRepository: TraktRepository,
        private val watchProgressRepository: WatchProgressRepository,
        private val workManager: WorkManager,
        private val firebaseAnalytics: FirebaseAnalytics,
    ) : ViewModel() {

        @AssistedFactory
        interface Factory {
            fun create(route: Destinations.EpisodeDetail): EpisodeDetailViewModel
        }

        private val _uiState =
            MutableStateFlow(
                EpisodeDetailState(
                    isLoading = true,
                    isPeopleLoading = true,
                    isAuthorizedOnTrakt = route.isAuthorizedOnTrakt ?: false,
                    isWatched = route.isWatched ?: false,
                    episodeDetail =
                        EpisodeDetail(
                            title = null,
                            overview = null,
                            season = route.seasonNumber,
                            number = route.episodeNumber,
                            firstAired = null,
                            runtime = null,
                            rating = null,
                            tvdbId = null,
                            imdbId = route.imdbID,
                            tmdbId = null,
                            votes = null,
                        ),
                ),
            )
        val uiState: StateFlow<EpisodeDetailState> = _uiState.asStateFlow()

        init {
            getEpisodeDetails()
            getEpisodePeople()
            observeCheckInStatus()
            observeTraktAuthorization()
            observeWatchedEpisodes()
            refreshWatchedFromTrakt()
        }

        private fun getEpisodeDetails() {
            viewModelScope.launch {
                showDetailRepository.getEpisodeDetails(
                    traktId = route.showTraktId,
                    seasonNumber = route.seasonNumber,
                    episodeNumber = route.episodeNumber,
                ).collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = result.status)
                        }
                        is Result.Success -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    episodeDetail = result.data,
                                )
                        }
                        is Result.GenericError -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    error = result.error?.message ?: result.exception.message,
                                )
                        }
                        is Result.NetworkError -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    error = "Network Error",
                                )
                        }
                        else -> {}
                    }
                }
            }
        }

        private fun getEpisodePeople() {
            viewModelScope.launch {
                showDetailRepository.getEpisodePeople(
                    traktId = route.showTraktId,
                    seasonNumber = route.seasonNumber,
                    episodeNumber = route.episodeNumber,
                ).collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(isPeopleLoading = result.status)
                        }
                        is Result.Success -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isPeopleLoading = false,
                                    episodePeople = result.data,
                                )
                        }
                        is Result.GenericError -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isPeopleLoading = false,
                                    error = result.error?.message ?: result.exception.message,
                                )
                        }
                        is Result.NetworkError -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isPeopleLoading = false,
                                    error = "Network Error",
                                )
                        }
                        else -> {}
                    }
                }
            }
        }

        fun onCheckIn() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isCheckingIn = true)
                firebaseAnalytics.logEvent("episode_check_in") {
                    param("show_trakt_id", route.showTraktId.toLong())
                    param("season_number", route.seasonNumber.toLong())
                    param("episode_number", route.episodeNumber.toLong())
                    param("action", "check_in")
                }
                traktRepository.checkInToShow(
                    showTraktId = route.showTraktId,
                    seasonNumber = route.seasonNumber,
                    episodeNumber = route.episodeNumber,
                )
            }
        }

        fun onCancelCheckIn() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isCheckingIn = true)
                firebaseAnalytics.logEvent("episode_check_in") {
                    param("show_trakt_id", route.showTraktId.toLong())
                    param("season_number", route.seasonNumber.toLong())
                    param("episode_number", route.episodeNumber.toLong())
                    param("action", "cancel")
                }
                traktRepository.cancelCheckIn()
            }
        }

        private fun observeCheckInStatus() {
            viewModelScope.launch {
                traktRepository.traktCheckInEvent.collect { status ->
                    _uiState.value =
                        _uiState.value.copy(
                            isCheckingIn = false,
                            isCheckInSuccessful = status.checkInTime != null,
                            checkInStatus = status,
                        )
                }
            }
        }

        private fun observeTraktAuthorization() {
            viewModelScope.launch {
                traktRepository.isAuthorizedOnTrakt().collect { isAuthorized ->
                    _uiState.value = _uiState.value.copy(isAuthorizedOnTrakt = isAuthorized)
                }
            }
        }

        fun clearCheckInStatus() {
            _uiState.value = _uiState.value.copy(checkInStatus = null)
        }

        private fun observeWatchedEpisodes() {
            viewModelScope.launch {
                watchProgressRepository.getWatchedEpisodesForShow(route.showTraktId).collect { watchedList ->
                    if (watchedList.isNotEmpty() || route.isWatched == null) {
                        val isWatched =
                            watchedList.any {
                                it.seasonNumber == route.seasonNumber && it.episodeNumber == currentEpisodeNumber
                            }
                        _uiState.value = _uiState.value.copy(isWatched = isWatched)
                    }
                }
            }
        }

        private fun refreshWatchedFromTrakt() {
            viewModelScope.launch {
                traktRepository.traktAccessToken.firstOrNull()?.access_token?.let { token ->
                    try {
                        watchProgressRepository.refreshWatchedFromTrakt(
                            token = token,
                            showTraktId = route.showTraktId,
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to refresh watched state from Trakt, using local cache")
                    }
                }
            }
        }

        fun onToggleWatched() {
            if (!_uiState.value.isAuthorizedOnTrakt) return
            val targetWatchedState = !_uiState.value.isWatched
            _uiState.value = _uiState.value.copy(isWatched = targetWatchedState)

            firebaseAnalytics.logEvent("episode_toggle_watched") {
                param("show_trakt_id", route.showTraktId.toLong())
                param("season_number", route.seasonNumber.toLong())
                param("episode_number", currentEpisodeNumber.toLong())
                param("is_watched", targetWatchedState.toString())
                param("source", "episode_detail")
            }

            viewModelScope.launch {
                if (!targetWatchedState) {
                    watchProgressRepository.markEpisodeUnwatched(
                        showTraktId = route.showTraktId,
                        seasonNumber = route.seasonNumber,
                        episodeNumber = currentEpisodeNumber,
                    )
                } else {
                    watchProgressRepository.markEpisodeWatched(
                        showTraktId = route.showTraktId,
                        showTvMazeId = route.showId,
                        showImdbId = route.imdbID,
                        seasonNumber = route.seasonNumber,
                        episodeNumber = currentEpisodeNumber,
                    )
                }

                triggerSyncIfAuthenticated()
            }
        }

        private fun triggerSyncIfAuthenticated() {
            viewModelScope.launch {
                traktRepository.traktAccessToken.firstOrNull()?.access_token?.let { token ->
                    val syncWork =
                        OneTimeWorkRequestBuilder<SyncWatchProgressWorker>()
                            .setInputData(
                                Data.Builder()
                                    .putString(SyncWatchProgressWorker.ARG_TOKEN, token)
                                    .build(),
                            ).build()
                    workManager.enqueue(syncWork)
                }
            }
        }

        val currentEpisodeNumber: Int
            get() = _uiState.value.episodeDetail?.number ?: route.episodeNumber

        val canNavigatePrevious: Boolean
            get() = currentEpisodeNumber > 1

        fun getPreviousEpisodeRoute(): Destinations.EpisodeDetail? {
            if (!canNavigatePrevious) return null
            return route.copy(
                episodeNumber = currentEpisodeNumber - 1,
                episodeImageUrl = null,
            )
        }

        fun getNextEpisodeRoute(): Destinations.EpisodeDetail {
            return route.copy(
                episodeNumber = currentEpisodeNumber + 1,
                episodeImageUrl = null,
            )
        }
    }

data class EpisodeDetailState(
    val isLoading: Boolean = false,
    val isPeopleLoading: Boolean = false,
    val isCheckingIn: Boolean = false,
    val isCheckInSuccessful: Boolean = false,
    val isAuthorizedOnTrakt: Boolean = false,
    val isWatched: Boolean = false,
    val isWatchedLoading: Boolean = false,
    val episodeDetail: EpisodeDetail? = null,
    val episodePeople: EpisodePeople? = null,
    val checkInStatus: TraktCheckInStatus? = null,
    val error: String? = null,
)
