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

enum class WatchlistSortOption {
    ADDED,
    TITLE,
    RELEASE_YEAR,
    RATING,
}

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
                            traktRepository.refreshWatchlist(it)
                        }
                    }
                }
            }
        }

        // General loading state for initial connection/authorization processes
        val isLoadingConnection: StateFlow<Boolean> = traktRepository.isLoading

        // Specific loading state for fetching watchlist shows
        val isLoadingWatchlistShows: StateFlow<Boolean> = traktRepository.isLoadingWatchlistShows

        private val _watchlistSearchQuery = MutableStateFlow("")
        val watchlistSearchQuery: StateFlow<String> = _watchlistSearchQuery.asStateFlow()

        private val _watchlistSortOption = MutableStateFlow(WatchlistSortOption.ADDED)
        val watchlistSortOption: StateFlow<WatchlistSortOption> = _watchlistSortOption.asStateFlow()

        // Watchlist shows data
        val watchlistShows: StateFlow<List<TraktUserListItem>> =
            combine(
                traktRepository.traktWatchlistShows,
                _watchlistSearchQuery,
                _watchlistSortOption,
            ) { shows, query, sortOption ->
                var filteredList = shows.distinctBy { it.traktID }

                if (query.isNotBlank()) {
                    filteredList =
                        filteredList.filter {
                            it.title?.contains(query, ignoreCase = true) == true
                        }
                }

                filteredList =
                    when (sortOption) {
                        WatchlistSortOption.ADDED -> filteredList
                        WatchlistSortOption.TITLE -> filteredList.sortedBy { it.title }
                        WatchlistSortOption.RELEASE_YEAR -> filteredList.sortedByDescending { it.year?.toIntOrNull() ?: 0 }
                        WatchlistSortOption.RATING -> filteredList.sortedByDescending { it.rating ?: 0.0 }
                    }

                filteredList
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        fun onSearchQueryChange(query: String) {
            _watchlistSearchQuery.value = query
        }

        fun onSortOptionChange(option: WatchlistSortOption) {
            _watchlistSortOption.value = option
        }

        // Error state specifically for loading watchlist shows
        val watchlistShowsError: StateFlow<String?> = traktRepository.watchlistShowsError

        // Combined loading state for the screen (true if either connection or watchlists are loading)
        val isScreenLoading: StateFlow<Boolean> =
            combine(
                isLoadingConnection,
                isLoadingWatchlistShows,
            ) { connectionLoading, watchlistsLoading ->
                connectionLoading || watchlistsLoading
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

        private val _uiState = MutableStateFlow(TraktAccountUiState())
        val uiState: StateFlow<TraktAccountUiState> = _uiState.asStateFlow()

        private val _openCustomTab = Channel<String>()
        val openCustomTab = _openCustomTab.receiveAsFlow()

        val watchlistShowsEmpty: StateFlow<Boolean> =
            watchlistShows
                .map { it.isEmpty() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = true,
                )

        fun onRemoveFromWatchlistClick(traktId: Int) {
            viewModelScope.launch {
                val token = traktAccessToken.value?.access_token
                if (!token.isNullOrEmpty()) {
                    traktRepository.removeFromWatchlist(traktId, token)
                    // No immediate refreshWatchlist needed — optimistic local
                    // delete in the repository gives instant UI feedback.
                }
            }
        }

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
                    traktRepository.clearWatchlist()

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

        fun clearWatchlistShowsError() {
            // This is tricky because watchlistShowsError comes directly from the repository.
            // The repository would need a method to clear its own error.
            // For now, the UI can just choose to hide the error after a timeout or user action.
            // Or, if truly needed, add:
            // viewModelScope.launch { traktRepository.clearWatchlistShowsError() }
            // And implement `clearWatchlistShowsError()` in TraktRepository/Impl
        }
    }
