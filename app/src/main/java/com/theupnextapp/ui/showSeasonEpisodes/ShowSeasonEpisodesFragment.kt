package com.theupnextapp.ui.showSeasonEpisodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowSeasonEpisodesBinding
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowSeasonEpisodesFragment : BaseFragment(),
    ShowSeasonEpisodesAdapter.ShowSeasonEpisodesAdapterListener {

    private var _binding: FragmentShowSeasonEpisodesBinding? = null
    private val binding get() = _binding!!

    private var _showSeasonEpisodesAdapter: ShowSeasonEpisodesAdapter? = null
    private val showSeasonEpisodesAdapter get() = _showSeasonEpisodesAdapter!!

    private val args by navArgs<ShowSeasonEpisodesFragmentArgs>()

    @Inject
    lateinit var showSeasonEpisodesViewModelFactory: ShowSeasonEpisodesViewModel.ShowSeasonEpisodesViewModelFactory

    private val viewModel by viewModels<ShowSeasonEpisodesViewModel> {
        showSeasonEpisodesViewModelFactory.create(this, args.showSeasonEpisode)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowSeasonEpisodesBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        _showSeasonEpisodesAdapter = ShowSeasonEpisodesAdapter(this)

        binding.seasonEpisodesList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = showSeasonEpisodesAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.episodes.observe(viewLifecycleOwner, {
            showSeasonEpisodesAdapter.submitSeasonEpisodesList(it)
        })

        viewModel.seasonNumber.observe(viewLifecycleOwner, {
            if (it != null) {
                binding.textviewShowSeasonEpisodesTitle.text =
                    getString(R.string.show_detail_show_season_episodes_title_with_number, it)
            }
        })

        viewModel.confirmCheckIn.observe(viewLifecycleOwner, {
            if (it != null) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(
                        resources.getString(
                            R.string.show_detail_show_season_episode_check_in_dialog_title,
                            it.season,
                            it.number
                        )
                    )
                    .setMessage(
                        resources.getString(R.string.show_detail_show_season_episode_check_in_dialog_message)
                    )
                    .setNegativeButton(resources.getString(R.string.show_detail_show_season_episode_check_in_dialog_negative)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.show_detail_show_season_episode_check_in_dialog_positive)) { dialog, _ ->
                        viewModel.onCheckInConfirm(it)
                        dialog.dismiss()
                    }
                    .show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _showSeasonEpisodesAdapter = null
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onCheckInClick(showSeasonEpisode: ShowSeasonEpisode) {
        viewModel.onCheckInClick(showSeasonEpisode)
    }
}