package com.theupnextapp.ui.collectionSeasons

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentCollectionSeasonsBinding
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.ui.common.BaseFragment

class CollectionSeasonsFragment : BaseFragment(),
    CollectionSeasonsAdapter.CollectionSeasonsAdapterListener {

    private var _binding: FragmentCollectionSeasonsBinding? = null
    private val binding get() = _binding!!

    private var _adapter: CollectionSeasonsAdapter? = null
    private val adapter get() = _adapter!!

    private val args by navArgs<CollectionSeasonsFragmentArgs>()

    private val viewModel: CollectionSeasonsViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@CollectionSeasonsFragment,
            CollectionSeasonsViewModel.Factory(
                activity.application,
                args.traktCollection
            )
        ).get(CollectionSeasonsViewModel::class.java)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCollectionSeasonsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _adapter = CollectionSeasonsAdapter(this)

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

        viewModel.traktCollectionSeasons?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                viewModel.onCollectionSeasonsEmpty(false)
                adapter.traktCollectionSeasons = it
                adapter.traktCollection = args.traktCollection
            } else {
                viewModel.onCollectionSeasonsEmpty(true)
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