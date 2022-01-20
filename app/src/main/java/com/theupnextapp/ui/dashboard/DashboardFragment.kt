package com.theupnextapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentDashboardBinding
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : BaseFragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private var yesterdayShowsAdapter: YesterdayShowsAdapter? = null

    private var todayShowsAdapter: TodayShowsAdapter? = null

    private var tomorrowShowsAdapter: TomorrowShowsAdapter? = null

    private val viewModel by viewModels<DashboardViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        yesterdayShowsAdapter = YesterdayShowsAdapter()

        todayShowsAdapter = TodayShowsAdapter()

        tomorrowShowsAdapter = TomorrowShowsAdapter()

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
}