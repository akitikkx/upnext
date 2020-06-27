package com.theupnextapp.ui.traktAuthentication

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.ui.common.TraktViewModel

class TraktAuthenticationViewModel(application: Application) : TraktViewModel(application) {


    class Factory(
        val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TraktAuthenticationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TraktAuthenticationViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}