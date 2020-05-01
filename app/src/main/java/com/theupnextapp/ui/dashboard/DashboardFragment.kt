package com.theupnextapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.common.extensions.waitForTransition
import com.theupnextapp.databinding.FragmentDashboardBinding
import com.theupnextapp.domain.NewShows
import com.theupnextapp.domain.RecommendedShows
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ShowDetailArg

class DashboardFragment : Fragment(), RecommendedShowsAdapter.RecommendedShowsAdapterListener,
    NewShowsAdapter.NewShowsAdapterListener,
    TodayShowsAdapter.TodayShowsAdapterListener,
    YesterdayShowsAdapter.YesterdayShowsAdapterListener,
    TomorrowShowsAdapter.TomorrowShowsAdapterListener {

    private lateinit var binding: FragmentDashboardBinding

    private var recommendedShowsAdapter: RecommendedShowsAdapter? = null
    private var newShowsAdapter: NewShowsAdapter? = null
    private var yesterdayShowsAdapter: YesterdayShowsAdapter? = null
    private var todayShowsAdapter: TodayShowsAdapter? = null
    private var tomorrowShowsAdapter: TomorrowShowsAdapter? = null

    private val viewModel: DashboardViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            DashboardViewModel.Factory(activity.application)
        ).get(DashboardViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        recommendedShowsAdapter = RecommendedShowsAdapter(this)

        newShowsAdapter = NewShowsAdapter(this)

        yesterdayShowsAdapter = YesterdayShowsAdapter(this)

        todayShowsAdapter = TodayShowsAdapter(this)

        tomorrowShowsAdapter = TomorrowShowsAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.recommended_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            waitForTransition(this)
            adapter = recommendedShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.new_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            waitForTransition(this)
            adapter = newShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.yesterday_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            waitForTransition(this)
            adapter = yesterdayShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.today_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            waitForTransition(this)
            adapter = todayShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.tomorrow_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            waitForTransition(this)
            adapter = tomorrowShowsAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.recommendedShowsList.observe(
            viewLifecycleOwner,
            Observer<List<RecommendedShows>> { recommendedShows ->
                recommendedShows.apply {
                    recommendedShowsAdapter?.recommendedShows = recommendedShows
                }
            })

        viewModel.newShowsList.observe(viewLifecycleOwner, Observer<List<NewShows>> { newShows ->
            newShows.apply {
                newShowsAdapter?.newShows = newShows
            }
        })

        viewModel.yesterdayShowsList.observe(
            viewLifecycleOwner,
            Observer<List<ScheduleShow>> { yesterdayShows ->
                yesterdayShows.apply {
                    yesterdayShowsAdapter?.yesterdayShows = yesterdayShows
                }
            })

        viewModel.todayShowsList.observe(
            viewLifecycleOwner,
            Observer<List<ScheduleShow>> { todayShows ->
                todayShows.apply {
                    todayShowsAdapter?.todayShows = todayShows
                }
            })

        viewModel.tomorrowShowsList.observe(
            viewLifecycleOwner,
            Observer<List<ScheduleShow>> { tomorrowShows ->
                tomorrowShows.apply {
                    tomorrowShowsAdapter?.tomorrowShows = tomorrowShows
                }
            })

        viewModel.navigateToSelectedShow.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                val extras = FragmentNavigatorExtras(
                    it.imageView to "${it.source}_${it.showImageUrl}"
                )

                this.findNavController().navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(it),
                    extras
                )
                viewModel.displayShowDetailsComplete()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
    }

    override fun onYesterdayShowClick(view: View, yesterdayShow: ScheduleShow) {
        viewModel.displayShowDetails(
            ShowDetailArg(
                source = "yesterday",
                showId = yesterdayShow.id,
                showTitle = yesterdayShow.name,
                showImageUrl = yesterdayShow.image,
                imageView = view
            )
        )
    }

    override fun onRecommendedShowClick(view: View, recommendedShow: RecommendedShows) {
        viewModel.displayShowDetails(
            ShowDetailArg(
                source = "recommended",
                showId = recommendedShow.id,
                showTitle = recommendedShow.name,
                showImageUrl = recommendedShow.originalImageUrl,
                imageView = view
            )
        )
    }

    override fun onNewShowClick(view: View, newShow: NewShows) {
        viewModel.displayShowDetails(
            ShowDetailArg(
                source = "new",
                showId = newShow.id,
                showTitle = newShow.name,
                showImageUrl = newShow.originalImageUrl,
                imageView = view
            )
        )
    }

    override fun onTodayShowClick(view: View, scheduleShow: ScheduleShow) {
        viewModel.displayShowDetails(
            ShowDetailArg(
                source = "today",
                showId = scheduleShow.id,
                showTitle = scheduleShow.name,
                showImageUrl = scheduleShow.image,
                imageView = view
            )
        )
    }

    override fun onTomorrowShowClick(view: View, scheduleShow: ScheduleShow) {
        viewModel.displayShowDetails(
            ShowDetailArg(
                source = "tomorrow",
                showId = scheduleShow.id,
                showTitle = scheduleShow.name,
                showImageUrl = scheduleShow.image,
                imageView = view
            )
        )
    }
}