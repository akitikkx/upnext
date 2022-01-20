package com.theupnextapp.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentExploreBinding
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExploreFragment : BaseFragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private var _trendingShowsAdapter: TrendingShowsAdapter? = null
    private val trendingShowsAdapter get() = _trendingShowsAdapter!!

    private var _popularShowsAdapter: PopularShowsAdapter? = null
    private val popularShowsAdapter get() = _popularShowsAdapter!!

    private var _mostAnticipatedShowsAdapter: MostAnticipatedShowsAdapter? = null
    private val mostAnticipatedShowsAdapter get() = _mostAnticipatedShowsAdapter!!

    private val viewModel by viewModels<ExploreViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _trendingShowsAdapter = TrendingShowsAdapter()

        _popularShowsAdapter = PopularShowsAdapter()

        _mostAnticipatedShowsAdapter = MostAnticipatedShowsAdapter()

        binding.root.findViewById<RecyclerView>(R.id.trending_shows_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = trendingShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.popular_shows_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = popularShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.most_anticipated_shows_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = mostAnticipatedShowsAdapter
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

        viewModel.trendingShows.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                trendingShowsAdapter.submitList(it)
            }
        })

        viewModel.popularShows.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                popularShowsAdapter.submitList(it)
            }
        })

        viewModel.mostAnticipatedShows.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                mostAnticipatedShowsAdapter.submitList(it)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _trendingShowsAdapter = null
        _popularShowsAdapter = null
        _mostAnticipatedShowsAdapter = null
    }
}