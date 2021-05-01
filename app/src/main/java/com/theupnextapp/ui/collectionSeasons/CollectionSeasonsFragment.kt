package com.theupnextapp.ui.collectionSeasons

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentCollectionSeasonsBinding
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.domain.TraktCollectionSeasonEpisodeArg
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CollectionSeasonsFragment : BaseFragment(),
    CollectionSeasonsAdapter.CollectionSeasonsAdapterListener {

    private var _binding: FragmentCollectionSeasonsBinding? = null
    private val binding get() = _binding!!

    private var _adapter: CollectionSeasonsAdapter? = null
    private val adapter get() = _adapter!!

    private val args by navArgs<CollectionSeasonsFragmentArgs>()

    @Inject
    lateinit var assistedFactory: CollectionSeasonsViewModel.CollectionSeasonsViewModelFactory

    private val viewModel by viewModels<CollectionSeasonsViewModel> {
        CollectionSeasonsViewModel.provideFactory(
            assistedFactory,
            args.traktCollection
        )
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
    ): View {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, {
            if (it == false) {
                this.findNavController().navigate(
                    CollectionSeasonsFragmentDirections.actionCollectionSeasonsFragmentToLibraryFragment()
                )
            }
        })

        viewModel.traktCollectionSeasons?.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                viewModel.onCollectionSeasonsEmpty(false)
                adapter.submitList(it)
                adapter.traktCollection = args.traktCollection
            } else {
                viewModel.onCollectionSeasonsEmpty(true)
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
        this.findNavController().navigate(
            CollectionSeasonsFragmentDirections.actionCollectionSeasonsFragmentToCollectionSeasonEpisodesFragment(
                TraktCollectionSeasonEpisodeArg(
                    collection = args.traktCollection,
                    collectionSeason = traktCollectionSeason
                )
            ),
            getShowDetailNavigatorExtras(view)
        )
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