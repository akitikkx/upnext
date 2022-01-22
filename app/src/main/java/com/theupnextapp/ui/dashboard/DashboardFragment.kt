package com.theupnextapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentDashboardBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : BaseFragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val viewModel by viewModels<DashboardViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater)

        binding.composeContainer.apply {
            // Dispose of the Composition when the view's
            // LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    DashboardScreen {
                        val direction =
                            DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(
                                ShowDetailArg(
                                    source = "dashboard",
                                    showId = it.id,
                                    showTitle = it.name,
                                    showImageUrl = it.originalImage,
                                    showBackgroundUrl = it.mediumImage
                                )
                            )

                        findNavController().navigate(direction)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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