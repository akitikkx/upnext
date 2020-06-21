package com.theupnextapp.ui.collection

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.theupnextapp.BuildConfig
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentCollectionBinding
import com.theupnextapp.domain.TraktCollection
import com.theupnextapp.domain.TraktConnectionArg

class CollectionFragment : Fragment(), CollectionAdapter.CollectionAdapterListener {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    private var _adapter: CollectionAdapter? = null
    private val adapter get() = _adapter!!

    private val viewModel: CollectionViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            CollectionViewModel.Factory(activity.application)
        ).get(CollectionViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCollectionBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _adapter = CollectionAdapter(this)

        val args = arguments

        if (args?.getParcelable<TraktConnectionArg>(CollectionViewModel.EXTRA_TRAKT_URI) != null) {
            viewModel.onTraktConnectionBundleReceived(arguments)
        }

        binding.root.findViewById<RecyclerView>(R.id.collection_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = this@CollectionFragment.adapter
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

        viewModel.traktCollection.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                viewModel.onCollectionEmpty(false)
                adapter.traktCollection = it
            } else {
                viewModel.onCollectionEmpty(true)
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
        _adapter = null
    }


    override fun onCollectionClick(view: View, traktCollection: TraktCollection) {

    }

    override fun onCollectionRemoveClick(view: View, traktCollection: TraktCollection) {

    }

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
        const val TRAKT_API_URL = "https://api.trakt.tv"
        const val TRAKT_OAUTH_ENDPOINT = "/oauth/authorize"
    }
}