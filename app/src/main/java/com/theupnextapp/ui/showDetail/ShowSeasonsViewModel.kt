package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeason

class ShowSeasonsViewModel(
    application: Application,
    showDetail: ShowDetailArg?
) : AndroidViewModel(application) {

    fun onAddSeasonClick(showSeason: ShowSeason) {

    }

    fun onRemoveSeasonClick(showSeason: ShowSeason) {

    }

    class Factory(
        val app: Application,
        val showDetail: ShowDetailArg?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShowSeasonsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShowSeasonsViewModel(
                    app,
                    showDetail
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}