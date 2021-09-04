package com.theupnextapp.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.theupnextapp.repository.UpnextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application,
    private val upnextRepository: UpnextRepository
) : AndroidViewModel(application) {

    val searchResults = upnextRepository.showSearch

    fun onQueryTextSubmit(query: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            upnextRepository.getSearchSuggestions(query)
        }
    }

    fun onQueryTextChange(newText: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            upnextRepository.getSearchSuggestions(newText)
        }
    }
}