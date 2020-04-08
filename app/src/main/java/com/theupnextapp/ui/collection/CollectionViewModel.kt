package com.theupnextapp.ui.collection

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import com.theupnextapp.domain.TraktConnectionArg
import timber.log.Timber

class CollectionViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _launchConnectWindow = MutableLiveData<Boolean>()

    val launchConnectWindow: LiveData<Boolean>
        get() = _launchConnectWindow

    fun onConnectClick() {
        _launchConnectWindow.value = true
    }

    fun launchConnectWindowComplete() {
        _launchConnectWindow.value = false
    }

    fun onTraktConnectionBundleReceived(bundle : Bundle?){
        extractCode(bundle)
    }

    private fun extractCode(bundle: Bundle?) {
        val traktConnectionArg = bundle?.getParcelable<TraktConnectionArg>(EXTRA_TRAKT_URI)
        Timber.d(traktConnectionArg.toString())
    }

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
    }

    class Factory(val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CollectionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CollectionViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}