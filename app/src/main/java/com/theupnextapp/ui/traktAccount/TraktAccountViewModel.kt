package com.theupnextapp.ui.traktAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.theupnextapp.repository.TraktRepository

class TraktAccountViewModel(
    private val traktRepository: TraktRepository
) : ViewModel() {

    val isLoading = traktRepository.isLoading

    private val _openCustomTab = MutableLiveData<Boolean>()
    val openCustomTab: LiveData<Boolean> = _openCustomTab

    fun onConnectToTraktClick() {
        _openCustomTab.postValue(true)
    }

    fun onCustomTabOpened() {
        _openCustomTab.postValue(false)
    }

}