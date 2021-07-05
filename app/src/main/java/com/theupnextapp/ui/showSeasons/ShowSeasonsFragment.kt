package com.theupnextapp.ui.showSeasons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.theupnextapp.MainActivity
import com.theupnextapp.databinding.FragmentShowSeasonsBinding
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowSeasonsFragment : BaseFragment() {

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

        _showSeasonsAdapter = ShowSeasonsAdapter()

        binding.seasonList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = showSeasonsAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showSeasons.observe(viewLifecycleOwner, {
            showSeasonsAdapter.submitShowSeasonsList(it)
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

}