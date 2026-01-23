/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
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

package com.theupnextapp.common.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.RefreshFavoriteShowsWorker
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Singleton
class TraktAuthManager @Inject constructor(
    private val traktRepository: TraktRepository,
    private val workManager: WorkManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val traktAuthState: StateFlow<TraktAuthState> = traktRepository.traktAccessToken
        .map { accessToken ->
            val isTokenPresentAndNotEmpty = !accessToken?.access_token.isNullOrEmpty()
            val isTokenStructurallyValid = accessToken?.isTraktAccessTokenValid() == true

            if (isTokenPresentAndNotEmpty && isTokenStructurallyValid) {
                TraktAuthState.LoggedIn
            } else {
                TraktAuthState.LoggedOut
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TraktAuthState.Loading
        )

    init {
        scope.launch {
            // Keep track of the last token we tried to refresh to prevent loops
            var lastRefreshedToken: String? = null
            
            traktRepository.traktAccessToken
                .collect { accessToken ->
                    if (accessToken != null) {
                        if (!accessToken.isTraktAccessTokenValid()) {
                            val currentTokenString = accessToken.access_token
                            
                            // Only attempt refresh if we haven't just tried this token
                            if (currentTokenString != lastRefreshedToken) {
                                lastRefreshedToken = currentTokenString
                                traktRepository.getTraktAccessRefreshToken(accessToken.refresh_token)
                            } else {
                                // If we are here, it means we refreshed, got a token, and it's STILL invalid.
                                // Or we are looping on the same invalid token.
                                // We should probably logout or stop trying.
                                // For now, let's just log and maybe clear data if it persists?
                                // Safe fallback: do nothing, just don't loop.
                            }
                        } else {
                            // Token is valid. Reset tracker.
                            lastRefreshedToken = null
                            
                            val workerData = Data.Builder()
                                .putString(RefreshFavoriteShowsWorker.ARG_TOKEN, accessToken.access_token)
                                .build()
    
                            val refreshFavoritesWork = OneTimeWorkRequest.Builder(RefreshFavoriteShowsWorker::class.java)
                                .setInputData(workerData)
                                .build()
    
                            workManager.enqueueUniqueWork(
                                "refresh_favorite_shows_work",
                                androidx.work.ExistingWorkPolicy.KEEP,
                                refreshFavoritesWork
                            )
                        }
                    }
                }
        }
    }
}
