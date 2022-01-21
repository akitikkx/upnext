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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchResponse = MutableLiveData<List<ShowSearch>>()
    val searchResponse: LiveData<List<ShowSearch>> = _searchResponse

    fun onQueryTextSubmit(query: String?) {
        handleQuery(query)
    }

    fun onQueryTextChange(newText: String?) {
        handleQuery(newText)
    }

    private fun handleQuery(query: String?) {
        viewModelScope.launch {
            searchRepository.getShowSearchResults(query).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _searchResponse.value = result.data.filter {
                            !it.originalImageUrl.isNullOrEmpty() && !it.mediumImageUrl.isNullOrEmpty()
                        }
                    }
                    is Result.Loading -> {
                        _isLoading.value = result.status
                    }
                    else -> {}
                }
            }
        }
    }
}