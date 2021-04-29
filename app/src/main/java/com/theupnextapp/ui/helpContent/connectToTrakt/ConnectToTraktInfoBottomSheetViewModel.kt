package com.theupnextapp.ui.helpContent.connectToTrakt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectToTraktInfoBottomSheetViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application)