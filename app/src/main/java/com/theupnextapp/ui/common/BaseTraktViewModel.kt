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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.RefreshFavoriteShowsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class BaseTraktViewModel @Inject constructor(
    private val traktRepository: TraktRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val traktAccessToken = traktRepository.traktAccessToken.asLiveData()

    private val _isAuthorizedOnTrakt = MediatorLiveData<Boolean>()
    val isAuthorizedOnTrakt: LiveData<Boolean> = _isAuthorizedOnTrakt

    init {
        viewModelScope.launch {
            _isAuthorizedOnTrakt.addSource(traktAccessToken) { accessToken ->
                _isAuthorizedOnTrakt.value =
                    accessToken?.access_token.isNullOrEmpty() == false && accessToken?.isTraktAccessTokenValid() == true

                if (accessToken?.isTraktAccessTokenValid() == false) {
                    viewModelScope.launch(Dispatchers.IO) {
                        traktRepository.getTraktAccessRefreshToken(accessToken.refresh_token)
                    }
                } else {
                    val workerData = Data.Builder().putString(
                        RefreshFavoriteShowsWorker.ARG_TOKEN,
                        accessToken?.access_token
                    )
                    val refreshFavoritesWork =
                        OneTimeWorkRequest.Builder(RefreshFavoriteShowsWorker::class.java)
                    refreshFavoritesWork.setInputData(workerData.build())

                    workManager.enqueue(refreshFavoritesWork.build())
                }
            }
        }
    }

    fun revokeTraktAccessToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val accessToken = traktAccessToken.value
            if (accessToken != null) {
                if (!accessToken.access_token.isNullOrEmpty() && accessToken.isTraktAccessTokenValid()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        traktRepository.revokeTraktAccessToken(accessToken)
                    }
                }
            }
        }
    }
}
