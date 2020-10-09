package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.*
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class ShowSeasonsBottomSheetViewModel(
    application: Application,
    val showDetail: ShowInfo?
) : TraktViewModel(application) {

    val isLoading = traktRepository.isLoading

    val addToHistoryResponse = traktRepository.addToHistoryResponse

    val removeFromHistoryResponse = traktRepository.removeFromHistoryResponse

    val addToCollectionResponse = traktRepository.addToCollectionResponse

    val removeFromCollectionResponse = traktRepository.removeFromCollectionResponse

    val watchedProgress = traktRepository.traktWatchedProgress

    val traktCollectionSeasons =
        showDetail?.imdbID?.let { traktRepository.traktCollectionSeasons(it) }

    fun onAddSeasonToHistoryClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_ADD, showSeason)
    }

    fun onRemoveSeasonFromHistoryClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_REMOVE, showSeason)
    }

    fun onAddSeasonToCollectionClick(showSeason: ShowSeason) {
        onSeasonCollectionAction(COLLECTION_ACTION_ADD, showSeason)
    }

    fun onRemoveSeasonFromCollectionClick(showSeason: ShowSeason) {
        onSeasonCollectionAction(COLLECTION_ACTION_REMOVE, showSeason)
    }

    fun onAddToHistoryResponseReceived(addToHistory: TraktAddToHistory) {
        viewModelScope?.launch {
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.getTraktWatchedProgress(
                    accessToken.value,
                    showDetail?.imdbID
                )
            }
        }
    }

    fun onRemoveFromHistoryResponseReceived(removeFromHistory: TraktRemoveFromHistory) {
        viewModelScope?.launch {
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
                    viewModelScope?.launch {
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
                    viewModelScope?.launch {
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

    fun onAddToCollectionResponseReceived(addToCollection: TraktAddToCollection) {
        viewModelScope?.launch {
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.refreshTraktCollection(
                    accessToken.value
                )
            }
        }
    }

    fun onRemoveFromCollectionResponseReceived(removeFromCollection: TraktRemoveFromCollection) {
        viewModelScope?.launch {
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.refreshTraktCollection(
                    accessToken.value
                )
            }
        }
    }

    private fun onSeasonCollectionAction(action: String, showSeason: ShowSeason) {
        if (isAuthorizedOnTrakt.value == true) {

            when (action) {
                COLLECTION_ACTION_ADD -> {
                    viewModelScope?.launch {
                        showDetail?.imdbID?.let { imdbID ->
                            traktRepository.traktAddSeasonToCollection(
                                accessToken = accessToken.value,
                                imdbID = imdbID,
                                showSeason = showSeason
                            )
                        }
                    }
                }
                COLLECTION_ACTION_REMOVE -> {
                    viewModelScope?.launch {
                        showDetail?.imdbID.let { imdbID ->
                            if (imdbID != null) {
                                traktRepository.traktRemoveSeasonFromCollection(
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

    companion object {
        const val HISTORY_ACTION_ADD = "add_to_history"
        const val HISTORY_ACTION_REMOVE = "remove_from_history"
        const val COLLECTION_ACTION_ADD = "add_to_collection"
        const val COLLECTION_ACTION_REMOVE = "remove_from_collection"
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