package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.ShowCast

class ShowCastBottomSheetViewModel(
    application: Application,
    castInfo: ShowCast?
) : AndroidViewModel(application) {

    private val _showCast = MutableLiveData<ShowCast>(castInfo)

    val showCast: LiveData<ShowCast>
        get() = _showCast

    class Factory(
        val app: Application,
        val castInfo: ShowCast?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShowCastBottomSheetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShowCastBottomSheetViewModel(
                    app,
                    castInfo
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}