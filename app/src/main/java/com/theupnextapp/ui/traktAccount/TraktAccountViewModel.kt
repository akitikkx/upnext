package com.theupnextapp.ui.traktAccount

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.datastore.UpnextDataStoreManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TraktAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val traktRepository: TraktRepository,
    private val upnextDataStoreManager: UpnextDataStoreManager
) : ViewModel() {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val isLoading = traktRepository.isLoading

    val traktAccessToken = traktRepository.traktAccessToken

    val prefTraktAccessToken = upnextDataStoreManager.traktAccessTokenFlow.asLiveData()

    private val _isAuthorizedOnTrakt = MutableLiveData<Boolean>()
    val isAuthorizedOnTrakt: LiveData<Boolean> = _isAuthorizedOnTrakt

    private val _openCustomTab = MutableLiveData<Boolean>()
    val openCustomTab: LiveData<Boolean> = _openCustomTab

    fun onConnectToTraktClick() {
        _openCustomTab.postValue(true)
    }

    fun onCustomTabOpened() {
        _openCustomTab.postValue(false)
    }

    fun onCodeReceived(code: String?) {
        if (!code.isNullOrEmpty()) {
            viewModelScope.launch {
                traktRepository.getTraktAccessToken(code)
            }
        }
    }

    fun onAccessTokenReceived(traktAccessToken: TraktAccessToken) {
        viewModelScope.launch {
            traktAccessToken.access_token?.let { upnextDataStoreManager.saveTraktAccessToken(it) }
            traktAccessToken.created_at?.let { upnextDataStoreManager.saveTraktAccessTokenCreatedAt(it) }
            traktAccessToken.expires_in?.let { upnextDataStoreManager.saveTraktAccessTokenExpiredIn(it) }
            traktAccessToken.scope?.let { upnextDataStoreManager.saveTraktAccessScope(it) }
            traktAccessToken.token_type?.let { upnextDataStoreManager.saveTraktAccessTokenType(it) }
            traktAccessToken.refresh_token?.let { upnextDataStoreManager.saveRefreshTraktAccessToken(it) }
        }
    }

    fun onPrefAccessTokenRetrieved(accessToken: TraktAccessToken) {

    }

    @AssistedFactory
    interface TraktAccountViewModelFactory {
        fun create(
            owner: SavedStateRegistryOwner
        ): Factory
    }

    class Factory @AssistedInject constructor(
        @Assisted owner: SavedStateRegistryOwner,
        private val traktRepository: TraktRepository,
        private val upnextDataStoreManager: UpnextDataStoreManager
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return TraktAccountViewModel(handle, traktRepository, upnextDataStoreManager) as T
        }
    }

}