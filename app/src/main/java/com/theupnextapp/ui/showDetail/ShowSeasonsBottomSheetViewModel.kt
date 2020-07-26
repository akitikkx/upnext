package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.common.utils.UpnextPreferenceManager
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

    fun onAddToCollectionResponseReceived(addToCollection: TraktAddToCollection) {
        viewModelScope?.launch {
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.refreshTraktCollection(
                    UpnextPreferenceManager(getApplication()).getTraktAccessToken()
                )
            }
        }
    }

    fun onRemoveFromCollectionResponseReceived(removeFromCollection: TraktRemoveFromCollection) {
        viewModelScope?.launch {
            if (isAuthorizedOnTrakt.value == true) {
                traktRepository.refreshTraktCollection(
                    UpnextPreferenceManager(getApplication()).getTraktAccessToken()
                )
            }
        }
    }

    private fun onSeasonCollectionAction(action: String, showSeason: ShowSeason) {
        if (isAuthorizedOnTrakt.value == true) {
            val accessToken = UpnextPreferenceManager(getApplication()).getTraktAccessToken()

            when (action) {
                COLLECTION_ACTION_ADD -> {
                    viewModelScope?.launch {
                        showDetail?.imdbID?.let { imdbID ->
                            traktRepository.traktAddSeasonToCollection(
                                accessToken = accessToken,
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