package com.theupnextapp.ui.traktAuthentication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.BuildConfig
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentTraktAuthenticationBinding
import com.theupnextapp.ui.collection.CollectionFragment
import com.theupnextapp.ui.common.BaseFragment

class TraktAuthenticationFragment : BaseFragment() {

    private var _binding: FragmentTraktAuthenticationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TraktAuthenticationViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@TraktAuthenticationFragment,
            TraktAuthenticationViewModel.Factory(activity.application)
        ).get(TraktAuthenticationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTraktAuthenticationBinding.inflate(inflater)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val settingsItem = menu.findItem(R.id.menu_settings)
        if (settingsItem != null) {
            settingsItem.isVisible = false
        }
    }

}