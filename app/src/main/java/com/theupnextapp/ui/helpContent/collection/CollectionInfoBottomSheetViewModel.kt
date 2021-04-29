package com.theupnextapp.ui.helpContent.collection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CollectionInfoBottomSheetViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application)