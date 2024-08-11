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

import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.domain.emptyShowData
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import com.theupnextapp.work.AddFavoriteShowWorker
import com.theupnextapp.work.BaseWorker
import com.theupnextapp.work.RemoveFavoriteShowWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val showDetailRepository: ShowDetailRepository,
    private val workManager: WorkManager,
    private val traktRepository: TraktRepository,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseTraktViewModel(
    traktRepository,
    workManager
) {

    private val _show = MutableSharedFlow<ShowDetailArg>()
    val show: SharedFlow<ShowDetailArg> = _show

    private val _navigateToSeasons = MutableSharedFlow<Unit>()

    internal val _showSummary = MutableStateFlow<ShowDetailSummary?>(null)
    val showSummary: StateFlow<ShowDetailSummary?> = _showSummary

    private val _showPreviousEpisode = MutableStateFlow<ShowPreviousEpisode?>(null)
    val showPreviousEpisode: StateFlow<ShowPreviousEpisode?> = _showPreviousEpisode

    private val _showNextEpisode = MutableStateFlow<ShowNextEpisode?>(null)
    val showNextEpisode: StateFlow<ShowNextEpisode?> = _showNextEpisode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isFavoriteShow = MutableStateFlow(false)
    val isFavoriteShow: StateFlow<Boolean> = _isFavoriteShow

    private val _showCast = MutableStateFlow<List<ShowCast>?>(null)
    val showCast: StateFlow<List<ShowCast>?> = _showCast

    val showRating = traktRepository.traktShowRating

    val showStats = traktRepository.traktShowStats

    val favoriteShow = traktRepository.favoriteShow

    init {
        viewModelScope.launch {
            combine(
                showDetailRepository.isLoading,
                traktRepository.isLoading
            ) { upNextLoading, traktLoading ->
                upNextLoading || traktLoading
            }.collect {
                _isLoading.value = it
            }
        }

        viewModelScope.launch {
            traktRepository.favoriteShow.collect {
                _isFavoriteShow.value = it != null
            }
        }
    }

    private fun <T> handleResult(
        result: Result<T>,
        stateFlow: MutableStateFlow<T?>,
        onSuccess: (Result.Success<T>) -> Unit = {}
    ) {
        when (result) {
            is Result.Success -> {
                stateFlow.value = result.data
                onSuccess(result)
                _isLoading.value = false
            }

            is Result.Loading -> _isLoading.value = result.isLoading
            is Result.UnknownError -> {
                firebaseCrashlytics.recordException(result.exception)
                _isLoading.value = false
            }

            else -> _isLoading.value = false
        }
    }

    fun selectedShow(show: ShowDetailArg?) {
        show?.let {
            viewModelScope.launch {
                _show.emit(it)
                getShowSummary(it)
            }
        }
    }

    private fun getShowSummary(show: ShowDetailArg) {
        viewModelScope.launch {
            show.showId?.let { showId ->
                if (showId.isNotEmpty() && showId != "null") {
                    showDetailRepository.getShowSummary(showId.toInt()).collect { result ->
                        handleResult(result, _showSummary) {
                            // Perform actions when Result.Success is received
                            val showSummary = it.data
                            getShowPreviousEpisode(showSummary.previousEpisodeHref)
                            getShowNextEpisode(showSummary.nextEpisodeHref)
                            showSummary.imdbID?.let { imdbId ->
                                getTraktShowRating(imdbId)
                                getTraktShowStats(imdbId)
                                checkIfShowIsTraktFavorite(imdbId)
                            }
                        }
                    }
                    getShowCast(showId.toInt())
                } else {
                    _showSummary.value = emptyShowData()
                }
            }
        }
    }

    private fun getShowCast(showId: Int) {
        viewModelScope.launch {
            showDetailRepository.getShowCast(showId).collect { result ->
                handleResult(result, _showCast)
            }
        }
    }

    private fun getShowPreviousEpisode(previousEpisodeHref: String?) {
        viewModelScope.launch {
            if (!previousEpisodeHref.isNullOrEmpty()) {
                showDetailRepository.getPreviousEpisode(previousEpisodeHref).collect { result ->
                    handleResult(result, _showPreviousEpisode)
                }
            }
        }
    }

    private fun getShowNextEpisode(nextEpisodeHref: String?) {
        viewModelScope.launch {
            if (!nextEpisodeHref.isNullOrEmpty()) {
                showDetailRepository.getNextEpisode(nextEpisodeHref).collect { result ->
                    handleResult(result, _showNextEpisode)
                }
            }
        }
    }

    private fun getTraktShowRating(imdbID: String?) {
        viewModelScope.launch {
            traktRepository.getTraktShowRating(imdbID)
        }
    }

    private fun getTraktShowStats(imdbID: String?) {
        viewModelScope.launch {
            traktRepository.getTraktShowStats(imdbID)
        }
    }

    private fun checkIfShowIsTraktFavorite(imdbID: String?) {
        viewModelScope.launch {
            traktRepository.checkIfShowIsFavorite(imdbID)
        }
    }

    fun onSeasonsClick() {
        viewModelScope.launch {
            _navigateToSeasons.emit(Unit)
        }
    }

    fun onAddRemoveFavoriteClick() {
        viewModelScope.launch(Dispatchers.IO) {
            traktAccessToken.collect { accessToken ->
                if (accessToken != null) {
                    when (favoriteShow.value) {
                        null -> addFavoriteShow(accessToken, showSummary.value)
                        else -> removeFavoriteShow(accessToken, favoriteShow.value)
                    }
                } else {
                    handleAccessTokenError()
                }
            }
        }
    }

    private fun addFavoriteShow(accessToken: TraktAccessToken, showSummary: ShowDetailSummary?) {
        showSummary?.imdbID?.let { imdbId ->
            val workerData = workDataOf(
                AddFavoriteShowWorker.ARG_IMDB_ID to imdbId,
                AddFavoriteShowWorker.ARG_TOKEN to accessToken.access_token
            )
            enqueueWorker<AddFavoriteShowWorker>(workerData)
        } ?: handleShowSummaryError() // Handle the case where showSummary or imdbID is null
    }

    private fun removeFavoriteShow(
        accessToken: TraktAccessToken,
        showToRemove: TraktUserListItem?
    ) {
        showToRemove?.let { show ->
            val workerData = workDataOf(
                RemoveFavoriteShowWorker.ARG_TRAKT_ID to show.traktID,
                RemoveFavoriteShowWorker.ARG_IMDB_ID to show.imdbID,
                RemoveFavoriteShowWorker.ARG_TOKEN to accessToken.access_token
            )
            enqueueWorker<RemoveFavoriteShowWorker>(workerData)
        } ?: handleShowToRemoveError() // Handle the case where showToRemove is null
    }

    internal inline fun <reified T : BaseWorker> enqueueWorker(workerData: Data) {
        val workRequest = OneTimeWorkRequest.Builder(T::class.java)
            .setInputData(workerData)
            .build()
        workManager.enqueue(workRequest)
    }

    private fun handleAccessTokenError() {
        // Log the error for debugging
        FirebaseCrashlytics.getInstance().recordException(Throwable("Access token is null"))
    }

    private fun handleShowSummaryError() {
        FirebaseCrashlytics.getInstance()
            .recordException(Throwable("Show summary or IMDB ID is null"))
    }

    private fun handleShowToRemoveError() {
        // Log the error for debugging
        FirebaseCrashlytics.getInstance().recordException(Throwable("Show to remove is null"))
    }
}
