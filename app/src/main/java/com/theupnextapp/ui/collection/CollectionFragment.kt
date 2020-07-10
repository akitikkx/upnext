package com.theupnextapp.ui.collection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentCollectionBinding
import com.theupnextapp.domain.TraktCollection
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.ui.common.BaseFragment

class CollectionFragment : BaseFragment(), CollectionAdapter.CollectionAdapterListener {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    private var _adapter: CollectionAdapter? = null
    private val adapter get() = _adapter!!

    private val viewModel: CollectionViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@CollectionFragment,
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

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                this.findNavController().navigate(
                    CollectionFragmentDirections.actionCollectionFragmentToLibraryFragment()
                )
            }
        })

        viewModel.traktCollection.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                adapter.traktCollection = it
            }
        })

        viewModel.navigateToSelectedCollection.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController().navigate(
                    CollectionFragmentDirections.actionCollectionFragmentToCollectionSeasonsFragment(
                        it
                    )
                )
                val analyticsBundle = Bundle()
                analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, it.imdbID)
                analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, it.title)
                analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "collection_show")

                Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)

                viewModel.navigateToSelectedCollectionComplete()
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onCollectionClick(view: View, traktCollection: TraktCollection) {
        viewModel.onCollectionClick(traktCollection)
    }

    override fun onCollectionRemoveClick(view: View, traktCollection: TraktCollection) {

    }

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
        const val TRAKT_API_URL = "https://api.trakt.tv"
        const val TRAKT_OAUTH_ENDPOINT = "/oauth/authorize"
    }
}