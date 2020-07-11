package com.theupnextapp.ui.dashboard

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentDashboardBinding
import com.theupnextapp.domain.NewShows
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktRecommendations
import com.theupnextapp.ui.common.BaseFragment
import com.theupnextapp.ui.features.FeaturesBottomSheetFragment

class DashboardFragment : BaseFragment(), RecommendedShowsAdapter.RecommendedShowsAdapterListener,
    NewShowsAdapter.NewShowsAdapterListener,
    TodayShowsAdapter.TodayShowsAdapterListener,
    YesterdayShowsAdapter.YesterdayShowsAdapterListener,
    TomorrowShowsAdapter.TomorrowShowsAdapterListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var _firebaseAnalytics: FirebaseAnalytics? = null
    private val firebaseAnalytics get() = _firebaseAnalytics!!

    private var _recommendedShowsAdapter: RecommendedShowsAdapter? = null
    private val recommendedShowsAdapter get() = _recommendedShowsAdapter!!

    private var newShowsAdapter: NewShowsAdapter? = null

    private var yesterdayShowsAdapter: YesterdayShowsAdapter? = null

    private var todayShowsAdapter: TodayShowsAdapter? = null

    private var tomorrowShowsAdapter: TomorrowShowsAdapter? = null

    private val viewModel: DashboardViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@DashboardFragment,
            DashboardViewModel.Factory(activity.application)
        ).get(DashboardViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _firebaseAnalytics = Firebase.analytics

        _recommendedShowsAdapter = RecommendedShowsAdapter(this)

        newShowsAdapter = NewShowsAdapter(this)

        yesterdayShowsAdapter = YesterdayShowsAdapter(this)

        todayShowsAdapter = TodayShowsAdapter(this)

        tomorrowShowsAdapter = TomorrowShowsAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.recommended_shows_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = recommendedShowsAdapter
        }

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.showFeaturesBottomSheet.observe(viewLifecycleOwner, Observer {
            if (it != null && it == true) {
                val featuresBottomSheet = FeaturesBottomSheetFragment()
                activity?.supportFragmentManager?.let { fragmentManager ->
                    featuresBottomSheet.show(fragmentManager, FeaturesBottomSheetFragment.TAG)
                    viewModel.showFeaturesBottomSheetComplete()
                }
            }
        })

        viewModel.traktRecommendationsList.observe(
            viewLifecycleOwner,
            Observer { recommendedShows ->
                recommendedShows.apply {
                    if (!recommendedShows.isNullOrEmpty()) {
                        recommendedShowsAdapter.recommendedShows = recommendedShows
                    }
                }
            })

        viewModel.newShowsList.observe(viewLifecycleOwner, Observer { newShows ->
            newShows.apply {
                if (!newShows.isNullOrEmpty()) {
                    newShowsAdapter?.newShows = newShows
                }
            }
        })

        viewModel.yesterdayShowsList.observe(
            viewLifecycleOwner,
            Observer { yesterdayShows ->
                yesterdayShows.apply {
                    if (!yesterdayShows.isNullOrEmpty()) {
                        yesterdayShowsAdapter?.yesterdayShows = yesterdayShows
                    }
                }
            })

        viewModel.todayShowsList.observe(
            viewLifecycleOwner,
            Observer { todayShows ->
                todayShows.apply {
                    if (!todayShows.isNullOrEmpty()) {
                        todayShowsAdapter?.todayShows = todayShows
                    }
                }
            })

        viewModel.tomorrowShowsList.observe(
            viewLifecycleOwner,
            Observer { tomorrowShows ->
                tomorrowShows.apply {
                    if (!tomorrowShows.isNullOrEmpty()) {
                        tomorrowShowsAdapter?.tomorrowShows = tomorrowShows
                    }
                }
            })

        viewModel.yesterdayShowsTableUpdate.observe(viewLifecycleOwner, Observer {
            viewModel.onYesterdayShowsTableUpdateReceived(it)
        })

        viewModel.todayShowsTableUpdate.observe(viewLifecycleOwner, Observer {
            viewModel.onTodayShowsTableUpdateReceived(it)
        })

        viewModel.tomorrowShowsTableUpdate.observe(viewLifecycleOwner, Observer {
            viewModel.onTomorrowShowsTableUpdateReceived(it)
        })

        viewModel.traktRecommendedShowsTableUpdate.observe(viewLifecycleOwner, Observer {
            viewModel.onTraktRecommendationsShowsTableUpdateReceived(it)
        })

        viewModel.navigateToSelectedShow.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController().navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(it)
                )
                val analyticsBundle = Bundle()
                analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, it.showId.toString())
                analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, it.showTitle)
                analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")

                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)

                viewModel.displayShowDetailsComplete()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _recommendedShowsAdapter = null
        newShowsAdapter = null
        yesterdayShowsAdapter = null
        todayShowsAdapter = null
        tomorrowShowsAdapter = null
        _firebaseAnalytics = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_refresh -> {
                viewModel.onRefreshShowsClick()
                Firebase.analytics.logEvent("dashboard_refresh_shows_click", null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onYesterdayShowClick(view: View, yesterdayShow: ScheduleShow) {
        viewModel.onDashboardItemClick(
            ShowDetailArg(
                source = "yesterday",
                showId = yesterdayShow.id,
                showTitle = yesterdayShow.name,
                showImageUrl = yesterdayShow.image
            )
        )
        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, yesterdayShow.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, yesterdayShow.name)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")

        Firebase.analytics.logEvent("yesterday_shows_click", analyticsBundle)
    }

    override fun onRecommendedShowClick(view: View, traktRecommendations: TraktRecommendations) {
        viewModel.onDashboardItemClick(
            ShowDetailArg(
                source = "recommended",
                showId = traktRecommendations.tvMazeID,
                showTitle = traktRecommendations.title,
                showImageUrl = traktRecommendations.originalImageUrl
            )
        )
        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, traktRecommendations.tvMazeID.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, traktRecommendations.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")

        Firebase.analytics.logEvent("recommended_shows_click", analyticsBundle)
    }

    override fun onNewShowClick(view: View, newShow: NewShows) {
        viewModel.onDashboardItemClick(
            ShowDetailArg(
                source = "new",
                showId = newShow.id,
                showTitle = newShow.name,
                showImageUrl = newShow.originalImageUrl
            )
        )
        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, newShow.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, newShow.name)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")

        Firebase.analytics.logEvent("new_shows_click", analyticsBundle)
    }

    override fun onTodayShowClick(view: View, scheduleShow: ScheduleShow) {
        viewModel.onDashboardItemClick(
            ShowDetailArg(
                source = "today",
                showId = scheduleShow.id,
                showTitle = scheduleShow.name,
                showImageUrl = scheduleShow.image
            )
        )
    }

    override fun onTomorrowShowClick(view: View, scheduleShow: ScheduleShow) {
        viewModel.onDashboardItemClick(
            ShowDetailArg(
                source = "tomorrow",
                showId = scheduleShow.id,
                showTitle = scheduleShow.name,
                showImageUrl = scheduleShow.image
            )
        )
        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, scheduleShow.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, scheduleShow.name)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "dashboard_show")

        Firebase.analytics.logEvent("new_shows_click", analyticsBundle)
    }
}