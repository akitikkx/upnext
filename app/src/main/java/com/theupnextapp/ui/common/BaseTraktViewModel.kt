package com.theupnextapp.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.areVariablesEmpty
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.datastore.UpnextDataStoreManager
import com.theupnextapp.work.RefreshFavoriteShowsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
open class BaseTraktViewModel @Inject constructor(
    private val traktRepository: TraktRepository,
    private val upnextDataStoreManager: UpnextDataStoreManager,
    private val workManager: WorkManager
): ViewModel() {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val traktAccessToken = traktRepository.traktAccessToken

    val prefTraktAccessToken = upnextDataStoreManager.traktAccessTokenFlow.asLiveData()

    val _isAuthorizedOnTrakt = MutableLiveData<Boolean>()
    val isAuthorizedOnTrakt: LiveData<Boolean> = _isAuthorizedOnTrakt

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

    fun clearTraktPreferences() {
        viewModelScope.launch {
            upnextDataStoreManager.apply {
                saveTraktAccessToken("")
                saveTraktAccessTokenCreatedAt(UpnextDataStoreManager.NOT_FOUND)
                saveTraktAccessTokenExpiredIn(UpnextDataStoreManager.NOT_FOUND)
                saveTraktAccessScope("")
                saveTraktAccessTokenType("")
                saveRefreshTraktAccessToken("")
            }
        }
    }

    fun revokeTraktAccessToken() {
        val traktAccessToken: TraktAccessToken?
        runBlocking(Dispatchers.IO) {
            traktAccessToken = upnextDataStoreManager.traktAccessTokenFlow.first()
        }
        if (traktAccessToken != null) {
            if (!traktAccessToken.access_token.isNullOrEmpty() && traktAccessToken.isTraktAccessTokenValid()) {
                viewModelScope.launch {
                    traktRepository.revokeTraktAccessToken(traktAccessToken)
                }
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
}