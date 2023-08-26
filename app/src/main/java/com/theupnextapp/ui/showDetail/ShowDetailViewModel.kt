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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val showDetailRepository: ShowDetailRepository,
    private val workManager: WorkManager,
    private val traktRepository: TraktRepository
) : BaseTraktViewModel(
    traktRepository,
    workManager
) {

    private val _show = MutableLiveData<ShowDetailArg>()
    val showDetailArg: LiveData<ShowDetailArg> = _show

    private val _showCastBottomSheet = MutableLiveData<ShowCast?>()
    val showCastBottomSheet: LiveData<ShowCast?> = _showCastBottomSheet

    private val _navigateToSeasons = MutableLiveData<Boolean>()
    val navigateToSeasons: LiveData<Boolean> = _navigateToSeasons

    private val _showSummary = MutableLiveData<ShowDetailSummary?>()
    val showSummary: LiveData<ShowDetailSummary?> = _showSummary

    private val _showPreviousEpisode = MutableLiveData<ShowPreviousEpisode?>()
    val showPreviousEpisode: LiveData<ShowPreviousEpisode?> = _showPreviousEpisode

    private val _showNextEpisode = MutableLiveData<ShowNextEpisode?>()
    val showNextEpisode: LiveData<ShowNextEpisode?> = _showNextEpisode

    val isLoading = MediatorLiveData<Boolean>()

    val isFavoriteShow = MediatorLiveData<Boolean>()

    private val isUpnextRepositoryLoading = showDetailRepository.isLoading

    private val isTraktRepositoryLoading = traktRepository.isLoading

    private val _showCast = MutableLiveData<List<ShowCast>?>()
    val showCast: LiveData<List<ShowCast>?> = _showCast

    val showRating = traktRepository.traktShowRating

    val showStats = traktRepository.traktShowStats

    private val favoriteShow = traktRepository.favoriteShow

    init {
        isLoading.addSource(isUpnextRepositoryLoading) { result ->
            isLoading.value = result == true
        }

        isLoading.addSource(isTraktRepositoryLoading) { result ->
            isLoading.value = result == true
        }

        isFavoriteShow.addSource(favoriteShow) { result ->
            isFavoriteShow.value = result != null
        }
    }

    fun selectedShow(show: ShowDetailArg?) {
        show?.let {
            _show.value = it
            getShowSummary(it)
        }
    }

    private fun getShowSummary(show: ShowDetailArg) {
        viewModelScope.launch {
            show.showId?.let {
                if (!it.isNullOrEmpty()) {
                    showDetailRepository.getShowSummary(it.toInt()).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val showSummary = result.data
                                _showSummary.value = showSummary
                                getShowPreviousEpisode(showSummary.previousEpisodeHref)
                                getShowNextEpisode(showSummary.nextEpisodeHref)
                                getTraktShowRating(showSummary.imdbID)
                                getTraktShowStats(showSummary.imdbID)
                                checkIfShowIsTraktFavorite(showSummary.imdbID)
                            }
                            is Result.Loading -> {
                                isLoading.value = result.status
                            }
                            else -> {}
                        }
                    }
                    getShowCast(it.toInt())
                } else {
                    _showSummary.value = emptyShowData()
                }
            }
        }
    }

    private fun getShowCast(showId: Int) {
        viewModelScope.launch {
            showDetailRepository.getShowCast(showId).collect { response ->
                when (response) {
                    is Result.Success -> {
                        _showCast.value = response.data
                    }
                    is Result.Loading -> {
                        isLoading.value = response.status
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getShowPreviousEpisode(previousEpisodeHref: String?) {
        viewModelScope.launch {
            showDetailRepository.getPreviousEpisode(previousEpisodeHref).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _showPreviousEpisode.value = result.data
                    }
                    is Result.Loading -> {
                        isLoading.value = result.status
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getShowNextEpisode(nextEpisodeHref: String?) {
        viewModelScope.launch {
            showDetailRepository.getNextEpisode(nextEpisodeHref).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _showNextEpisode.value = result.data
                    }
                    is Result.Loading -> {
                        isLoading.value = result.status
                    }
                    else -> {}
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
        _navigateToSeasons.value = false
    }
}
