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

package com.theupnextapp.ui.showSeasonEpisodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.navigation.fragment.navArgs
import com.theupnextapp.databinding.FragmentShowSeasonEpisodesBinding
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

// TODO Remove fragment
@AndroidEntryPoint
class ShowSeasonEpisodesFragment : BaseFragment() {

    private var _binding: FragmentShowSeasonEpisodesBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<ShowSeasonEpisodesFragmentArgs>()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowSeasonEpisodesBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

//        binding.composeContainer.apply {
//            setContent {
//                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//                MdcTheme {
//                    ShowSeasonEpisodesScreen(viewModel = viewModel)
//                }
//            }
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel.traktCheckInStatus.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let { checkInStatus ->
//                if (checkInStatus.season == null && checkInStatus.episode == null && !checkInStatus.message.isNullOrEmpty()) {
//                    Snackbar.make(
//                        binding.root,
//                        "${checkInStatus.message}",
//                        Snackbar.LENGTH_LONG
//                    ).show()
//                } else if (checkInStatus.season != null && checkInStatus.episode != null && !checkInStatus.checkInTime.isNullOrEmpty()) {
//                    Snackbar.make(
//                        binding.root,
//                        "Trakt Check-in for Season ${checkInStatus.season} Episode ${checkInStatus.episode} in progress from ${checkInStatus.checkInTime}",
//                        Snackbar.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }

//        viewModel.confirmCheckIn.observe(viewLifecycleOwner) {
//            if (it != null) {
//                MaterialAlertDialogBuilder(requireActivity())
//                    .setTitle(
//                        resources.getString(
//                            R.string.show_detail_show_season_episode_check_in_dialog_title,
//                            it.season,
//                            it.number
//                        )
//                    )
//                    .setMessage(
//                        resources.getString(R.string.show_detail_show_season_episode_check_in_dialog_message)
//                    )
//                    .setNegativeButton(resources.getString(R.string.show_detail_show_season_episode_check_in_dialog_negative)) { dialog, _ ->
//                        dialog.dismiss()
//                    }
//                    .setPositiveButton(resources.getString(R.string.show_detail_show_season_episode_check_in_dialog_positive)) { dialog, _ ->
//                        viewModel.onCheckInConfirm(it)
//                        dialog.dismiss()
//                    }
//                    .show()
//            }
//        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

//    override fun onStart() {
//        super.onStart()
//        (activity as MainActivity).hideBottomNavigation()
//    }

//    override fun onStop() {
//        super.onStop()
//        (activity as MainActivity).showBottomNavigation()
//    }
}