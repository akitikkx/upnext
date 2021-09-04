package com.theupnextapp.ui.showSeasons

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.repository.UpnextRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowSeasonsViewModel constructor(
    savedStateHandle: SavedStateHandle,
    upnextRepository: UpnextRepository,
    show: ShowDetailArg
) : ViewModel() {

    val isLoading = upnextRepository.isLoading

    val showSeasons = upnextRepository.showSeasons

    init {
        savedStateHandle.set(SHOW_ID, show.showId)
        viewModelScope.launch(Dispatchers.IO) {
            savedStateHandle.get<Int>(SHOW_ID)?.let { upnextRepository.getShowSeasons(it) }
        }
    }

    @AssistedFactory
    interface ShowSeasonsViewModelFactory {
        fun create(
            owner: SavedStateRegistryOwner,
            showDetail: ShowDetailArg
        ): Factory
    }

    companion object {
        const val SHOW_ID = "showId"
    }

    class Factory @AssistedInject constructor(
        @Assisted owner: SavedStateRegistryOwner,
        private val repository: UpnextRepository,
        @Assisted private val showDetailArg: ShowDetailArg
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return ShowSeasonsViewModel(handle, repository, showDetailArg) as T
        }
    }
}