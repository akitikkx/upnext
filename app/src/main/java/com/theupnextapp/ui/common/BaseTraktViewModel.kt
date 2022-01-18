package com.theupnextapp.ui.common

import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.RefreshFavoriteEpisodesWorker
import com.theupnextapp.work.RefreshFavoriteShowsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
open class BaseTraktViewModel @Inject constructor(
    private val traktRepository: TraktRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val traktAccessToken = traktRepository.traktAccessToken.asLiveData()

    private val _isAuthorizedOnTrakt = MediatorLiveData<Boolean>()
    val isAuthorizedOnTrakt: LiveData<Boolean> = _isAuthorizedOnTrakt

    init {
        viewModelScope.launch {
            _isAuthorizedOnTrakt.addSource(traktAccessToken) { accessToken ->
                _isAuthorizedOnTrakt.value =
                    accessToken?.access_token.isNullOrEmpty() == false && accessToken?.isTraktAccessTokenValid() == true

                if (accessToken?.isTraktAccessTokenValid() == false) {
                    viewModelScope.launch(Dispatchers.IO) {
                        traktRepository.getTraktAccessRefreshToken(accessToken.refresh_token)
                    }
                } else {
                    val workerData = Data.Builder().putString(
                        RefreshFavoriteShowsWorker.ARG_TOKEN,
                        accessToken?.access_token
                    )
                    val refreshFavoritesWork =
                        OneTimeWorkRequest.Builder(RefreshFavoriteShowsWorker::class.java)
                    refreshFavoritesWork.setInputData(workerData.build())

                    val refreshFavoriteEpisodesWork =
                        OneTimeWorkRequest.Builder(RefreshFavoriteEpisodesWorker::class.java)

                    workManager.enqueue(refreshFavoritesWork.build())
                    workManager.enqueue(refreshFavoriteEpisodesWork.build())
                }
            }
        }
    }

    fun revokeTraktAccessToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val accessToken = traktAccessToken.value
            if (accessToken != null) {
                if (!accessToken.access_token.isNullOrEmpty() && accessToken.isTraktAccessTokenValid()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        traktRepository.revokeTraktAccessToken(accessToken)
                    }
                }
            }
        }
    }
}