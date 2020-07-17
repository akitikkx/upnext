package com.theupnextapp.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentExploreBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows

class ExploreFragment : Fragment(),
    TrendingShowsAdapter.TrendingShowsAdapterListener,
    PopularShowsAdapter.PopularShowsAdapterListener {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private var _trendingShowsAdapter: TrendingShowsAdapter? = null
    private val trendingShowsAdapter get() = _trendingShowsAdapter!!

    private var _popularShowsAdapter: PopularShowsAdapter? = null
    private val popularShowsAdapter get() = _popularShowsAdapter!!

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

        _popularShowsAdapter = PopularShowsAdapter(this)

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

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.popularShowsTableUpdate.observe(viewLifecycleOwner, Observer {
            viewModel.onPopularShowsTableUpdateReceived(it)
        })

        viewModel.trendingShows.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                trendingShowsAdapter.trendingList = it
            }
        })

        viewModel.popularShows.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                popularShowsAdapter.popularList = it
            }
        })

        viewModel.navigateToSelectedShow.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.findNavController().navigate(
                    ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(it)
                )
                viewModel.displayShowDetailsComplete()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _trendingShowsAdapter = null
        _popularShowsAdapter = null
    }

    override fun onTrendingShowClick(view: View, traktTrending: TraktTrendingShows) {
        viewModel.onExploreItemClick(
            ShowDetailArg(
                source = "popular",
                showId = traktTrending.tvMazeID,
                showTitle = traktTrending.title,
                showImageUrl = traktTrending.originalImageUrl
            )
        )
        val analyticsBundle = Bundle()
        analyticsBundle.putString(
            FirebaseAnalytics.Param.ITEM_ID,
            traktTrending.tvMazeID.toString()
        )
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, traktTrending.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "explore_show")

        Firebase.analytics.logEvent("trending_shows_click", analyticsBundle)
    }

    override fun onPopularShowClick(view: View, popularShows: TraktPopularShows) {
        viewModel.onExploreItemClick(
            ShowDetailArg(
                source = "popular",
                showId = popularShows.tvMazeID,
                showTitle = popularShows.title,
                showImageUrl = popularShows.originalImageUrl
            )
        )
        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, popularShows.tvMazeID.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, popularShows.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "explore_show")

        Firebase.analytics.logEvent("popular_shows_click", analyticsBundle)
    }
}