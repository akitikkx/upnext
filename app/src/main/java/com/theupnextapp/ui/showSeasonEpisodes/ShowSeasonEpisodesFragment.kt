package com.theupnextapp.ui.showSeasonEpisodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowSeasonEpisodesBinding
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowSeasonEpisodesFragment : BaseFragment() {

    private var _binding: FragmentShowSeasonEpisodesBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<ShowSeasonEpisodesFragmentArgs>()

    @Inject
    lateinit var showSeasonEpisodesViewModelFactory: ShowSeasonEpisodesViewModel.ShowSeasonEpisodesViewModelFactory

    private val viewModel by viewModels<ShowSeasonEpisodesViewModel> {
        showSeasonEpisodesViewModelFactory.create(this, args.showSeasonEpisode)
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowSeasonEpisodesBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.composeContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    ShowSeasonEpisodesScreen()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.traktCheckInStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { checkInStatus ->
                if (checkInStatus.season == null && checkInStatus.episode == null && !checkInStatus.message.isNullOrEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "${checkInStatus.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else if (checkInStatus.season != null && checkInStatus.episode != null && !checkInStatus.checkInTime.isNullOrEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "Trakt Check-in for Season ${checkInStatus.season} Episode ${checkInStatus.episode} in progress from ${checkInStatus.checkInTime}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
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
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).showBottomNavigation()
    }
}