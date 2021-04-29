package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktShowWatchedProgress
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ShowWatchedProgressBottomSheetViewModel @AssistedInject constructor(
    application: Application,
    @Assisted watchedProgressInfo: TraktShowWatchedProgress?,
    @Assisted showDetail: ShowDetailArg?
) : AndroidViewModel(application) {

    private val _watchedProgress = MutableLiveData<TraktShowWatchedProgress>(watchedProgressInfo)

    private val _showInfo = MutableLiveData<ShowDetailArg>(showDetail)

    val watchedProgress: LiveData<TraktShowWatchedProgress> = _watchedProgress

    val showInfo: LiveData<ShowDetailArg> = _showInfo

    @AssistedFactory
    interface ShowWatchedProgressBottomSheetViewModelFactory {
        fun create(
            watchedProgressInfo: TraktShowWatchedProgress?,
            showDetail: ShowDetailArg?
        ): ShowWatchedProgressBottomSheetViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: ShowWatchedProgressBottomSheetViewModelFactory,
            watchedProgressInfo: TraktShowWatchedProgress?,
            showDetail: ShowDetailArg?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(watchedProgressInfo, showDetail) as T
            }
        }
    }
}