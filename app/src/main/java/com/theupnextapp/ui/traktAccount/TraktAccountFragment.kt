package com.theupnextapp.ui.traktAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentTraktAccountBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TraktAccountFragment : BaseFragment(), FavoritesAdapter.FavoritesAdapterListener {

    private var _binding: FragmentTraktAccountBinding? = null
    val binding get() = _binding!!

    private var favoritesAdapter: FavoritesAdapter? = null

    @Inject
    lateinit var traktAccountViewModelFactory: TraktAccountViewModel.TraktAccountViewModelFactory

    private val viewModel by viewModels<TraktAccountViewModel> {
        traktAccountViewModelFactory.create(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTraktAccountBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        favoritesAdapter = FavoritesAdapter(this)
        binding.layoutAuthorized.recyclerviewFavorites.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = favoritesAdapter
        }

        if (arguments?.getParcelable<TraktConnectionArg>(MainActivity.EXTRA_TRAKT_URI) != null) {
            val connectionArg =
                arguments?.getParcelable<TraktConnectionArg>(MainActivity.EXTRA_TRAKT_URI)
            viewModel.onCodeReceived(connectionArg?.code)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.openCustomTab.observe(viewLifecycleOwner, {
            if (it) {
                (activity as MainActivity).connectToTrakt()
                viewModel.onCustomTabOpened()
            }
        })

        viewModel.confirmDisconnectFromTrakt.observe(viewLifecycleOwner, {
            if (it == true) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(resources.getString(R.string.library_disconnect_from_trakt_dialog_title))
                    .setMessage(resources.getString(R.string.disconnect_from_trakt_dialog_message))
                    .setNegativeButton(resources.getString(R.string.library_disconnect_from_trakt_dialog_negative)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.library_disconnect_from_trakt_dialog_positive)) { dialog, _ ->
                        viewModel.onDisconnectConfirm()
                        dialog.dismiss()
                    }
                    .show()
                viewModel.onDisconnectFromTraktConfirmed()
            }
        })

        viewModel.favoriteShows.observe(viewLifecycleOwner, {
            favoritesAdapter?.submitFavoriteShowsList(it)
        })

        viewModel.favoriteEpisodes.observe(viewLifecycleOwner, {
            favoritesAdapter?.submitFavoriteNextEpisodes(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        favoritesAdapter = null
    }

    override fun onFavoriteItemClick(view: View, favoriteShows: TraktUserListItem) {
        val directions =
            TraktAccountFragmentDirections.actionTraktAccountFragmentToShowDetailFragment(
                ShowDetailArg(
                    source = "most_anticipated",
                    showId = favoriteShows.tvMazeID,
                    showTitle = favoriteShows.title,
                    showImageUrl = favoriteShows.originalImageUrl,
                    showBackgroundUrl = favoriteShows.mediumImageUrl
                )
            )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))
    }
}