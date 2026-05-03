package com.theupnextapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theupnextapp.domain.SimklAccessToken
import com.theupnextapp.domain.Theme
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.repository.ProviderManager
import com.theupnextapp.repository.SettingsRepository
import com.theupnextapp.repository.SimklAuthManager
import com.theupnextapp.repository.SimklRepository
import com.theupnextapp.repository.TraktRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val traktRepository: TraktRepository,
        private val providerManager: ProviderManager,
        private val simklAuthManager: SimklAuthManager,
        private val simklRepository: SimklRepository,
    ) : ViewModel() {
        val themeStream: StateFlow<Theme> =
            settingsRepository.themeStream.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Theme.SYSTEM,
            )

        val dataSaverStream: StateFlow<Boolean> =
            settingsRepository.dataSaverStream.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

        val traktAccessToken: StateFlow<TraktAccessToken?> =
            traktRepository.traktAccessToken.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

        val simklAccessToken: StateFlow<SimklAccessToken?> =
            simklAuthManager.simklAccessToken.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

        val activeProvider: StateFlow<String> =
            providerManager.activeProvider.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ProviderManager.PROVIDER_TRAKT,
            )

        val areNotificationsEnabled: StateFlow<Boolean> =
            settingsRepository.areNotificationsEnabled.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

        fun onThemeSelected(theme: Theme) {
            viewModelScope.launch {
                settingsRepository.setTheme(theme)
            }
        }

        fun onDataSaverToggled(enabled: Boolean) {
            viewModelScope.launch {
                settingsRepository.setDataSaverEnabled(enabled)
            }
        }

        fun onNotificationsToggled(enabled: Boolean) {
            viewModelScope.launch {
                settingsRepository.setNotificationsEnabled(enabled)
            }
        }

        fun onProviderSelected(provider: String) {
            viewModelScope.launch {
                providerManager.setActiveProvider(provider)
            }
        }

        fun onDisconnectTrakt() {
            viewModelScope.launch {
                traktAccessToken.value?.let { token ->
                    traktRepository.revokeTraktAccessToken(token)
                    traktRepository.clearWatchlist()
                }
            }
        }

        fun onDisconnectSimkl() {
            viewModelScope.launch {
                simklRepository.clearSyncShows()
                // TODO: revoke access token for SIMKL if API allows, or simply delete local
            }
        }
    }
