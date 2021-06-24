package com.theupnextapp.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentExploreBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExploreFragment : BaseFragment(),
    TrendingShowsAdapter.TrendingShowsAdapterListener,
    PopularShowsAdapter.PopularShowsAdapterListener,
    MostAnticipatedShowsAdapter.MostAnticipatedShowsAdapterListener {

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

        _trendingShowsAdapter = TrendingShowsAdapter(this)

        _popularShowsAdapter = PopularShowsAdapter(this)

        _mostAnticipatedShowsAdapter = MostAnticipatedShowsAdapter(this)

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

        viewModel.popularShowsTableUpdate.observe(viewLifecycleOwner, Observer {
            viewModel.onPopularShowsTableUpdateReceived(it)
        })

        viewModel.trendingShowsTableUpdate.observe(viewLifecycleOwner, Observer {
            viewModel.onTrendingShowsTableUpdateReceived(it)
        })

        viewModel.mostAnticipatedShowsTableUpdate.observe(viewLifecycleOwner, Observer {
            viewModel.onMostAnticipatedShowsTableUpdateReceived(it)
        })

        viewModel.trendingShows.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                trendingShowsAdapter.submitList(it)
            }
        })

        viewModel.popularShows.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                popularShowsAdapter.submitList(it)
            }
        })

        viewModel.mostAnticipatedShows.observe(viewLifecycleOwner, Observer {
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

    override fun onTrendingShowClick(view: View, traktTrending: TraktTrendingShows) {
        val directions = ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "trending",
                showId = traktTrending.tvMazeID,
                showTitle = traktTrending.title,
                showImageUrl = traktTrending.originalImageUrl,
                showBackgroundUrl = traktTrending.mediumImageUrl
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, traktTrending.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, traktTrending.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "explore_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }

    override fun onPopularShowClick(view: View, popularShows: TraktPopularShows) {
        val directions = ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "popular",
                showId = popularShows.tvMazeID,
                showTitle = popularShows.title,
                showImageUrl = popularShows.originalImageUrl,
                showBackgroundUrl = popularShows.mediumImageUrl
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, popularShows.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, popularShows.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "explore_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }

    override fun onMostAnticipatedShowClick(view: View, mostAnticipated: TraktMostAnticipated) {
        val directions = ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "most_anticipated",
                showId = mostAnticipated.tvMazeID,
                showTitle = mostAnticipated.title,
                showImageUrl = mostAnticipated.originalImageUrl,
                showBackgroundUrl = mostAnticipated.mediumImageUrl
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, mostAnticipated.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, mostAnticipated.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "explore_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }
}