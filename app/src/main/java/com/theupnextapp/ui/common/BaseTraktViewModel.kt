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

package com.theupnextapp.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.RefreshFavoriteShowsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class BaseTraktViewModel @Inject constructor(
    private val traktRepository: TraktRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val traktAccessToken = traktRepository.traktAccessToken

    private val _isAuthorizedOnTrakt = MutableStateFlow(false)
    val isAuthorizedOnTrakt: StateFlow<Boolean> = _isAuthorizedOnTrakt

    init {
        viewModelScope.launch {
            traktAccessToken.collect { accessToken ->
                val token = accessToken?.access_token
                _isAuthorizedOnTrakt.value = !token.isNullOrEmpty() && accessToken?.isTraktAccessTokenValid() == true

                delay(100)

                if (accessToken?.isTraktAccessTokenValid() == false) {
                    try {
                        traktRepository.getTraktAccessRefreshToken(accessToken.refresh_token)
                    } catch (e: Exception) {
                        // Handle refresh token error (e.g., log, emit error state)
                    }
                } else {
                    val refreshFavoritesWork = OneTimeWorkRequest.Builder(RefreshFavoriteShowsWorker::class.java)
                        .apply {
                            setInputData(
                                Data.Builder()
                                    .putString(RefreshFavoriteShowsWorker.ARG_TOKEN, accessToken?.access_token)
                                    .build()
                            )
                        }
                        .build()
                    workManager.enqueue(refreshFavoritesWork)
                }
            }
        }
    }

    fun revokeTraktAccessToken() {
        viewModelScope.launch(Dispatchers.IO) {
            traktAccessToken.collect { accessToken -> // Collect from the flowif (!accessToken?.access_token.isNullOrEmpty() && accessToken.isTraktAccessTokenValid()) {
                try {
                    if (accessToken != null) {
                        traktRepository.revokeTraktAccessToken(accessToken)
                    }
                } catch (e: Exception) {
                    // Handle token revocation error
                }
            }
        }
    }
}
