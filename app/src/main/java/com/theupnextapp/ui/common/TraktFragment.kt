package com.theupnextapp.ui.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.BuildConfig
import com.theupnextapp.ui.collection.CollectionFragment
import com.theupnextapp.ui.showDetail.ShowDetailFragment

open class TraktFragment : BaseFragment() {

    private val viewModel: TraktViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@TraktFragment,
            TraktViewModel.Factory(activity.application)
        ).get(TraktViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.launchTraktConnectWindow.observe(viewLifecycleOwner, Observer {
            if (it) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("${CollectionFragment.TRAKT_API_URL}${CollectionFragment.TRAKT_OAUTH_ENDPOINT}?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}")
                )
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, activity?.packageName)
                startActivity(intent)
                viewModel.launchConnectWindowComplete()
            }
        })

        viewModel.traktAccessToken.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.onTraktAccessTokenReceived(it)
            }
        })

        viewModel.invalidToken.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.onInvalidTokenResponseReceived(it)
            }
        })

        viewModel.invalidGrant.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.onInvalidTokenResponseReceived(it)
            }
        })
    }

    protected fun connectToTraktWindow() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("${ShowDetailFragment.TRAKT_API_URL}${ShowDetailFragment.TRAKT_OAUTH_ENDPOINT}?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}")
        )
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, activity?.packageName)
        startActivity(intent)
        viewModel.launchConnectWindowComplete()
    }
}