package com.theupnextapp.ui.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.theupnextapp.BuildConfig
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentLibraryBinding
import com.theupnextapp.domain.LibraryList
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.ui.collection.CollectionFragment
import com.theupnextapp.ui.common.BaseFragment
import com.theupnextapp.ui.watchlist.WatchlistViewModel

class LibraryFragment : BaseFragment(), LibraryAdapter.LibraryAdapterListener {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private var _adapter: LibraryAdapter? = null
    private val adapter get() = _adapter!!

    private val viewModel: LibraryViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@LibraryFragment,
            LibraryViewModel.Factory(
                activity.application
            )
        ).get(LibraryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLibraryBinding.inflate(inflater)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        if (arguments?.getParcelable<TraktConnectionArg>(WatchlistViewModel.EXTRA_TRAKT_URI) != null) {
            viewModel.onTraktConnectionBundleReceived(arguments)
        }

        _adapter = LibraryAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.library_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = this@LibraryFragment.adapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.launchTraktConnectWindow.observe(viewLifecycleOwner, Observer {
            if (it) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(resources.getString(R.string.library_connect_to_trakt_dialog_title))
                    .setMessage(resources.getString(R.string.library_connect_to_trakt_dialog_message))
                    .setNegativeButton(resources.getString(R.string.library_connect_to_trakt_dialog_negative)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.library_connect_to_trakt_dialog_positive)) { dialog, _ ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("${CollectionFragment.TRAKT_API_URL}${CollectionFragment.TRAKT_OAUTH_ENDPOINT}?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}")
                        )
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, activity?.packageName)
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .show()
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

        viewModel.fetchAccessTokenInProgress.observe(viewLifecycleOwner, Observer {
            if (it) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.fetch_access_token_progress_text),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.fetch_access_token_progress_text),
                    Snackbar.LENGTH_LONG
                ).dismiss()
            }
        })

        viewModel.storingTraktAccessTokenInProgress.observe(viewLifecycleOwner, Observer {
            if (it) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.storing_access_token_progress_text),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.fetch_access_token_progress_text),
                    Snackbar.LENGTH_LONG
                ).dismiss()
            }
        })

        viewModel.libraryList.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                adapter.libraryList = it
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _adapter = null
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onLibraryItemClick(view: View, libraryList: LibraryList) {
        this.findNavController().navigate(libraryList.link)
    }

}