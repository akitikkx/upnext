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

package com.theupnextapp.ui.showSeasons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.navigation.fragment.navArgs
import com.theupnextapp.MainActivity
import com.theupnextapp.databinding.FragmentShowSeasonsBinding
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

// TODO Remove fragment
@ExperimentalMaterialApi
@AndroidEntryPoint
class ShowSeasonsFragment : BaseFragment() {

    private var _binding: FragmentShowSeasonsBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<ShowSeasonsFragmentArgs>()

//    @Inject
//    lateinit var showSeasonsViewModelFactory: ShowSeasonsViewModel.ShowSeasonsViewModelFactory
//
//    private val viewModel by viewModels<ShowSeasonsViewModel> {
//        showSeasonsViewModelFactory.create(this, args.show)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowSeasonsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

//        binding.composeContainer.apply {
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//            setContent {
//                MdcTheme {
//                    ShowSeasonsScreen(viewModel = viewModel) {
//                        val directions =
//                            ShowSeasonsFragmentDirections.actionShowSeasonsFragmentToShowSeasonEpisodesFragment(
//                                ShowSeasonEpisodesArg(
//                                    showId = args.show.showId?.toInt(),
//                                    seasonNumber = it.seasonNumber,
//                                    imdbID = args.show.imdbID,
//                                    isAuthorizedOnTrakt = args.show.isAuthorizedOnTrakt
//                                )
//                            )
//                        findNavController().navigate(directions)
//                    }
//                }
//            }
//        }

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