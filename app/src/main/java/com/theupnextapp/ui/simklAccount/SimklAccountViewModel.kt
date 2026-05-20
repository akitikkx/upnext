package com.theupnextapp.ui.simklAccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theupnextapp.common.utils.SimklConstants
import com.theupnextapp.datasource.SimklAuthDataSource
import com.theupnextapp.domain.SimklAccessToken
import com.theupnextapp.repository.SimklAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SimklAccountUiState(
    val isConnecting: Boolean = false,
    val connectionError: String? = null,
    val isDisconnecting: Boolean = false,
    val disconnectionError: String? = null,
    val confirmDisconnectFromSimkl: Boolean = false,
)

@HiltViewModel
class SimklAccountViewModel @Inject constructor(
    private val simklAuthDataSource: SimklAuthDataSource,
    private val simklAuthManager: SimklAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimklAccountUiState())
    val uiState: StateFlow<SimklAccountUiState> = _uiState.asStateFlow()

    private val _openCustomTab = Channel<String>()
    val openCustomTab = _openCustomTab.receiveAsFlow()

    val simklAccessToken: StateFlow<SimklAccessToken?> =
        simklAuthManager.simklAccessToken.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onConnectToSimklClick() {
        viewModelScope.launch {
            _openCustomTab.send(SimklConstants.SIMKL_AUTH_URL)
        }
    }

    fun onCodeReceived(code: String?) {
        if (!code.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(connectionError = null, isConnecting = true)
            viewModelScope.launch {
                val result = simklAuthDataSource.getAccessToken(code)
                if (result.isFailure) {
                    Timber.e("Failed to get SIMKL access token with code. Result: $result")
                    _uiState.value = _uiState.value.copy(
                        connectionError = "Failed to exchange authorization code for access token.",
                        isConnecting = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isConnecting = false)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                connectionError = "Invalid authorization code received.",
                isConnecting = false
            )
        }
    }

    fun clearConnectionError() {
        _uiState.value = _uiState.value.copy(connectionError = null)
    }

    fun clearDisconnectionError() {
        _uiState.value = _uiState.value.copy(disconnectionError = null)
    }
}
