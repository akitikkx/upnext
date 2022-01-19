package com.theupnextapp.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowSearch
import com.theupnextapp.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application,
    private val searchRepository: SearchRepository
) : AndroidViewModel(application) {

    private val _searchResponse = MutableLiveData<Result<List<ShowSearch>>>()
    val searchResponse: LiveData<Result<List<ShowSearch>>> = _searchResponse

    fun onQueryTextSubmit(query: String?) {
        viewModelScope.launch {
            searchRepository.getShowSearchResults(query).collect {
                _searchResponse.value = it
            }
        }
    }

    fun onQueryTextChange(newText: String?) {
        viewModelScope.launch {
            searchRepository.getShowSearchResults(newText).collect {
                _searchResponse.value = it
            }
        }
    }
}