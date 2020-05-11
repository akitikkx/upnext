package com.theupnextapp.ui.history

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : TraktViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)

    private val traktRepository = TraktRepository(database)

    private val _launchTraktConnectWindow = MutableLiveData<Boolean>()

    private val _fetchingAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _storingTraktAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _transactionInProgress = MutableLiveData<Boolean>()

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    private val _historyEmpty = MutableLiveData<Boolean>()

    val isLoadingHistory = traktRepository.isLoadingTraktHistory

    val launchTraktConnectWindow: LiveData<Boolean> = _launchTraktConnectWindow

    val fetchAccessTokenInProgress: LiveData<Boolean> = _fetchingAccessTokenInProgress

    val storingTraktAccessTokenInProgress: LiveData<Boolean> = _storingTraktAccessTokenInProgress

    val transactionInProgress: LiveData<Boolean> = _transactionInProgress

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val historyEmpty: LiveData<Boolean>
        get() = _historyEmpty

    fun onHistoryEmpty(empty: Boolean) {
        _historyEmpty.value = empty
    }

    fun onConnectClick() {
        _launchTraktConnectWindow.value = true
    }

    fun launchConnectWindowComplete() {
        _launchTraktConnectWindow.value = false
    }

    init {
        _historyEmpty.value = false
        if (ifValidAccessTokenExists()) {
            loadTraktHistory()
            _isAuthorizedOnTrakt.value = true
        }
    }

    private fun loadTraktHistory() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val accessToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)

        viewModelScope.launch {
            traktRepository.refreshTraktHistory(accessToken)
        }
    }

    val traktAccessToken = traktRepository.traktAccessToken

    val traktHistory = traktRepository.traktHistory

    fun onTraktConnectionBundleReceived(bundle: Bundle?) {
        _transactionInProgress.value = true
        extractCode(bundle)
    }

    private fun extractCode(bundle: Bundle?) {
        val traktConnectionArg = bundle?.getParcelable<TraktConnectionArg>(EXTRA_TRAKT_URI)

        _fetchingAccessTokenInProgress.value = true

        viewModelScope.launch {
            traktRepository.getTraktAccessToken(traktConnectionArg?.code)
        }
    }

    fun onTraktAccessTokenReceived(traktAccessToken: TraktAccessToken) {
        _fetchingAccessTokenInProgress.value = false
        _storingTraktAccessTokenInProgress.value = true

        storeTraktAccessToken(traktAccessToken)

        _transactionInProgress.value = false
        _isAuthorizedOnTrakt.value = true
        _storingTraktAccessTokenInProgress.value = false
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

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
    }

    class Factory(val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}