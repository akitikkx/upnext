package com.theupnextapp.ui.dashboard

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentDashboardBinding
import com.theupnextapp.domain.NewShows
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktRecommendations
import com.theupnextapp.ui.common.BaseFragment
import com.theupnextapp.ui.features.FeaturesBottomSheetFragment
import com.theupnextapp.ui.traktRecommendations.TraktRecommendationsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : BaseFragment(),
    TraktRecommendationsAdapter.TraktRecommendationsAdapterListener,
    NewShowsAdapter.NewShowsAdapterListener,
    TodayShowsAdapter.TodayShowsAdapterListener,
    YesterdayShowsAdapter.YesterdayShowsAdapterListener,
    TomorrowShowsAdapter.TomorrowShowsAdapterListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    lateinit var newShowsAdapter: NewShowsAdapter

    private var yesterdayShowsAdapter: YesterdayShowsAdapter? = null

    private var todayShowsAdapter: TodayShowsAdapter? = null

    private var tomorrowShowsAdapter: TomorrowShowsAdapter? = null

    private val viewModel by viewModels<DashboardViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        newShowsAdapter = NewShowsAdapter(this)

        yesterdayShowsAdapter = YesterdayShowsAdapter(this)

        todayShowsAdapter = TodayShowsAdapter(this)

        tomorrowShowsAdapter = TomorrowShowsAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.new_shows_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = newShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.yesterday_shows_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = yesterdayShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.today_shows_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = todayShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.tomorrow_shows_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = tomorrowShowsAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.showFeaturesBottomSheet.observe(viewLifecycleOwner, {
            if (it != null && it == true) {
                val featuresBottomSheet = FeaturesBottomSheetFragment()
                activity?.supportFragmentManager?.let { fragmentManager ->
                    featuresBottomSheet.show(fragmentManager, FeaturesBottomSheetFragment.TAG)
                    viewModel.showFeaturesBottomSheetComplete()
                }
            }
        })

        viewModel.newShowsList.observe(viewLifecycleOwner, { newShows ->
            newShows.apply {
                if (!newShows.isNullOrEmpty()) {
                    newShowsAdapter?.submitList(newShows)
                }
            }
        })

        viewModel.yesterdayShowsList.observe(
            viewLifecycleOwner,
            { yesterdayShows ->
                yesterdayShows.apply {
                    if (!yesterdayShows.isNullOrEmpty()) {
                        yesterdayShowsAdapter?.submitList(yesterdayShows)
                    }
                }
            })

        viewModel.todayShowsList.observe(
            viewLifecycleOwner,
            { todayShows ->
                todayShows.apply {
                    if (!todayShows.isNullOrEmpty()) {
                        todayShowsAdapter?.submitList(todayShows)
                    }
                }
            })

        viewModel.tomorrowShowsList.observe(
            viewLifecycleOwner,
            { tomorrowShows ->
                tomorrowShows.apply {
                    if (!tomorrowShows.isNullOrEmpty()) {
                        tomorrowShowsAdapter?.submitList(tomorrowShows)
                    }
                }
            })

        viewModel.yesterdayShowsTableUpdate.observe(viewLifecycleOwner, {
            viewModel.onYesterdayShowsTableUpdateReceived(it)
        })

        viewModel.todayShowsTableUpdate.observe(viewLifecycleOwner, {
            viewModel.onTodayShowsTableUpdateReceived(it)
        })

        viewModel.tomorrowShowsTableUpdate.observe(viewLifecycleOwner, {
            viewModel.onTomorrowShowsTableUpdateReceived(it)
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        yesterdayShowsAdapter = null
        todayShowsAdapter = null
        tomorrowShowsAdapter = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_refresh -> {
                viewModel.onRefreshShowsClick()
                firebaseAnalytics.logEvent("dashboard_refresh_shows_click", null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onYesterdayShowClick(view: View, yesterdayShow: ScheduleShow) {
        val directions = DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "yesterday",
                showId = yesterdayShow.id,
                showTitle = yesterdayShow.name,
                showImageUrl = yesterdayShow.image
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, yesterdayShow.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, yesterdayShow.name)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }

    override fun onRecommendedShowClick(view: View, traktRecommendations: TraktRecommendations) {
        val directions = DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "recommended",
                showId = traktRecommendations.tvMazeID,
                showTitle = traktRecommendations.title,
                showImageUrl = traktRecommendations.originalImageUrl
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(
            FirebaseAnalytics.Param.ITEM_ID,
            traktRecommendations.id.toString()
        )
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, traktRecommendations.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }

    override fun onNewShowClick(view: View, newShow: NewShows) {
        val directions = DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "new",
                showId = newShow.id,
                showTitle = newShow.name,
                showImageUrl = newShow.originalImageUrl
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, newShow.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, newShow.name)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }

    override fun onTodayShowClick(view: View, scheduleShow: ScheduleShow) {
        val directions = DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "today",
                showId = scheduleShow.id,
                showTitle = scheduleShow.name,
                showImageUrl = scheduleShow.image
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, scheduleShow.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, scheduleShow.name)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }

    override fun onTomorrowShowClick(view: View, scheduleShow: ScheduleShow) {
        val directions = DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "tomorrow",
                showId = scheduleShow.id,
                showTitle = scheduleShow.name,
                showImageUrl = scheduleShow.image
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, scheduleShow.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, scheduleShow.name)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }
}