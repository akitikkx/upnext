package com.theupnextapp.ui.collectionSeasons

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.theupnextapp.BuildConfig
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentCollectionSeasonsBinding
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.ui.collection.CollectionViewModel

class CollectionSeasonsFragment : Fragment(),
    CollectionSeasonsAdapter.CollectionSeasonsAdapterListener {

    private var _binding: FragmentCollectionSeasonsBinding? = null
    private val binding get() = _binding!!

    private var _adapter: CollectionSeasonsAdapter? = null
    private val adapter get() = _adapter!!

    val args by navArgs<CollectionSeasonsFragmentArgs>()

    private val viewModel: CollectionSeasonsViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            CollectionSeasonsViewModel.Factory(
                activity.application,
                args.traktCollection
            )
        ).get(CollectionSeasonsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCollectionSeasonsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        val args = arguments

        _adapter = CollectionSeasonsAdapter(this)

        if (args?.getParcelable<TraktConnectionArg>(CollectionViewModel.EXTRA_TRAKT_URI) != null) {
            viewModel.onTraktConnectionBundleReceived(arguments)
        }

        binding.root.findViewById<RecyclerView>(R.id.collection_season_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = this@CollectionSeasonsFragment.adapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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

        viewModel.traktCollectionSeasons?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                viewModel.onCollectionSeasonsEmpty(false)
                adapter.traktCollectionSeasons = it
                adapter.traktCollection = args?.traktCollection
            } else {
                viewModel.onCollectionSeasonsEmpty(true)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.title_trakt_collection)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
        const val TRAKT_API_URL = "https://api.trakt.tv"
        const val TRAKT_OAUTH_ENDPOINT = "/oauth/authorize"
    }

    override fun onCollectionSeasonClick(view: View, traktCollectionSeason: TraktCollectionSeason) {

    }

    override fun onCollectionSeasonRemoveClick(
        view: View,
        traktCollectionSeason: TraktCollectionSeason
    ) {

    }
}