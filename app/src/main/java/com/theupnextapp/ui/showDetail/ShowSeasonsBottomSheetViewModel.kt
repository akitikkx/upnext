package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.TraktAddToHistory
import com.theupnextapp.domain.TraktRemoveFromHistory
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class ShowSeasonsBottomSheetViewModel @AssistedInject constructor(
    application: Application,
    private val traktRepository: TraktRepository,
    @Assisted val showDetail: ShowInfo?
) : TraktViewModel(application, traktRepository) {

    val isLoading = traktRepository.isLoading

    val addToHistoryResponse = traktRepository.addToHistoryResponse

    val removeFromHistoryResponse = traktRepository.removeFromHistoryResponse

    val watchedProgress = traktRepository.traktWatchedProgress

    fun onAddSeasonToHistoryClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_ADD, showSeason)
    }

    fun onRemoveSeasonFromHistoryClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_REMOVE, showSeason)
    }

    fun onAddToHistoryResponseReceived(addToHistory: TraktAddToHistory) {
        viewModelScope.launch {
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.getTraktWatchedProgress(
                    accessToken.value,
                    showDetail?.imdbID
                )
            }
        }
    }

    fun onRemoveFromHistoryResponseReceived(removeFromHistory: TraktRemoveFromHistory) {
        viewModelScope.launch {
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.getTraktWatchedProgress(
                    accessToken.value,
                    showDetail?.imdbID
                )
            }
        }
    }

    private fun onSeasonHistoryAction(action: String, showSeason: ShowSeason) {
        if (isAuthorizedOnTrakt.value == true) {

            when (action) {
                HISTORY_ACTION_ADD -> {
                    viewModelScope.launch {
                        showDetail?.imdbID?.let { imdbID ->
                            traktRepository.traktAddSeasonToHistory(
                                accessToken = accessToken.value,
                                imdbID = imdbID,
                                showSeason = showSeason
                            )
                        }
                    }
                }
                HISTORY_ACTION_REMOVE -> {
                    viewModelScope.launch {
                        showDetail?.imdbID.let { imdbID ->
                            if (imdbID != null) {
                                traktRepository.traktRemoveSeasonFromHistory(
                                    accessToken = accessToken.value,
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

    @AssistedFactory
    interface ShowSeasonsBottomSheetViewModelFactory {
        fun create(showDetail: ShowInfo?): ShowSeasonsBottomSheetViewModel
    }

    companion object {
        const val HISTORY_ACTION_ADD = "add_to_history"
        const val HISTORY_ACTION_REMOVE = "remove_from_history"

        fun provideFactory(
            assistedFactory: ShowSeasonsBottomSheetViewModelFactory,
            showDetail: ShowInfo?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(showDetail) as T
            }
        }
    }
}