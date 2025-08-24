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
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.RefreshFavoriteShowsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class BaseTraktViewModel
    @Inject
    constructor(
        private val traktRepository: TraktRepository,
        private val workManager: WorkManager,
    ) : ViewModel() {
        val traktAccessToken: StateFlow<TraktAccessToken?> =
            traktRepository.traktAccessToken
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null,
                )

        val favoriteShow: StateFlow<TraktUserListItem?> =
            traktRepository.favoriteShow
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null,
                )

        val isAuthorizedOnTrakt: StateFlow<Boolean> =
            traktAccessToken
                .map { accessToken ->
                    val isTokenPresentAndNotEmpty = !accessToken?.access_token.isNullOrEmpty()

                    val isTokenStructurallyValid = accessToken?.isTraktAccessTokenValid() == true

                    isTokenPresentAndNotEmpty && isTokenStructurallyValid
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = false,
                )

        init {
            viewModelScope.launch {
                traktAccessToken
                    .onEach { accessToken ->
                        if (accessToken != null) {
                            if (!accessToken.isTraktAccessTokenValid()) {
                                viewModelScope.launch(Dispatchers.IO) {
                                    traktRepository.getTraktAccessRefreshToken(accessToken.refresh_token)
                                }
                            } else {
                                val workerData =
                                    Data.Builder().putString(
                                        RefreshFavoriteShowsWorker.ARG_TOKEN,
                                        accessToken.access_token,
                                    ).build()

                                val refreshFavoritesWork =
                                    OneTimeWorkRequest.Builder(RefreshFavoriteShowsWorker::class.java)
                                        .setInputData(workerData)
                                        .build()

                                workManager.enqueue(refreshFavoritesWork)
                            }
                        }
                    }
                    .collect()
            }
        }

        fun revokeTraktAccessToken() {
            viewModelScope.launch(Dispatchers.IO) {
                val currentToken = traktAccessToken.value
                if (currentToken != null) {
                    if (!currentToken.access_token.isNullOrEmpty() &&
                        currentToken.isTraktAccessTokenValid()
                    ) {
                        traktRepository.revokeTraktAccessToken(currentToken)
                    }
                }
            }
        }
    }
