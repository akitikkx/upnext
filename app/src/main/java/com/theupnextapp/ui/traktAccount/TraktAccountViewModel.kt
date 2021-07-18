package com.theupnextapp.ui.traktAccount

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import androidx.work.WorkManager
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.datastore.UpnextDataStoreManager
import com.theupnextapp.ui.common.BaseTraktViewModel
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
    upnextDataStoreManager: UpnextDataStoreManager,
    workManager: WorkManager
) : BaseTraktViewModel(
    traktRepository,
    upnextDataStoreManager,
    workManager
) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val isLoading = traktRepository.isLoading

    val favoriteShows = traktRepository.traktFavoriteShows

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
        viewModelScope.launch {
            revokeTraktAccessToken()
            clearTraktPreferences()
            traktRepository.clearFavorites()
        }
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
        @Suppress("UNCHECKED_CAST")
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