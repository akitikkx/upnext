package com.theupnextapp.ui.traktAccount

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.areVariablesEmpty
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.datastore.UpnextDataStoreManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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

    fun onAccessTokenReceived(response: TraktAccessToken) {
        viewModelScope.launch {
            upnextDataStoreManager.apply {
                if (!response.areVariablesEmpty()) {
                    saveTraktAccessToken("")
                    saveTraktAccessTokenCreatedAt(UpnextDataStoreManager.NOT_FOUND)
                    saveTraktAccessTokenExpiredIn(UpnextDataStoreManager.NOT_FOUND)
                    saveTraktAccessScope("")
                    saveTraktAccessTokenType("")
                    saveRefreshTraktAccessToken("")

                    saveTraktAccessToken(response.access_token.orEmpty())
                    saveTraktAccessTokenCreatedAt(
                        response.created_at ?: UpnextDataStoreManager.NOT_FOUND
                    )
                    saveTraktAccessTokenExpiredIn(
                        response.expires_in ?: UpnextDataStoreManager.NOT_FOUND
                    )
                    saveTraktAccessScope(response.scope.orEmpty())
                    saveTraktAccessTokenType(response.token_type.orEmpty())
                    saveRefreshTraktAccessToken(response.refresh_token.orEmpty())
                }
            }
        }
    }

    fun onPrefAccessTokenRetrieved(accessToken: TraktAccessToken) {
        if (accessToken.areVariablesEmpty()) {
            _isAuthorizedOnTrakt.postValue(false)
            return
        }

        val currentDateEpoch = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        val expiryDateEpoch = accessToken.expires_in?.let { accessToken.created_at?.plus(it) }

        if (expiryDateEpoch != null) {
            if (currentDateEpoch >= expiryDateEpoch) {
                _isAuthorizedOnTrakt.postValue(false)
                viewModelScope.launch {
                    traktRepository.getTraktAccessRefreshToken(accessToken.refresh_token)
                }
            } else {
                _isAuthorizedOnTrakt.postValue(true)
            }
        }
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