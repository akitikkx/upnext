package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.TraktAddToHistory
import com.theupnextapp.domain.TraktRemoveFromHistory
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class ShowSeasonsBottomSheetViewModel(
    application: Application,
    val showDetail: ShowInfo?
) : TraktViewModel(application) {

    fun onAddSeasonClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_ADD, showSeason)
    }

    fun onRemoveSeasonClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_REMOVE, showSeason)
    }

    val isLoading = traktRepository.isLoading

    val addToHistoryResponse = traktRepository.addToHistoryResponse

    val removeFromHistoryResponse = traktRepository.removeFromHistoryResponse

    val watchedProgress = traktRepository.traktWatchedProgress

    fun onAddToHistoryResponseReceived(addToHistory: TraktAddToHistory) {
        viewModelScope?.launch {
            if (_isAuthorizedOnTrakt.value == true) {
                traktRepository.getTraktWatchedProgress(
                    getAccessToken(),
                    showDetail?.imdbID
                )
            }
        }
    }

    fun onRemoveFromHistoryResponseReceived(removeFromHistory: TraktRemoveFromHistory) {
        viewModelScope?.launch {
            if (_isAuthorizedOnTrakt.value == true) {
                traktRepository.getTraktWatchedProgress(
                    getAccessToken(),
                    showDetail?.imdbID
                )
            }
        }
    }

    private fun onSeasonHistoryAction(action: String, showSeason: ShowSeason) {
        if (ifValidAccessTokenExists()) {
            _isAuthorizedOnTrakt.value = true
            val accessToken = getAccessToken()

            when (action) {
                HISTORY_ACTION_ADD -> {
                    viewModelScope?.launch {
                        showDetail?.imdbID?.let { imdbID ->
                            traktRepository.traktAddSeasonToHistory(
                                accessToken = accessToken,
                                imdbID = imdbID,
                                showSeason = showSeason
                            )
                        }
                    }
                }
                HISTORY_ACTION_REMOVE -> {
                    viewModelScope?.launch {
                        showDetail?.imdbID.let { imdbID ->
                            if (imdbID != null) {
                                traktRepository.traktRemoveSeasonFromHistory(
                                    accessToken = accessToken,
                                    imdbID = imdbID,
                                    showSeason = showSeason
                                )
                            }
                        }
                    }
                }
            }
        } else {
            _isAuthorizedOnTrakt.value = false
        }
    }

    companion object {
        const val HISTORY_ACTION_ADD = "add_to_history"
        const val HISTORY_ACTION_REMOVE = "remove_from_history"
    }


    class Factory(
        val app: Application,
        val showDetail: ShowInfo?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShowSeasonsBottomSheetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShowSeasonsBottomSheetViewModel(
                    app,
                    showDetail
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}