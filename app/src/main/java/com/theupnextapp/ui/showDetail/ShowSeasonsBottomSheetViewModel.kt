package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.common.utils.UpnextPreferenceManager
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

    val isLoading = traktRepository.isLoading

    val addToHistoryResponse = traktRepository.addToHistoryResponse

    val removeFromHistoryResponse = traktRepository.removeFromHistoryResponse

    val watchedProgress = traktRepository.traktWatchedProgress

    fun onAddSeasonClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_ADD, showSeason)
    }

    fun onRemoveSeasonClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_REMOVE, showSeason)
    }

    fun onAddToHistoryResponseReceived(addToHistory: TraktAddToHistory) {
        viewModelScope?.launch {
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.getTraktWatchedProgress(
                    UpnextPreferenceManager(getApplication()).getTraktAccessToken(),
                    showDetail?.imdbID
                )
            }
        }
    }

    fun onRemoveFromHistoryResponseReceived(removeFromHistory: TraktRemoveFromHistory) {
        viewModelScope?.launch {
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.getTraktWatchedProgress(
                    UpnextPreferenceManager(getApplication()).getTraktAccessToken(),
                    showDetail?.imdbID
                )
            }
        }
    }

    private fun onSeasonHistoryAction(action: String, showSeason: ShowSeason) {
        if (isAuthorizedOnTrakt.value == true) {
            val accessToken = UpnextPreferenceManager(getApplication()).getTraktAccessToken()

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