package com.theupnextapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentDashboardBinding
import com.theupnextapp.domain.NewShows
import com.theupnextapp.domain.RecommendedShows
import com.theupnextapp.domain.ScheduleShow

class DashboardFragment : Fragment() {

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

        recommendedShowsAdapter =
            RecommendedShowsAdapter(RecommendedShowsAdapter.RecommendedShowsAdapterListener {
                // TODO handle click of show
            })

        newShowsAdapter = NewShowsAdapter(NewShowsAdapter.NewShowsAdapterListener {

        })

        yesterdayShowsAdapter =
            YesterdayShowsAdapter(YesterdayShowsAdapter.YesterdayShowsAdapterListener {

            })

        todayShowsAdapter =
            TodayShowsAdapter(TodayShowsAdapter.TodayShowsAdapterListener {

            })

        tomorrowShowsAdapter =
            TomorrowShowsAdapter(TomorrowShowsAdapter.TomorrowShowsAdapterListener {

            })

        binding.root.findViewById<RecyclerView>(R.id.recommended_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = recommendedShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.new_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = newShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.yesterday_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = yesterdayShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.today_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = todayShowsAdapter
        }

        binding.root.findViewById<RecyclerView>(R.id.tomorrow_shows_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
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

    }
}