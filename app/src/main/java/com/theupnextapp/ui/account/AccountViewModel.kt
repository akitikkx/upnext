package com.theupnextapp.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theupnextapp.repository.ProviderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    providerManager: ProviderManager
) : ViewModel() {
    val activeProvider: StateFlow<String> = providerManager.activeProvider.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProviderManager.PROVIDER_TRAKT
    )
}
