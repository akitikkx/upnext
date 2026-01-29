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
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.common.utils.TraktConstants
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TraktAccountViewModel
@Inject
constructor(
    private val traktRepository: TraktRepository,
    workManager: WorkManager,
    val traktAuthManager: TraktAuthManager,
) : BaseTraktViewModel(
    traktRepository,
    workManager,
    traktAuthManager,
) {

    init {
        viewModelScope.launch {
            traktAccessToken.collect { token ->
                if (token != null && token.isTraktAccessTokenValid()) {
                    token.access_token?.let {
                        traktRepository.refreshFavoriteShows(it)
                    }
                }
            }
        }
    }

    // General loading state for initial connection/authorization processes
    val isLoadingConnection: StateFlow<Boolean> = traktRepository.isLoading

    // Specific loading state for fetching favorite shows
    val isLoadingFavoriteShows: StateFlow<Boolean> = traktRepository.isLoadingFavoriteShows

    // Favorite shows data
    val favoriteShows: StateFlow<List<TraktUserListItem>> =
        traktRepository.traktFavoriteShows
            .map { list -> list.distinctBy { it.traktID } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    // Error state specifically for loading favorite shows
    val favoriteShowsError: StateFlow<String?> = traktRepository.favoriteShowsError

    // Combined loading state for the screen (true if either connection or favorites are loading)
    val isScreenLoading: StateFlow<Boolean> =
        combine(
            isLoadingConnection,
            isLoadingFavoriteShows,
        ) { connectionLoading, favoritesLoading ->
            connectionLoading || favoritesLoading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    private val _uiState = MutableStateFlow(TraktAccountUiState())
    val uiState: StateFlow<TraktAccountUiState> = _uiState.asStateFlow()

    private val _openCustomTab = Channel<String>()
    val openCustomTab = _openCustomTab.receiveAsFlow()

    val favoriteShowsEmpty: StateFlow<Boolean> =
        favoriteShows
            .map { it.isEmpty() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    fun onConnectToTraktClick() {
        viewModelScope.launch {
            _openCustomTab.send(TraktConstants.TRAKT_AUTH_URL)
        }
    }

    fun onDisconnectFromTraktClick() {
        _uiState.value = _uiState.value.copy(confirmDisconnectFromTrakt = true)
    }

    fun onDisconnectFromTraktRefused() {
        _uiState.value = _uiState.value.copy(confirmDisconnectFromTrakt = false)
    }

    fun onDisconnectConfirm() {
        _uiState.value =
            _uiState.value.copy(
                isDisconnecting = true,
                confirmDisconnectFromTrakt = false,
                disconnectionError = null,
            )
        viewModelScope.launch {
            try {
                traktRepository.clearFavorites()

                val currentToken = traktAccessToken.value
                val accessToken = currentToken?.access_token
                if (accessToken != null) {
                    val result = traktRepository.revokeTraktAccessToken(accessToken)

                    if (result.isFailure) {
                        Timber.e(
                            "Failed to revoke Trakt access token during disconnect. Result: $result",
                        )
                        _uiState.value =
                            _uiState.value.copy(
                                disconnectionError = TraktConnectionError.DISCONNECT_FAILED.message,
                            )
                    }
                } else {
                    Timber.w("Attempted to disconnect without a valid access token.")
                    _uiState.value =
                        _uiState.value.copy(
                            disconnectionError = TraktConnectionError.DISCONNECT_FAILED.message, // Or a more specific error
                        )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during disconnection process.")
                _uiState.value =
                    _uiState.value.copy(
                        disconnectionError = TraktConnectionError.DISCONNECT_FAILED.message,
                    )
            } finally {
                _uiState.value = _uiState.value.copy(isDisconnecting = false)
            }
        }
    }

    fun onCodeReceived(code: String?) {
        if (!code.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(connectionError = null)
            viewModelScope.launch {
                val result = traktRepository.getTraktAccessToken(code)
                if (result.isFailure) {
                    Timber.e(
                        "Failed to get Trakt access token with code. Result: $result",
                    )
                    _uiState.value =
                        _uiState.value.copy(
                            connectionError = TraktConnectionError.TOKEN_EXCHANGE_FAILED.message,
                        )
                }
            }
        } else {
            _uiState.value =
                _uiState.value.copy(
                    connectionError = TraktConnectionError.INVALID_AUTH_CODE.message,
                )
        }
    }

    fun clearConnectionError() {
        _uiState.value = _uiState.value.copy(connectionError = null)
    }

    fun clearDisconnectionError() {
        _uiState.value = _uiState.value.copy(disconnectionError = null)
    }

    fun clearFavoriteShowsError() {
        // This is tricky because favoriteShowsError comes directly from the repository.
        // The repository would need a method to clear its own error.
        // For now, the UI can just choose to hide the error after a timeout or user action.
        // Or, if truly needed, add:
        // viewModelScope.launch { traktRepository.clearFavoriteShowsError() }
        // And implement `clearFavoriteShowsError()` in TraktRepository/Impl
    }
}
