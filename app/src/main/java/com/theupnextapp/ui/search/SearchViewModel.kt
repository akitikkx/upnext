package com.theupnextapp.ui.search

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.repository.UpnextRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)

    private val upnextRepository = UpnextRepository(database)

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    val navigateToSelectedShow: LiveData<ShowDetailArg>
        get() = _navigateToSelectedShow

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

    fun displayShowDetails(showDetailArg: ShowDetailArg) {
        _navigateToSelectedShow.value = showDetailArg
    }

    fun displayShowDetailsComplete() {
        _navigateToSelectedShow.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}