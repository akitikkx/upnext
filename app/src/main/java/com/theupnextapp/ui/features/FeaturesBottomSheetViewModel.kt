package com.theupnextapp.ui.features

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeaturesBottomSheetViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {

    fun onGotItClick() {

    }
}