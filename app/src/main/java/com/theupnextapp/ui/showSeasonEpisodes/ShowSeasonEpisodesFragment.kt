package com.theupnextapp.ui.showSeasonEpisodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.theupnextapp.MainActivity
import com.theupnextapp.databinding.FragmentShowSeasonEpisodesBinding
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowSeasonEpisodesFragment : BaseFragment() {

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

        _showSeasonEpisodesAdapter = ShowSeasonEpisodesAdapter()

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
}