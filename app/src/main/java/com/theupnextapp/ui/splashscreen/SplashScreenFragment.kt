package com.theupnextapp.ui.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentSplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreenFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<SplashScreenViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSplashScreenBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isFreshInstall.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.updateShows()
            }
        })

        viewModel.isUpgradeInstall.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.showDashboard()
            }
        })

        viewModel.isNormalInstall.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.showDashboard()
            }
        })

        viewModel.isLoadingYesterdayShows.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.displayLoadingText(getString(R.string.splash_screen_loading_text_yesterday_schedule))
            } else {
                viewModel.displayLoadingTextComplete()
            }
        })

        viewModel.isLoadingTodayShows.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.displayLoadingText(getString(R.string.splash_screen_loading_text_today_schedule))
            } else {
                viewModel.displayLoadingTextComplete()
            }
        })

        viewModel.isLoadingTomorrowShows.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.displayLoadingText(getString(R.string.splash_screen_loading_text_tomorrow_schedule))
            } else {
                viewModel.displayLoadingTextComplete()
                viewModel.showDashboard()
            }
        })

        viewModel.navigateToDashboard.observe(viewLifecycleOwner, {
            if (null != it) {
                this.findNavController().navigate(
                    SplashScreenFragmentDirections.actionSplashScreenFragmentToDashboardFragment()
                )
                viewModel.showDashboardComplete()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar?.hide()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as MainActivity).showBottomNavigation()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}