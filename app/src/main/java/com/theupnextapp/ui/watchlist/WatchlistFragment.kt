package com.theupnextapp.ui.watchlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
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
import com.google.android.material.snackbar.Snackbar
import com.theupnextapp.BuildConfig
import com.theupnextapp.R
import com.theupnextapp.common.extensions.waitForTransition
import com.theupnextapp.databinding.FragmentWatchlistBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.domain.TraktWatchlist

class WatchlistFragment : Fragment(), WatchlistAdapter.WatchlistAdapterListener {

    private lateinit var binding: FragmentWatchlistBinding

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
        binding = FragmentWatchlistBinding.inflate(inflater)

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
            waitForTransition(this)
            adapter = watchlistAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.loadTraktWatchilist()
            }
        })

        viewModel.launchTraktConnectWindow.observe(viewLifecycleOwner, Observer {
            if (it) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("${TRAKT_API_URL}${TRAKT_OAUTH_ENDPOINT}?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}")
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
            if (it.isEmpty()) {
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
                showImageUrl = watchlistItem.originalImageUrl,
                imageView = view
            )
        )
    }
}