package com.theupnextapp.ui.showDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.repository.TraktRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ShowSeasonsBottomSheetViewModel @AssistedInject constructor(
    traktRepository: TraktRepository,
    @Assisted val showDetail: ShowInfo?
) : ViewModel() {

    val isLoading = traktRepository.isLoading

    @AssistedFactory
    interface ShowSeasonsBottomSheetViewModelFactory {
        fun create(showDetail: ShowInfo?): ShowSeasonsBottomSheetViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: ShowSeasonsBottomSheetViewModelFactory,
            showDetail: ShowInfo?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(showDetail) as T
            }
        }
    }
}