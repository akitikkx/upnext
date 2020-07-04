package com.theupnextapp.ui.collectionSeasonEpisodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentCollectionSeasonEpisodesBinding
import com.theupnextapp.domain.TraktCollectionSeasonEpisode
import com.theupnextapp.ui.common.BaseFragment

class CollectionSeasonEpisodesFragment : BaseFragment(),
    CollectionSeasonEpisodesAdapter.CollectionSeasonEpisodesAdapterListener {

    private var _binding: FragmentCollectionSeasonEpisodesBinding? = null
    private val binding get() = _binding!!

    private var _adapter: CollectionSeasonEpisodesAdapter? = null
    private val adapter get() = _adapter!!

    private val args by navArgs<CollectionSeasonEpisodesFragmentArgs>()

    private val viewModel: CollectionSeasonEpisodesViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@CollectionSeasonEpisodesFragment,
            CollectionSeasonEpisodesViewModel.Factory(
                activity.application,
                args.traktCollectionSeason.collection,
                args.traktCollectionSeason.collectionSeason
            )
        ).get(CollectionSeasonEpisodesViewModel::class.java)
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
        _binding = FragmentCollectionSeasonEpisodesBinding.inflate(inflater)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        _adapter = CollectionSeasonEpisodesAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.collection_season_episode_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = this@CollectionSeasonEpisodesFragment.adapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                this.findNavController().navigate(
                    CollectionSeasonEpisodesFragmentDirections.actionCollectionSeasonEpisodesFragmentToLibraryFragment()
                )
            }
        })

        viewModel.traktCollectionSeasonEpisodes?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                adapter.episodesList = it
            } else {
                viewModel.onCollectionSeasonEpisodesEmpty(true)
            }
        })

        viewModel.collection.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapter.traktCollection = it
            }
        })

        viewModel.collectionSeason.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapter.traktCollectionSeason = it
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _adapter = null
    }

    override fun onCollectionSeasonEpisodeRemoveClick(
        view: View,
        traktCollectionSeasonEpisode: TraktCollectionSeasonEpisode
    ) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(resources.getString(R.string.collection_season_episode_remove_dialog_title))
            .setNegativeButton(resources.getString(R.string.collection_season_episode_remove_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.collection_season_episode_remove_dialog_positive)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}