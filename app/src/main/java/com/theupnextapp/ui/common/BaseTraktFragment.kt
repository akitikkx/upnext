package com.theupnextapp.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentBaseTraktFragmentBinding
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.ui.watchlist.WatchlistViewModel

open class BaseTraktFragment: BaseFragment() {

    private var _binding: FragmentBaseTraktFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TraktViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@BaseTraktFragment,
            TraktViewModel.Factory(
                activity.application
            )
        ).get(TraktViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBaseTraktFragmentBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        if (arguments?.getParcelable<TraktConnectionArg>(WatchlistViewModel.EXTRA_TRAKT_URI) != null) {
            viewModel.onTraktConnectionBundleReceived(arguments)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.launchTraktConnectWindow.observe(viewLifecycleOwner, {
            if (it) {
                launchTraktWindow()
                viewModel.launchConnectWindowComplete()
            }
        })

        viewModel.traktAccessToken.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.onTraktAccessTokenReceived(it)
            }
        })

        viewModel.fetchAccessTokenInProgress.observe(viewLifecycleOwner, {
            if (it) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.fetch_access_token_progress_text),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })

        viewModel.storingTraktAccessTokenInProgress.observe(viewLifecycleOwner, {
            if (it) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.storing_access_token_progress_text),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })

        viewModel.onDisconnectClick.observe(viewLifecycleOwner, {
            if (it == true) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(resources.getString(R.string.library_disconnect_from_trakt_dialog_title))
                    .setMessage(resources.getString(R.string.library_disconnect_from_trakt_dialog_message))
                    .setNegativeButton(resources.getString(R.string.library_disconnect_from_trakt_dialog_negative)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.library_disconnect_from_trakt_dialog_positive)) { dialog, _ ->
                        viewModel.onDisconnectConfirm()
                        dialog.dismiss()
                    }
                    .show()
            }
        })
    }

    protected fun getLibraryNavigatorExtras(view: View, transitionName: String): FragmentNavigator.Extras {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
        return FragmentNavigatorExtras(view to transitionName)
    }

    protected fun getCollectionSeasonsNavigatorExtras(view: View): FragmentNavigator.Extras {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
        val libraryTransitionName = getString(R.string.collection_seasons_transition_name)
        return FragmentNavigatorExtras(view to libraryTransitionName)
    }
}