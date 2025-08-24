/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.showDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.domain.TraktShowStats
import com.theupnextapp.domain.emptyShowData
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import com.theupnextapp.work.AddFavoriteShowWorker
import com.theupnextapp.work.RemoveFavoriteShowWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val showDetailRepository: ShowDetailRepository,
    private val workManager: WorkManager,
    private val traktRepository: TraktRepository,
    private val firebaseCrashlytics: FirebaseCrashlytics,
) : BaseTraktViewModel(
    traktRepository,
    workManager,
) {
    private val _show = MutableLiveData<ShowDetailArg>()
    private val _showCastBottomSheet = MutableStateFlow<ShowCast?>(null)
    val showCastBottomSheet: StateFlow<ShowCast?> = _showCastBottomSheet.asStateFlow()

    private val _navigateToSeasons = MutableStateFlow(false)
    val navigateToSeasons: StateFlow<Boolean> = _navigateToSeasons.asStateFlow()

    private val _uiState = MutableStateFlow(ShowDetailUiState())
    val uiState: StateFlow<ShowDetailUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isFavoriteShow =
        traktRepository.favoriteShow.map { it != null }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                false,
            )

    val showRating: StateFlow<TraktShowRating?> =
        traktRepository.traktShowRating
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                null,
            )

    val showStats: StateFlow<TraktShowStats?> =
        traktRepository.traktShowStats
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                null,
            )

    data class ShowDetailUiState(
        val showSummary: ShowDetailSummary? = null,
        val showPreviousEpisode: ShowPreviousEpisode? = null,
        val showNextEpisode: ShowNextEpisode? = null,
        val showCast: List<ShowCast>? = null,
        val isLoadingSummary: Boolean = false,
        val isCastLoading: Boolean = false,
        val isPreviousEpisodeLoading: Boolean = false,
        val isNextEpisodeLoading: Boolean = false,
        val summaryErrorMessage: String? = null,
        val castErrorMessage: String? = null,
        val previousEpisodeErrorMessage: String? = null,
        val nextEpisodeErrorMessage: String? = null,
        val generalErrorMessage: String? = null,
    )

    fun selectedShow(show: ShowDetailArg?) {
        _uiState.update { ShowDetailUiState() }
        _isLoading.value = true

        show?.let {
            _show.value = it
            _uiState.update { currentState ->
                currentState.copy(
                    isLoadingSummary = true,
                    summaryErrorMessage = null,
                    generalErrorMessage = null,
                    showSummary = null,
                    showCast = null,
                    showPreviousEpisode = null,
                    showNextEpisode = null,
                )
            }
            getShowSummary(it)
        } ?: run {
            _isLoading.value = false
            _uiState.update { currentState ->
                currentState.copy(
                    isLoadingSummary = false,
                    generalErrorMessage = "No show information provided.",
                )
            }
        }
    }

    private fun getShowSummary(show: ShowDetailArg) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSummary = true, summaryErrorMessage = null) }

            show.showId?.let { showId ->
                if (showId.isNotEmpty() && showId != "null") {
                    showDetailRepository.getShowSummary(showId.toInt()).collect { result ->
                        _isLoading.value = false

                        when (result) {
                            is Result.Success -> {
                                val summary = result.data
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        showSummary = summary,
                                        isLoadingSummary = false,
                                    )
                                }
                                getShowPreviousEpisode(summary.previousEpisodeHref)
                                getShowNextEpisode(summary.nextEpisodeHref)
                                getTraktShowRating(summary.imdbID)
                                getTraktShowStats(summary.imdbID)
                                checkIfShowIsTraktFavorite(summary.imdbID)
                                getShowCast(showId.toInt())
                            }

                            is Result.GenericError -> {
                                val errorMessage =
                                    result.error?.message
                                        ?: "Error loading summary (Code: ${result.code ?: "Unknown"})"
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = "GenericError in getShowSummary: $errorMessage",
                                        errorResponse = result.error,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        showSummary = emptyShowData(),
                                        isLoadingSummary = false,
                                        summaryErrorMessage = errorMessage,
                                    )
                                }
                            }

                            is Result.NetworkError -> {
                                val networkErrorMessage = "Network error loading summary."
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = networkErrorMessage,
                                        errorResponse = null,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        showSummary = emptyShowData(),
                                        isLoadingSummary = false,
                                        summaryErrorMessage = networkErrorMessage,
                                    )
                                }
                            }

                            is Result.Error -> {
                                val errorMessage =
                                    result.message
                                        ?: "An unexpected error occurred while loading summary."
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = errorMessage,
                                        cause = result.exception
                                    )
                                )
                                _uiState.update {
                                    it.copy(
                                        showSummary = emptyShowData(),
                                        isLoadingSummary = false,
                                        summaryErrorMessage = errorMessage,
                                    )
                                }
                            }

                            is Result.Loading -> {
                                _uiState.update { it.copy(isLoadingSummary = result.status) }
                                if (!result.status && !_uiState.value.isCastLoading && !_uiState.value.isPreviousEpisodeLoading && !_uiState.value.isNextEpisodeLoading) {
                                    _isLoading.value = false
                                } else if (result.status) {
                                    _isLoading.value = true
                                }
                            }
                        }
                    }
                } else {
                    _isLoading.value = false
                    _uiState.update {
                        it.copy(
                            showSummary = emptyShowData(),
                            isLoadingSummary = false,
                            summaryErrorMessage = "Invalid Show ID.",
                        )
                    }
                }
            } ?: run {
                _isLoading.value = false
                _uiState.update {
                    it.copy(
                        isLoadingSummary = false,
                        generalErrorMessage = "Show ID is missing.",
                    )
                }
            }
        }
    }

    private fun getShowCast(showId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCastLoading = true, castErrorMessage = null) }
            showDetailRepository.getShowCast(showId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(showCast = result.data, isCastLoading = false) }
                    }

                    is Result.GenericError -> {
                        val errorMessage =
                            result.error?.message
                                ?: "Failed to load cast (Code: ${result.code ?: "Unknown"})"
                        firebaseCrashlytics.recordException(
                            ShowDetailFetchException(
                                message = "GenericError in getShowCast: $errorMessage",
                                errorResponse = result.error,
                                cause = result.exception,
                            ),
                        )
                        _uiState.update {
                            it.copy(
                                isCastLoading = false,
                                castErrorMessage = errorMessage,
                            )
                        }
                    }

                    is Result.NetworkError -> {
                        val networkErrorMessage = "Network error loading cast."
                        firebaseCrashlytics.recordException(
                            ShowDetailFetchException(
                                message = networkErrorMessage,
                                errorResponse = null,
                                cause = result.exception,
                            ),
                        )
                        _uiState.update {
                            it.copy(
                                isCastLoading = false,
                                castErrorMessage = networkErrorMessage,
                            )
                        }
                    }

                    is Result.Error -> {
                        val errorMessage =
                            result.message ?: "An unexpected error occurred while loading cast."
                        firebaseCrashlytics.recordException(
                             ShowDetailFetchException(
                                 message = errorMessage,
                                 cause = result.exception
                             )
                        )
                        _uiState.update {
                            it.copy(
                                isCastLoading = false,
                                castErrorMessage = errorMessage,
                            )
                        }
                    }

                    is Result.Loading -> {
                        _uiState.update { it.copy(isCastLoading = result.status) }
                    }
                }
            }
        }
    }

    private fun getShowPreviousEpisode(previousEpisodeHref: String?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPreviousEpisodeLoading = true,
                    previousEpisodeErrorMessage = null,
                )
            }
            previousEpisodeHref?.takeIf { it.isNotEmpty() }?.let { href ->
                showDetailRepository.getPreviousEpisode(href).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    showPreviousEpisode = result.data,
                                    isPreviousEpisodeLoading = false,
                                )
                            }
                        }

                        is Result.GenericError -> {
                            val errorMessage =
                                result.error?.message
                                    ?: "Failed to load previous episode (Code: ${result.code ?: "Unknown"})"
                            firebaseCrashlytics.recordException(
                                ShowDetailFetchException(
                                    message = "GenericError in getShowPreviousEpisode: $errorMessage",
                                    errorResponse = result.error,
                                    cause = result.exception,
                                ),
                            )
                            _uiState.update {
                                it.copy(
                                    isPreviousEpisodeLoading = false,
                                    previousEpisodeErrorMessage = errorMessage,
                                )
                            }
                        }

                        is Result.NetworkError -> {
                            val networkErrorMessage = "Network error loading previous episode."
                            firebaseCrashlytics.recordException(
                                ShowDetailFetchException(
                                    message = networkErrorMessage,
                                    errorResponse = null,
                                    cause = result.exception,
                                ),
                            )
                            _uiState.update {
                                it.copy(
                                    isPreviousEpisodeLoading = false,
                                    previousEpisodeErrorMessage = networkErrorMessage,
                                )
                            }
                        }

                        is Result.Error -> {
                            val errorMessage =
                                result.message
                                    ?: "An unexpected error occurred while loading the previous episode."
                            firebaseCrashlytics.recordException(
                                ShowDetailFetchException(
                                    message = errorMessage,
                                    cause = result.exception
                                )
                            )
                            _uiState.update {
                                it.copy(
                                    isPreviousEpisodeLoading = false,
                                    previousEpisodeErrorMessage = errorMessage,
                                )
                            }
                        }

                        is Result.Loading -> {
                            _uiState.update { it.copy(isPreviousEpisodeLoading = result.status) }
                        }
                    }
                }
            } ?: _uiState.update {
                it.copy(
                    showPreviousEpisode = null,
                    isPreviousEpisodeLoading = false,
                )
            }
        }
    }

    private fun getShowNextEpisode(nextEpisodeHref: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isNextEpisodeLoading = true, nextEpisodeErrorMessage = null) }
            nextEpisodeHref?.takeIf { it.isNotEmpty() }?.let { href ->
                showDetailRepository.getNextEpisode(href).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    showNextEpisode = result.data,
                                    isNextEpisodeLoading = false,
                                )
                            }
                        }

                        is Result.GenericError -> {
                            val errorMessage =
                                result.error?.message
                                    ?: "Failed to load next episode (Code: ${result.code ?: "Unknown"})"
                            firebaseCrashlytics.recordException(
                                ShowDetailFetchException(
                                    message = "GenericError in getShowNextEpisode: $errorMessage",
                                    errorResponse = result.error,
                                    cause = result.exception,
                                ),
                            )
                            _uiState.update {
                                it.copy(
                                    isNextEpisodeLoading = false,
                                    nextEpisodeErrorMessage = errorMessage,
                                )
                            }
                        }

                        is Result.NetworkError -> {
                            val networkErrorMessage = "Network error loading next episode."
                            firebaseCrashlytics.recordException(
                                ShowDetailFetchException(
                                    message = networkErrorMessage,
                                    errorResponse = null,
                                    cause = result.exception,
                                ),
                            )
                            _uiState.update {
                                it.copy(
                                    isNextEpisodeLoading = false,
                                    nextEpisodeErrorMessage = networkErrorMessage,
                                )
                            }
                        }

                        is Result.Error -> {
                            val errorMessage =
                                result.message
                                    ?: "An unexpected error occurred while loading the next episode."
                            firebaseCrashlytics.recordException(
                                ShowDetailFetchException(
                                    message = errorMessage,
                                    cause = result.exception
                                )
                            )
                            _uiState.update {
                                it.copy(
                                    isNextEpisodeLoading = false,
                                    nextEpisodeErrorMessage = errorMessage,
                                )
                            }
                        }

                        is Result.Loading -> {
                            _uiState.update { it.copy(isNextEpisodeLoading = result.status) }
                        }
                    }
                }
            } ?: _uiState.update {
                it.copy(
                    showNextEpisode = null,
                    isNextEpisodeLoading = false,
                )
            }
        }
    }

    private fun getTraktShowRating(imdbID: String?) {
        viewModelScope.launch {
            try {
                traktRepository.getTraktShowRating(imdbID)
            } catch (e: Exception) {
                firebaseCrashlytics.recordException(
                    ShowDetailFetchException(
                        message = "Error fetching Trakt show rating for IMDB ID: $imdbID",
                        cause = e,
                    ),
                )
            }
        }
    }

    private fun getTraktShowStats(imdbID: String?) {
        viewModelScope.launch {
            try {
                traktRepository.getTraktShowStats(imdbID)
            } catch (e: Exception) {
                firebaseCrashlytics.recordException(
                    ShowDetailFetchException(
                        message = "Error fetching Trakt show stats for IMDB ID: $imdbID",
                        cause = e,
                    ),
                )
            }
        }
    }

    private fun checkIfShowIsTraktFavorite(imdbID: String?) {
        viewModelScope.launch {
            try {
                traktRepository.checkIfShowIsFavorite(imdbID)
            } catch (e: Exception) {
                firebaseCrashlytics.recordException(
                    ShowDetailFetchException(
                        message = "Error checking Trakt favorite status for IMDB ID: $imdbID",
                        cause = e,
                    ),
                )
                _uiState.update { it.copy(generalErrorMessage = "Could not check Trakt favorite status.") }
            }
        }
    }

    fun displayCastBottomSheetComplete() {
        _showCastBottomSheet.value = null
    }

    fun onShowCastItemClicked(showCast: ShowCast) {
        _showCastBottomSheet.value = showCast
    }

    fun onSeasonsClick() {
        _navigateToSeasons.value = true
    }

    fun onAddRemoveFavoriteClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAccessToken = traktAccessToken.value
            val currentFavoriteShow = favoriteShow.value
            val currentShowSummary = uiState.value.showSummary

            if (currentAccessToken != null) {
                if (currentFavoriteShow != null) {
                    val workerDataBuilder = Data.Builder()
                    currentFavoriteShow.traktID?.let {
                        workerDataBuilder.putInt(RemoveFavoriteShowWorker.ARG_TRAKT_ID, it)
                    }
                    workerDataBuilder.putString(
                        RemoveFavoriteShowWorker.ARG_IMDB_ID,
                        currentFavoriteShow.imdbID ?: currentShowSummary?.imdbID,
                    )
                    workerDataBuilder.putString(
                        RemoveFavoriteShowWorker.ARG_TOKEN,
                        currentAccessToken.access_token,
                    )

                    val removeFavoriteWork =
                        OneTimeWorkRequest.Builder(RemoveFavoriteShowWorker::class.java)
                            .setInputData(workerDataBuilder.build())
                            .build()
                    workManager.enqueue(removeFavoriteWork)
                } else {
                    currentShowSummary?.imdbID?.let { imdbId ->
                        val workerData =
                            Data.Builder()
                                .putString(AddFavoriteShowWorker.ARG_IMDB_ID, imdbId)
                                .putString(
                                    AddFavoriteShowWorker.ARG_TOKEN,
                                    currentAccessToken.access_token,
                                )
                                .build()

                        val addFavoriteWork =
                            OneTimeWorkRequest.Builder(AddFavoriteShowWorker::class.java)
                                .setInputData(workerData)
                                .build()
                        workManager.enqueue(addFavoriteWork)
                    }
                        ?: firebaseCrashlytics.log("Cannot add favorite: IMDB ID is null in showSummary.")
                }
            } else {
                firebaseCrashlytics.log("Cannot add/remove favorite: Trakt access token is null.")
                _uiState.update { it.copy(generalErrorMessage = "Please log in to Trakt to manage favorites.") }
            }
        }
    }

    fun onSeasonsNavigationComplete() {
        _navigateToSeasons.value = false
    }
}
