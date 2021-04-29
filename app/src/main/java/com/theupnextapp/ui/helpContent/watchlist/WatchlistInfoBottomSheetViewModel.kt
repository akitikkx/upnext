package com.theupnextapp.ui.helpContent.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WatchlistInfoBottomSheetViewModel @Inject constructor(application: Application) : AndroidViewModel(application)