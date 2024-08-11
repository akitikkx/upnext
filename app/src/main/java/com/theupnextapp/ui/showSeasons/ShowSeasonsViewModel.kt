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

package com.theupnextapp.ui.showSeasons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.repository.ShowDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowSeasonsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val showDetailRepository: ShowDetailRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _showSeasons = MutableLiveData<List<ShowSeason>?>()
    val showSeasons: LiveData<List<ShowSeason>?> = _showSeasons

    fun setSelectedShow(showDetailArg: ShowDetailArg?) {
        showDetailArg?.let { selectedShow ->
            savedStateHandle.set(SHOW_ID, selectedShow.showId)
            viewModelScope.launch {
                savedStateHandle.get<String>(SHOW_ID)?.let {
                    showDetailRepository.getShowSeasons(it.toInt()).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _showSeasons.value = result.data
                            }
                            is Result.Loading -> {
                                _isLoading.value = result.isLoading
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val SHOW_ID = "showId"
    }
}
