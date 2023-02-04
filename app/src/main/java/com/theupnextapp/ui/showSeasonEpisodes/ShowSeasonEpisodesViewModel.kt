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

package com.theupnextapp.ui.showSeasonEpisodes

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import androidx.work.WorkManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class ShowSeasonEpisodesViewModel(
    savedStateHandle: SavedStateHandle,
    showDetailRepository: ShowDetailRepository,
    private val traktRepository: TraktRepository,
    workManager: WorkManager,
    showSeasonEpisodesArg: ShowSeasonEpisodesArg
) : BaseTraktViewModel(
    traktRepository,
    workManager
) {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _episodes = MutableLiveData<List<ShowSeasonEpisode>?>()
    val episodes: LiveData<List<ShowSeasonEpisode>?> = _episodes

    val traktCheckInStatus = traktRepository.traktCheckInStatus

    private val _seasonNumber = MutableLiveData<Int?>(showSeasonEpisodesArg.seasonNumber)
    val seasonNumber: LiveData<Int?> = _seasonNumber

    private val _confirmCheckIn = MutableLiveData<ShowSeasonEpisode?>()
    val confirmCheckIn: LiveData<ShowSeasonEpisode?> = _confirmCheckIn

    init {
        savedStateHandle.set(SEASON_NUMBER, showSeasonEpisodesArg.seasonNumber)
        savedStateHandle.set(SHOW_ID, showSeasonEpisodesArg.showId)

        viewModelScope.launch {
            savedStateHandle.get<Int>(SEASON_NUMBER)?.let { seasonNumber ->
                savedStateHandle.get<Int>(SHOW_ID)?.let { showId ->
                    showDetailRepository.getShowSeasonEpisodes(
                        showId = showId,
                        seasonNumber = seasonNumber
                    ).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _episodes.value = result.data
                            }
                            is Result.Loading -> {
                                _isLoading.value = result.status
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    fun onCheckInConfirm(showSeasonEpisode: ShowSeasonEpisode) {
        viewModelScope.launch {
            traktRepository.checkInToShow(showSeasonEpisode, traktAccessToken.value?.access_token)
            onCheckInComplete()
        }
    }

    private fun onCheckInComplete() {
        _confirmCheckIn.value = null
    }

    @AssistedFactory
    interface ShowSeasonEpisodesViewModelFactory {
        fun create(
            owner: SavedStateRegistryOwner,
            showSeasonEpisodesArg: ShowSeasonEpisodesArg
        ): Factory
    }

    companion object {
        const val SHOW_ID = "showId"
        const val SEASON_NUMBER = "seasonNumber"
    }

    class Factory @AssistedInject constructor(
        @Assisted owner: SavedStateRegistryOwner,
        private val repository: ShowDetailRepository,
        private val traktRepository: TraktRepository,
        private val workManager: WorkManager,
        @Assisted private val showSeasonEpisodesArg: ShowSeasonEpisodesArg
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return ShowSeasonEpisodesViewModel(
                handle,
                repository,
                traktRepository,
                workManager,
                showSeasonEpisodesArg
            ) as T
        }
    }
}
