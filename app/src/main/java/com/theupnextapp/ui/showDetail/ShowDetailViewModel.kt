package com.theupnextapp.ui.showDetail

import androidx.lifecycle.*
import com.theupnextapp.domain.*
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.UpnextRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShowDetailViewModel @AssistedInject constructor(
    upnextRepository: UpnextRepository,
    private val traktRepository: TraktRepository,
    @Assisted show: ShowDetailArg
) : ViewModel() {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _show = MutableLiveData(show)
    val showDetailArg: LiveData<ShowDetailArg> = _show

    private val _showCastEmpty = MutableLiveData<Boolean>()
    val showCastEmpty: LiveData<Boolean> = _showCastEmpty

    private val _showCastBottomSheet = MutableLiveData<ShowCast?>()
    val showCastBottomSheet: LiveData<ShowCast?> = _showCastBottomSheet

    private val _navigateToSeasons = MutableLiveData<Boolean>()
    val navigateToSeasons: LiveData<Boolean> = _navigateToSeasons

    val isLoading = MediatorLiveData<Boolean>()

    private val isUpnextRepositoryLoading = upnextRepository.isLoading

    private val isTraktRepositoryLoading = traktRepository.isLoading

    val showInfo = upnextRepository.showInfo

    val showCast = upnextRepository.showCast

    val showSeasons = upnextRepository.showSeasons

    val showRating = traktRepository.traktShowRating

    val showStats = traktRepository.traktShowStats

    init {
        viewModelScope.launch {
            show.showId?.let {
                upnextRepository.getShowData(it)
                upnextRepository.getShowCast(it)
                upnextRepository.getShowSeasons(it)
            }
            traktRepository.getTraktShowRating(showInfo.value?.imdbID)
            traktRepository.getTraktShowStats(showInfo.value?.imdbID)
        }

        isLoading.addSource(isUpnextRepositoryLoading) { result ->
            isLoading.value = result == true
        }
        isLoading.addSource(isTraktRepositoryLoading) { result ->
            isLoading.value = result == true
        }
    }

    fun displayCastBottomSheetComplete() {
        _showCastBottomSheet.value = null
    }

    fun onShowCastInfoReceived(showCast: List<ShowCast>) {
        _showCastEmpty.value = showCast.isNullOrEmpty()
    }

    fun onShowCastItemClicked(showCast: ShowCast) {
        _showCastBottomSheet.value = showCast
    }

    fun onSeasonsClick() {
        _navigateToSeasons.value = true
    }

    fun onSeasonsNavigationComplete() {
        _navigateToSeasons.value = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    @AssistedFactory
    interface ShowDetailViewModelFactory {
        fun create(show: ShowDetailArg): ShowDetailViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: ShowDetailViewModelFactory,
            show: ShowDetailArg
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(show) as T
            }
        }
    }
}