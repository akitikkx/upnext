package com.theupnextapp.ui.traktAccount

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.areVariablesEmpty
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.datastore.UpnextDataStoreManager
import com.theupnextapp.work.RefreshFavoriteShowsWorker
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
    private val upnextDataStoreManager: UpnextDataStoreManager,
    private val workManager: WorkManager
) : ViewModel() {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val isLoading = traktRepository.isLoading

    val traktAccessToken = traktRepository.traktAccessToken

    val favoriteShows = traktRepository.traktFavoriteShows

    val prefTraktAccessToken = upnextDataStoreManager.traktAccessTokenFlow.asLiveData()

    private val _isAuthorizedOnTrakt = MutableLiveData<Boolean>()
    val isAuthorizedOnTrakt: LiveData<Boolean> = _isAuthorizedOnTrakt

    private val _openCustomTab = MutableLiveData<Boolean>()
    val openCustomTab: LiveData<Boolean> = _openCustomTab

    private val _confirmDisconnectFromTrakt = MutableLiveData<Boolean>()
    val confirmDisconnectFromTrakt: LiveData<Boolean> = _confirmDisconnectFromTrakt

    val favoriteShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(favoriteShows) {
            value = it.isNullOrEmpty() == true
        }
    }

    fun onConnectToTraktClick() {
        _openCustomTab.postValue(true)
    }

    fun onDisconnectFromTraktClick() {
        _confirmDisconnectFromTrakt.postValue(true)
    }

    fun onDisconnectConfirm() {

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

    fun onAuthorizationConfirmation() {
        if (!prefTraktAccessToken.value?.access_token.isNullOrEmpty()) {
            val workerData = Data.Builder().putString(
                RefreshFavoriteShowsWorker.ARG_TOKEN,
                prefTraktAccessToken.value?.access_token
            )
            val refreshFavoritesWork =
                OneTimeWorkRequest.Builder(RefreshFavoriteShowsWorker::class.java)
            refreshFavoritesWork.setInputData(workerData.build())

            workManager.enqueue(refreshFavoritesWork.build())
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
        private val upnextDataStoreManager: UpnextDataStoreManager,
        private val workManager: WorkManager
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return TraktAccountViewModel(
                handle,
                traktRepository,
                upnextDataStoreManager,
                workManager
            ) as T
        }
    }
}