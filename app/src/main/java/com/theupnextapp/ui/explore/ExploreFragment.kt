package com.theupnextapp.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.databinding.FragmentExploreBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExploreFragment : BaseFragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val viewModel by viewModels<ExploreViewModel>()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.composeContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    ExploreScreen(
                        onPopularShowClick = {
                            val directions = ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
                                ShowDetailArg(
                                    source = "popular",
                                    showId = it.tvMazeID,
                                    showTitle = it.title,
                                    showImageUrl = it.originalImageUrl,
                                    showBackgroundUrl = it.mediumImageUrl
                                )
                            )
                            findNavController().navigate(directions)
                        },
                        onTrendingShowClick = {
                            val directions = ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
                                ShowDetailArg(
                                    source = "trending",
                                    showId = it.tvMazeID,
                                    showTitle = it.title,
                                    showImageUrl = it.originalImageUrl,
                                    showBackgroundUrl = it.mediumImageUrl
                                )
                            )
                            findNavController().navigate(directions)
                        },
                        onMostAnticipatedShowClick = {
                            val directions = ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
                                ShowDetailArg(
                                    source = "most_anticipated",
                                    showId = it.tvMazeID,
                                    showTitle = it.title,
                                    showImageUrl = it.originalImageUrl,
                                    showBackgroundUrl = it.mediumImageUrl
                                )
                            )
                            findNavController().navigate(directions)
                        }
                    )
                }

            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.popularShowsTableUpdate.observe(viewLifecycleOwner, {
            viewModel.onPopularShowsTableUpdateReceived(it)
        })

        viewModel.trendingShowsTableUpdate.observe(viewLifecycleOwner, {
            viewModel.onTrendingShowsTableUpdateReceived(it)
        })

        viewModel.mostAnticipatedShowsTableUpdate.observe(viewLifecycleOwner, {
            viewModel.onMostAnticipatedShowsTableUpdateReceived(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}