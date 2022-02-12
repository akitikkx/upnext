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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.theupnextapp.MainActivity
import com.theupnextapp.databinding.FragmentShowSeasonsBinding
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowSeasonsFragment : BaseFragment(), ShowSeasonsAdapter.ShowSeasonsAdapterListener {

    private var _binding: FragmentShowSeasonsBinding? = null
    private val binding get() = _binding!!

    private var _showSeasonsAdapter: ShowSeasonsAdapter? = null
    private val showSeasonsAdapter get() = _showSeasonsAdapter!!

    private val args by navArgs<ShowSeasonsFragmentArgs>()

    @Inject
    lateinit var showSeasonsViewModelFactory: ShowSeasonsViewModel.ShowSeasonsViewModelFactory

    private val viewModel by viewModels<ShowSeasonsViewModel> {
        showSeasonsViewModelFactory.create(this, args.show)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowSeasonsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        _showSeasonsAdapter = ShowSeasonsAdapter(this, args.show.showId)

        binding.seasonEpisodesList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = showSeasonsAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showSeasons.observe(viewLifecycleOwner, { showSeasons ->
            if (showSeasons != null) {
                showSeasonsAdapter.submitShowSeasonsList(showSeasons)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _showSeasonsAdapter = null
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onSeasonClick(view: View, showId: Int, seasonNumber: Int) {
        val directions =
            ShowSeasonsFragmentDirections.actionShowSeasonsFragmentToShowSeasonEpisodesFragment(
                ShowSeasonEpisodesArg(
                    showId = showId,
                    seasonNumber = seasonNumber,
                    imdbID = args.show.imdbID,
                    isAuthorizedOnTrakt = args.show.isAuthorizedOnTrakt
                )
            )
        findNavController().navigate(directions)
    }

}