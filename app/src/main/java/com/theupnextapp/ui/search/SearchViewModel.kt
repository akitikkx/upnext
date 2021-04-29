package com.theupnextapp.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.theupnextapp.repository.UpnextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application,
    private val upnextRepository: UpnextRepository
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val searchResults = upnextRepository.showSearch

    fun onQueryTextSubmit(query: String?) {
        viewModelScope.launch {
            upnextRepository.getSearchSuggestions(query)
        }
    }

    fun onQueryTextChange(newText: String?) {
        viewModelScope.launch {
            upnextRepository.getSearchSuggestions(newText)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}