package com.theupnextapp.ui.showSeasons

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.repository.ShowDetailRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ShowSeasonsViewModel constructor(
    savedStateHandle: SavedStateHandle,
    showDetailRepository: ShowDetailRepository,
    show: ShowDetailArg
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _showSeasons = MutableLiveData<List<ShowSeason>?>()
    val showSeasons: LiveData<List<ShowSeason>?> = _showSeasons

    init {
        savedStateHandle.set(SHOW_ID, show.showId)
        viewModelScope.launch {
            savedStateHandle.get<Int>(SHOW_ID)?.let {
                showDetailRepository.getShowSeasons(it).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _showSeasons.value = result.data
                        }
                        is Result.Loading -> {
                            _isLoading.value = result.status
                        }
                        else -> {}
                    }
                }
            }
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

    @Suppress("UNCHECKED_CAST")
    class Factory @AssistedInject constructor(
        @Assisted owner: SavedStateRegistryOwner,
        private val repository: ShowDetailRepository,
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