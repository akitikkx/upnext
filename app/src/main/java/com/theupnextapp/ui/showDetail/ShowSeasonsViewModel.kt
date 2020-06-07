package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class ShowSeasonsViewModel(
    application: Application,
    val showDetail: ShowInfo?
) : TraktViewModel(application) {

    fun onAddSeasonClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_ADD, showSeason)
    }

    fun onRemoveSeasonClick(showSeason: ShowSeason) {
        onSeasonHistoryAction(HISTORY_ACTION_REMOVE, showSeason)
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
            if (modelClass.isAssignableFrom(ShowSeasonsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShowSeasonsViewModel(
                    app,
                    showDetail
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}