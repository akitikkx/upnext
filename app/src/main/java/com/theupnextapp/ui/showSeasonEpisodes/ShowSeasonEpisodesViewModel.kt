package com.theupnextapp.ui.showSeasonEpisodes

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.repository.UpnextRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowSeasonEpisodesViewModel(
    savedStateHandle: SavedStateHandle,
    upnextRepository: UpnextRepository,
    showSeasonEpisodesArg: ShowSeasonEpisodesArg
) : ViewModel() {

    val isLoading = upnextRepository.isLoading

    val episodes = upnextRepository.episodes

    private val _seasonNumber = MutableLiveData<Int?>(showSeasonEpisodesArg.seasonNumber)
    val seasonNumber: LiveData<Int?> = _seasonNumber

    private val _confirmCheckIn = MutableLiveData<ShowSeasonEpisode?>()
    val confirmCheckIn: LiveData<ShowSeasonEpisode?> = _confirmCheckIn

    init {
        savedStateHandle.set(SEASON_NUMBER, showSeasonEpisodesArg.seasonNumber)
        savedStateHandle.set(SHOW_ID, showSeasonEpisodesArg.showId)

        viewModelScope.launch(Dispatchers.IO) {
            savedStateHandle.get<Int>(SEASON_NUMBER)?.let { seasonNumber ->
                savedStateHandle.get<Int>(SHOW_ID)?.let { showId ->
                    upnextRepository.getShowSeasonEpisodes(
                        showId = showId,
                        seasonNumber = seasonNumber
                    )
                }
            }
        }
    }

    fun onCheckInClick(showSeasonEpisode: ShowSeasonEpisode) {
        _confirmCheckIn.value = showSeasonEpisode
    }

    fun onCheckInConfirm(showSeasonEpisode: ShowSeasonEpisode) {

    }

    fun onCheckInComplete() {
        _confirmCheckIn.value = null
    }

    @AssistedFactory
    interface ShowSeasonEpisodesViewModelFactory {
        fun create(
            owner: SavedStateRegistryOwner,
            showSeasonEpisodesArg: ShowSeasonEpisodesArg
        ): Factory
    }

    companion object {
        const val SHOW_ID = "showId"
        const val SEASON_NUMBER = "seasonNumber"
    }

    class Factory @AssistedInject constructor(
        @Assisted owner: SavedStateRegistryOwner,
        private val repository: UpnextRepository,
        @Assisted private val showSeasonEpisodesArg: ShowSeasonEpisodesArg
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return ShowSeasonEpisodesViewModel(handle, repository, showSeasonEpisodesArg) as T
        }
    }
}