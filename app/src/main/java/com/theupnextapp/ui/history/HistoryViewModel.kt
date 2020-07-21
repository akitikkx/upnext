package com.theupnextapp.ui.history

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.domain.TraktHistory
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : TraktViewModel(application) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    val isLoadingHistory = traktRepository.isLoadingTraktHistory

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val historyEmpty = MediatorLiveData<Boolean>()

    val traktHistory = traktRepository.traktHistory

    val historyTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_HISTORY.tableName)

    init {
        historyEmpty.addSource(traktHistory) {
            historyEmpty.value = it.isNullOrEmpty() == true
        }
    }

    private fun loadTraktHistory() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val accessToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)

        viewModelScope?.launch {
            traktRepository.refreshTraktHistory(accessToken)
        }
    }

    fun displayShowDetails(showDetailArg: ShowDetailArg) {
        _navigateToSelectedShow.value = showDetailArg
    }

    fun displayShowDetailsComplete() {
        _navigateToSelectedShow.value = null
    }

    fun onHistoryTableUpdateReceived(tableUpdate: TableUpdate?) {
        if (isAuthorizedOnTrakt.value == false) {
            return
        }

        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.dateDifference(it, "minutes") }

        // Only perform an update if there has been enough time before the previous update
        if (diffInMinutes != null && historyEmpty.value != true) {
            if (diffInMinutes >= TableUpdateInterval.TRAKT_HISTORY_ITEMS.intervalMins) {
                loadTraktHistory()
            }
        } else if (historyEmpty.value == true) {
            loadTraktHistory()
        }
    }

    fun onRemoveClick(historyItem: TraktHistory) {

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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