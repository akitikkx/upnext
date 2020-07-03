package com.theupnextapp.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentExploreBinding
import com.theupnextapp.domain.TraktTrending

class ExploreFragment : Fragment(), TrendingShowsAdapter.TrendingShowsAdapterListener {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private var _trendingShowsAdapter: TrendingShowsAdapter? = null
    private val trendingShowsAdapter get() = _trendingShowsAdapter!!

    private val viewModel: ExploreViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }

        ViewModelProvider(
            this@ExploreFragment,
            ExploreViewModel.Factory(activity.application)
        ).get(ExploreViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExploreBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _trendingShowsAdapter = TrendingShowsAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.trending_shows_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = trendingShowsAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.trendingShows.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                trendingShowsAdapter.trendingList = it
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _trendingShowsAdapter = null
    }

    override fun onTrendingShowClick(view: View, traktTrending: TraktTrending) {

    }
}