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

import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    val isLoading: Flow<Boolean> = traktRepository.isLoading

    val favoriteShows: Flow<List<TraktUserListItem>> = traktRepository.traktFavoriteShows

    private val _openCustomTab = MutableSharedFlow<Boolean>()
    val openCustomTab = _openCustomTab.asSharedFlow()

    private val _confirmDisconnectFromTrakt = MutableSharedFlow<Boolean>()
    val confirmDisconnectFromTrakt = _confirmDisconnectFromTrakt.asSharedFlow()

    fun onConnectToTraktClick() {
        viewModelScope.launch {
            _openCustomTab.emit(true)
        }
    }fun onDisconnectFromTraktClick() {
        viewModelScope.launch {
            _confirmDisconnectFromTrakt.emit(true)
        }
    }

    fun onDisconnectFromTraktRefused() {
        viewModelScope.launch {
            _confirmDisconnectFromTrakt.emit(false)
        }
    }

    fun onDisconnectConfirm() {
        viewModelScope.launch(Dispatchers.IO) {
            traktRepository.clearFavorites()
            revokeTraktAccessToken()
            _confirmDisconnectFromTrakt.emit(false)
        }
    }

    fun onCustomTabOpened() {
        viewModelScope.launch {
            _openCustomTab.emit(false)
        }
    }

    fun onCodeReceived(code: String?) {
        code?.let {
            viewModelScope.launch(Dispatchers.IO) {
                traktRepository.getTraktAccessToken(it)
            }
        }
    }
}
