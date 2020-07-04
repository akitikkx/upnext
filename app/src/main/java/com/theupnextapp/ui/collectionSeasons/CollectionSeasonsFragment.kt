package com.theupnextapp.ui.collectionSeasons

import android.content.Context
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

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                this.findNavController().navigate(
                    CollectionSeasonsFragmentDirections.actionCollectionSeasonsFragmentToLibraryFragment()
                )
            }
        })

        viewModel.traktCollectionSeasons?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                viewModel.onCollectionSeasonsEmpty(false)
                adapter.traktCollectionSeasons = it
                adapter.traktCollection = args.traktCollection
            } else {
                viewModel.onCollectionSeasonsEmpty(true)
            }
        })

        viewModel.navigateToSelectedSeason.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.findNavController().navigate(
                    CollectionSeasonsFragmentDirections.actionCollectionSeasonsFragmentToCollectionSeasonEpisodesFragment(
                        it
                    )
                )
                viewModel.navigateToSelectedSeasonComplete()
            }
        })
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

    override fun onCollectionSeasonClick(view: View, traktCollectionSeason: TraktCollectionSeason) {
        viewModel.onSeasonClick(traktCollectionSeason)
    }

    override fun onCollectionSeasonRemoveClick(
        view: View,
        traktCollectionSeason: TraktCollectionSeason
    ) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(resources.getString(R.string.collection_season_remove_dialog_title))
            .setNegativeButton(resources.getString(R.string.collection_season_remove_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.collection_season_remove_dialog_positive)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}