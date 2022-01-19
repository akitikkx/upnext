package com.theupnextapp.ui.showSeasonEpisodes

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import androidx.work.WorkManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ShowSeasonEpisodesViewModel(
    savedStateHandle: SavedStateHandle,
    showDetailRepository: ShowDetailRepository,
    private val traktRepository: TraktRepository,
    workManager: WorkManager,
    showSeasonEpisodesArg: ShowSeasonEpisodesArg
) : BaseTraktViewModel(
    traktRepository,
    workManager
) {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _episodes = MutableLiveData<List<ShowSeasonEpisode>?>()
    val episodes: LiveData<List<ShowSeasonEpisode>?> = _episodes

    val traktCheckInStatus = traktRepository.traktCheckInStatus

    private val _seasonNumber = MutableLiveData<Int?>(showSeasonEpisodesArg.seasonNumber)
    val seasonNumber: LiveData<Int?> = _seasonNumber

    private val _confirmCheckIn = MutableLiveData<ShowSeasonEpisode?>()
    val confirmCheckIn: LiveData<ShowSeasonEpisode?> = _confirmCheckIn

    init {
        savedStateHandle.set(SEASON_NUMBER, showSeasonEpisodesArg.seasonNumber)
        savedStateHandle.set(SHOW_ID, showSeasonEpisodesArg.showId)

        viewModelScope.launch {
            savedStateHandle.get<Int>(SEASON_NUMBER)?.let { seasonNumber ->
                savedStateHandle.get<Int>(SHOW_ID)?.let { showId ->
                    showDetailRepository.getShowSeasonEpisodes(
                        showId = showId,
                        seasonNumber = seasonNumber
                    ).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _episodes.value = result.data
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
    }

    fun onCheckInClick(showSeasonEpisode: ShowSeasonEpisode, imdbID: String?) {
        showSeasonEpisode.imdbID = imdbID
        _confirmCheckIn.value = showSeasonEpisode
    }

    fun onCheckInConfirm(showSeasonEpisode: ShowSeasonEpisode) {
        viewModelScope.launch {
            traktRepository.checkInToShow(showSeasonEpisode, traktAccessToken.value?.access_token)
            onCheckInComplete()
        }
    }

    private fun onCheckInComplete() {
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
        private val repository: ShowDetailRepository,
        private val traktRepository: TraktRepository,
        private val workManager: WorkManager,
        @Assisted private val showSeasonEpisodesArg: ShowSeasonEpisodesArg
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return ShowSeasonEpisodesViewModel(
                handle,
                repository,
                traktRepository,
                workManager,
                showSeasonEpisodesArg
            ) as T
        }
    }
}