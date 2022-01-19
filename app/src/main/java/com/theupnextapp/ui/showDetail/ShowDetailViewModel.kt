package com.theupnextapp.ui.showDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import com.theupnextapp.work.AddFavoriteShowWorker
import com.theupnextapp.work.RemoveFavoriteShowWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ShowDetailViewModel @AssistedInject constructor(
    private val showDetailRepository: ShowDetailRepository,
    private val workManager: WorkManager,
    private val traktRepository: TraktRepository,
    @Assisted show: ShowDetailArg
) : BaseTraktViewModel(
    traktRepository,
    workManager
) {

    private val _show = MutableLiveData(show)
    val showDetailArg: LiveData<ShowDetailArg> = _show

    private val _showCastEmpty = MutableLiveData<Boolean>()
    val showCastEmpty: LiveData<Boolean> = _showCastEmpty

    private val _showCastBottomSheet = MutableLiveData<ShowCast?>()
    val showCastBottomSheet: LiveData<ShowCast?> = _showCastBottomSheet

    private val _navigateToSeasons = MutableLiveData<Boolean>()
    val navigateToSeasons: LiveData<Boolean> = _navigateToSeasons

    private val _showSummary = MutableLiveData<ShowDetailSummary?>()
    val showSummary: LiveData<ShowDetailSummary?> = _showSummary

    private val _showPreviousEpisode = MutableLiveData<ShowPreviousEpisode?>()
    val showPreviousEpisode: LiveData<ShowPreviousEpisode?> = _showPreviousEpisode

    private val _showNextEpisode = MutableLiveData<ShowNextEpisode?>()
    val showNextEpisode: LiveData<ShowNextEpisode?> = _showNextEpisode

    val isLoading = MediatorLiveData<Boolean>()

    val isFavoriteShow = MediatorLiveData<Boolean>()

    private val isUpnextRepositoryLoading = showDetailRepository.isLoading

    private val isTraktRepositoryLoading = traktRepository.isLoading

    private val _showCast = MutableLiveData<List<ShowCast>?>()
    val showCast: LiveData<List<ShowCast>?> = _showCast

    private val _showSeasons = MutableLiveData<List<ShowSeason>?>()
    val showSeasons: LiveData<List<ShowSeason>?> = _showSeasons

    val showRating = traktRepository.traktShowRating

    val showStats = traktRepository.traktShowStats

    private val favoriteShow = traktRepository.favoriteShow

    init {
        getShowSummary(show)

        isLoading.addSource(isUpnextRepositoryLoading) { result ->
            isLoading.value = result == true
        }

        isLoading.addSource(isTraktRepositoryLoading) { result ->
            isLoading.value = result == true
        }

        isFavoriteShow.addSource(favoriteShow) { result ->
            isFavoriteShow.value = result != null
        }
    }

    private fun getShowSummary(show: ShowDetailArg) {
        viewModelScope.launch {
            show.showId?.let {
                showDetailRepository.getShowSummary(it).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val showSummary = result.data
                            _showSummary.value = showSummary
                            getShowPreviousEpisode(showSummary.previousEpisodeHref)
                            getShowNextEpisode(showSummary.nextEpisodeHref)
                            getTraktShowRating(showSummary.imdbID)
                            getTraktShowStats(showSummary.imdbID)
                            checkIfShowIsTraktFavorite(showSummary.imdbID)
                        }
                        is Result.Loading -> {
                            isLoading.value = result.status
                        }
                        else -> {}
                    }

                }
                getShowCast(it)
                getShowSeasons(it)
            }
        }
    }

    private fun getShowCast(showId: Int) {
        viewModelScope.launch {
            showDetailRepository.getShowCast(showId).collect { response ->
                when (response) {
                    is Result.Success -> {
                        _showCast.value = response.data
                    }
                    is Result.Loading -> {
                        isLoading.value = response.status
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getShowSeasons(showId: Int) {
        viewModelScope.launch {
            showDetailRepository.getShowSeasons(showId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _showSeasons.value = result.data
                    }
                    is Result.Loading -> {
                        isLoading.value = result.status
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getShowPreviousEpisode(previousEpisodeHref: String?) {
        viewModelScope.launch {
            showDetailRepository.getPreviousEpisode(previousEpisodeHref).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _showPreviousEpisode.value = result.data
                    }
                    is Result.Loading -> {
                        isLoading.value = result.status
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getShowNextEpisode(nextEpisodeHref: String?) {
        viewModelScope.launch {
            showDetailRepository.getNextEpisode(nextEpisodeHref).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _showNextEpisode.value = result.data
                    }
                    is Result.Loading -> {
                        isLoading.value = result.status
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getTraktShowRating(imdbID: String?) {
        viewModelScope.launch {
            traktRepository.getTraktShowRating(imdbID)
        }
    }

    private fun getTraktShowStats(imdbID: String?) {
        viewModelScope.launch {
            traktRepository.getTraktShowStats(imdbID)
        }
    }

    private fun checkIfShowIsTraktFavorite(imdbID: String?) {
        viewModelScope.launch {
            traktRepository.checkIfShowIsFavorite(imdbID)
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

    fun onAddRemoveFavoriteClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val accessToken = traktAccessToken.value
            if (accessToken != null) {
                if (favoriteShow.value != null) {
                    val traktUserListItem = favoriteShow.value
                    val workerData = Data.Builder()
                    traktUserListItem?.traktID?.let {
                        workerData.putInt(
                            RemoveFavoriteShowWorker.ARG_TRAKT_ID,
                            it
                        )
                    }
                    workerData.putString(
                        RemoveFavoriteShowWorker.ARG_IMDB_ID,
                        traktUserListItem?.imdbID
                    )
                    workerData.putString(
                        RemoveFavoriteShowWorker.ARG_TOKEN,
                        accessToken.access_token
                    )

                    val removeFavoriteWork =
                        OneTimeWorkRequest.Builder(RemoveFavoriteShowWorker::class.java)
                    removeFavoriteWork.setInputData(workerData.build())

                    workManager.enqueue(removeFavoriteWork.build())
                } else {
                    val workerData = Data.Builder()
                    workerData.putString(
                        AddFavoriteShowWorker.ARG_IMDB_ID,
                        showSummary.value?.imdbID
                    )
                    workerData.putString(AddFavoriteShowWorker.ARG_TOKEN, accessToken.access_token)

                    val addFavoriteWork =
                        OneTimeWorkRequest.Builder(AddFavoriteShowWorker::class.java)
                    addFavoriteWork.setInputData(workerData.build())

                    workManager.enqueue(addFavoriteWork.build())
                }
            }
        }
    }

    fun onSeasonsNavigationComplete() {
        _navigateToSeasons.value = false
    }

    @AssistedFactory
    interface ShowDetailViewModelFactory {
        fun create(show: ShowDetailArg): ShowDetailViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: ShowDetailViewModelFactory,
            show: ShowDetailArg
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(show) as T
            }
        }
    }
}