package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktShowWatchedProgress

class ShowWatchedProgressBottomSheetViewModel(
    application: Application,
    watchedProgressInfo: TraktShowWatchedProgress?,
    showDetail: ShowDetailArg?
) : AndroidViewModel(application) {

    private val _watchedProgress = MutableLiveData<TraktShowWatchedProgress>(watchedProgressInfo)

    private val _showInfo = MutableLiveData<ShowDetailArg>(showDetail)

    val watchedProgress: LiveData<TraktShowWatchedProgress> = _watchedProgress

    val showInfo: LiveData<ShowDetailArg> = _showInfo

    class Factory(
        val app: Application,
        private val watchedProgressInfo: TraktShowWatchedProgress?,
        private val showDetail: ShowDetailArg?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShowWatchedProgressBottomSheetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShowWatchedProgressBottomSheetViewModel(
                    app,
                    watchedProgressInfo,
                    showDetail
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }

}