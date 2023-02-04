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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowSeasonEpisodesViewModel @Inject constructor(
    private val showDetailRepository: ShowDetailRepository,
    traktRepository: TraktRepository,
    workManager: WorkManager
) : BaseTraktViewModel(
    traktRepository,
    workManager
) {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _episodes = MutableLiveData<List<ShowSeasonEpisode>?>()
    val episodes: LiveData<List<ShowSeasonEpisode>?> = _episodes

    private val _seasonNumber = MutableLiveData<Int?>()
    val seasonNumber: LiveData<Int?> = _seasonNumber

    fun selectedSeason(showSeasonEpisodesArg: ShowSeasonEpisodesArg?) {
        showSeasonEpisodesArg?.let { selectedSeason ->
            _seasonNumber.value = selectedSeason.seasonNumber

            selectedSeason.showId?.let { season ->
                selectedSeason.seasonNumber?.let { seasonNumber ->
                    viewModelScope.launch {
                        showDetailRepository.getShowSeasonEpisodes(
                            showId = season,
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
    }
}
