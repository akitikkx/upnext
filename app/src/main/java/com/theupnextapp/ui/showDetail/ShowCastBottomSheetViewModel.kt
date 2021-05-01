package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.ShowCast
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ShowCastBottomSheetViewModel @AssistedInject constructor(
    application: Application,
    @Assisted castInfo: ShowCast?
) : AndroidViewModel(application) {

    private val _showCast = MutableLiveData<ShowCast>(castInfo)

    val showCast: LiveData<ShowCast>
        get() = _showCast

    @AssistedFactory
    interface ShowCastBottomSheetViewModelFactory {
        fun create(castInfo: ShowCast?): ShowCastBottomSheetViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: ShowCastBottomSheetViewModelFactory,
            castInfo: ShowCast?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(castInfo) as T
            }
        }
    }
}