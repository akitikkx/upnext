package com.theupnextapp.ui.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentWatchlistBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.domain.TraktWatchlist

class WatchlistFragment : Fragment(), WatchlistAdapter.WatchlistAdapterListener {

    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    private var watchlistAdapter: WatchlistAdapter? = null

    private val viewModel: WatchlistViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            WatchlistViewModel.Factory(activity.application)
        ).get(WatchlistViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchlistBinding.inflate(inflater)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        val args = arguments

        if (args?.getParcelable<TraktConnectionArg>(WatchlistViewModel.EXTRA_TRAKT_URI) != null) {
            viewModel.onTraktConnectionBundleReceived(arguments)
        }

        watchlistAdapter = WatchlistAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.watch_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = watchlistAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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

        viewModel.traktWatchlist.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                viewModel.onWatchlistEmpty(true)
            } else {
                viewModel.onWatchlistEmpty(false)
                watchlistAdapter?.watchlist = it
            }
        })

        viewModel.navigateToSelectedShow.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController().navigate(
                    WatchlistFragmentDirections.actionWatchlistFragmentToShowDetailFragment(it)
                )
                viewModel.displayShowDetailsComplete()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.title_trakt_watchlist)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        watchlistAdapter = null
    }

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
        const val TRAKT_API_URL = "https://api.trakt.tv"
        const val TRAKT_OAUTH_ENDPOINT = "/oauth/authorize"
    }

    override fun onWatchlistShowClick(view: View, watchlistItem: TraktWatchlist) {
        viewModel.displayShowDetails(
            ShowDetailArg(
                source = "watchlist",
                showId = watchlistItem.tvMazeID,
                showTitle = watchlistItem.title,
                showImageUrl = watchlistItem.originalImageUrl
            )
        )
    }

    override fun onWatchlistItemDeleteClick(view: View, watchlistItem: TraktWatchlist) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(
                resources.getString(
                    R.string.remove_from_watchlist_confirm,
                    watchlistItem.title
                )
            )
            .setNegativeButton(resources.getString(R.string.remove_from_watchlist_dialog_button_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.remove_from_watchlist_dialog_button_positive)) { dialog, _ ->
                viewModel.onWatchlistItemDeleteClick(watchlistItem)
                dialog.dismiss()
            }
            .show()
    }
}