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

package com.theupnextapp.ui.traktAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TraktAccountViewModel @Inject constructor(
    private val traktRepository: TraktRepository,
    workManager: WorkManager
) : BaseTraktViewModel(
    traktRepository,
    workManager
) {

    val isLoading = traktRepository.isLoading

    val favoriteShows = traktRepository.traktFavoriteShows.asLiveData()

    private val _openCustomTab = MutableLiveData<Boolean>()
    val openCustomTab: LiveData<Boolean> = _openCustomTab

    private val _confirmDisconnectFromTrakt = MutableLiveData<Boolean>()
    val confirmDisconnectFromTrakt: LiveData<Boolean> = _confirmDisconnectFromTrakt

    val favoriteShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(favoriteShows) {
            value = it.isNullOrEmpty() == true
        }
    }

    fun onConnectToTraktClick() {
        _openCustomTab.postValue(true)
    }

    fun onDisconnectFromTraktClick() {
        _confirmDisconnectFromTrakt.postValue(true)
    }

    fun onDisconnectFromTraktConfirmed() {
        _confirmDisconnectFromTrakt.postValue(false)
    }

    fun onDisconnectConfirm() {
        viewModelScope.launch(Dispatchers.IO) {
            traktRepository.clearFavorites()
            revokeTraktAccessToken()
        }
    }

    fun onCustomTabOpened() {
        _openCustomTab.postValue(false)
    }

    fun onCodeReceived(code: String?) {
        if (!code.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                traktRepository.getTraktAccessToken(code)
            }
        }
    }
}