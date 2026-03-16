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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.theupnextapp.domain.EpisodeDetail
import com.theupnextapp.domain.EpisodePeople
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.TraktCheckInStatus
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val showDetailRepository: ShowDetailRepository,
        private val traktRepository: TraktRepository,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<Destinations.EpisodeDetail>()

        private val _uiState = MutableStateFlow(EpisodeDetailState())
        val uiState: StateFlow<EpisodeDetailState> = _uiState.asStateFlow()

        init {
            getEpisodeDetails()
            getEpisodePeople()
            observeCheckInStatus()
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
                traktRepository.checkInToShow(
                    showTraktId = route.showTraktId,
                    seasonNumber = route.seasonNumber,
                    episodeNumber = route.episodeNumber,
                )
            }
        }

        private fun observeCheckInStatus() {
            viewModelScope.launch {
                traktRepository.traktCheckInEvent.collect { status ->
                    _uiState.value = _uiState.value.copy(
                        isCheckingIn = false,
                        checkInStatus = status
                    )
                }
            }
        }

        fun clearCheckInStatus() {
            _uiState.value = _uiState.value.copy(checkInStatus = null)
        }
    }

data class EpisodeDetailState(
    val isLoading: Boolean = false,
    val isPeopleLoading: Boolean = false,
    val isCheckingIn: Boolean = false,
    val episodeDetail: EpisodeDetail? = null,
    val episodePeople: EpisodePeople? = null,
    val checkInStatus: TraktCheckInStatus? = null,
    val error: String? = null,
)
