/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
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

@ExperimentalMaterial3Api
@AndroidEntryPoint
class ExploreFragment : BaseFragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val viewModel by viewModels<ExploreViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.composeContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    ExploreScreen(
                        onPopularShowClick = {
                            val directions =
                                ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
                                    ShowDetailArg(
                                        source = "popular",
                                        showId = it.tvMazeID.toString(),
                                        showTitle = it.title,
                                        showImageUrl = it.originalImageUrl,
                                        showBackgroundUrl = it.mediumImageUrl
                                    )
                                )
                            findNavController().navigate(directions)
                        },
                        onTrendingShowClick = {
                            val directions =
                                ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
                                    ShowDetailArg(
                                        source = "trending",
                                        showId = it.tvMazeID.toString(),
                                        showTitle = it.title,
                                        showImageUrl = it.originalImageUrl,
                                        showBackgroundUrl = it.mediumImageUrl
                                    )
                                )
                            findNavController().navigate(directions)
                        },
                        onMostAnticipatedShowClick = {
                            val directions =
                                ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
                                    ShowDetailArg(
                                        source = "most_anticipated",
                                        showId = it.tvMazeID.toString(),
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

        viewModel.popularShowsTableUpdate.observe(viewLifecycleOwner) {
            viewModel.onPopularShowsTableUpdateReceived(it)
        }

        viewModel.trendingShowsTableUpdate.observe(viewLifecycleOwner) {
            viewModel.onTrendingShowsTableUpdateReceived(it)
        }

        viewModel.mostAnticipatedShowsTableUpdate.observe(viewLifecycleOwner) {
            viewModel.onMostAnticipatedShowsTableUpdateReceived(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}