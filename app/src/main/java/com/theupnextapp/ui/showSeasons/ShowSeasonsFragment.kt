package com.theupnextapp.ui.showSeasons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.MainActivity
import com.theupnextapp.databinding.FragmentShowSeasonsBinding
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowSeasonsFragment : BaseFragment() {

    private var _binding: FragmentShowSeasonsBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<ShowSeasonsFragmentArgs>()

    @Inject
    lateinit var showSeasonsViewModelFactory: ShowSeasonsViewModel.ShowSeasonsViewModelFactory

    private val viewModel by viewModels<ShowSeasonsViewModel> {
        showSeasonsViewModelFactory.create(this, args.show)
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowSeasonsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.composeContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    ShowSeasonsScreen {
                        val directions =
                            ShowSeasonsFragmentDirections.actionShowSeasonsFragmentToShowSeasonEpisodesFragment(
                                ShowSeasonEpisodesArg(
                                    showId = args.show.showId,
                                    seasonNumber = it.seasonNumber,
                                    imdbID = args.show.imdbID,
                                    isAuthorizedOnTrakt = args.show.isAuthorizedOnTrakt
                                )
                            )
                        findNavController().navigate(directions)
                    }
                }
            }
        }

        return binding.root
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