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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.emptyShowData
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import com.theupnextapp.work.AddFavoriteShowWorker
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

    private val _showCastBottomSheet = MutableSharedFlow<ShowCast?>()
    val showCastBottomSheet: SharedFlow<ShowCast?> = _showCastBottomSheet

    private val _navigateToSeasons = MutableSharedFlow<Unit>()
    val navigateToSeasons: SharedFlow<Unit> = _navigateToSeasons

    private val _showSummary = MutableStateFlow<ShowDetailSummary?>(null)
    val showSummary: StateFlow<ShowDetailSummary?> = _showSummary

    private val _showPreviousEpisode = MutableStateFlow<ShowPreviousEpisode?>(null)
    val showPreviousEpisode: StateFlow<ShowPreviousEpisode?> = _showPreviousEpisode

    private val _showNextEpisode = MutableStateFlow<ShowNextEpisode?>(null)
    val showNextEpisode: StateFlow<ShowNextEpisode?> = _showNextEpisode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isFavoriteShow = MutableStateFlow(false)
    val isFavoriteShow: StateFlow<Boolean> = _isFavoriteShow

    private val isUpnextRepositoryLoading = showDetailRepository.isLoading

    private val isTraktRepositoryLoading = traktRepository.isLoading

    private val _showCast = MutableStateFlow<List<ShowCast>?>()
    val showCast: StateFlow<List<ShowCast>?> = _showCast

    val showRating = traktRepository.traktShowRating

    val showStats = traktRepository.traktShowStats

    private val favoriteShow = traktRepository.favoriteShow

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
                onSuccess(result) // Call onSuccess only for Result.Success
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
                    handleResult(result, _showNextEpisode)}
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
            val accessToken = traktAccessToken.value
            if (accessToken != null) {
                if (favoriteShow.value != null) {
                    val traktUserListItem = favoriteShow.value
                    val workerData = Data.Builder()
                    traktUserListItem?.traktID?.let {
                        workerData.putInt(
                            RemoveFavoriteShowWorker.ARG_TRAKT_ID,
                            it
                        )
                    }
                    workerData.putString(
                        RemoveFavoriteShowWorker.ARG_IMDB_ID,
                        traktUserListItem?.imdbID
                    )
                    workerData.putString(
                        RemoveFavoriteShowWorker.ARG_TOKEN,
                        accessToken.access_token
                    )

                    val removeFavoriteWork =
                        OneTimeWorkRequest.Builder(RemoveFavoriteShowWorker::class.java)
                    removeFavoriteWork.setInputData(workerData.build())

                    workManager.enqueue(removeFavoriteWork.build())
                } else {
                    val workerData = Data.Builder()
                    workerData.putString(
                        AddFavoriteShowWorker.ARG_IMDB_ID,
                        showSummary.value?.imdbID
                    )
                    workerData.putString(AddFavoriteShowWorker.ARG_TOKEN, accessToken.access_token)

                    val addFavoriteWork =
                        OneTimeWorkRequest.Builder(AddFavoriteShowWorker::class.java)
                    addFavoriteWork.setInputData(workerData.build())

                    workManager.enqueue(addFavoriteWork.build())
                }
            }
        }
    }

    fun onSeasonsNavigationComplete() {
        viewModelScope.launch {
            _navigateToSeasons.emit(Unit)
        }
    }
}
