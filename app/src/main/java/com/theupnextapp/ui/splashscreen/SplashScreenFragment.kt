package com.theupnextapp.ui.splashscreen

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentSplashScreenBinding

class SplashScreenFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SplashScreenViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@SplashScreenFragment,
            SplashScreenViewModel.Factory(activity.application)
        ).get(SplashScreenViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSplashScreenBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.isFreshInstall.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.updateShows()
            }
        })

        viewModel.isUpgradeInstall.observe(viewLifecycleOwner, Observer {
            if (it) {
                // TODO Add functionality to rather update the shows if the last update was the previous day
                viewModel.updateShows()
            }
        })

        viewModel.isNormalInstall.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.showDashboard()
            }
        })

        viewModel.isLoadingRecommendedShows.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.displayLoadingText(getString(R.string.splash_screen_loading_text_recommended_shows))
            } else {
                viewModel.displayLoadingTextComplete()
            }
        })

        viewModel.isLoadingNewShows.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.displayLoadingText(getString(R.string.splash_screen_loading_text_new_shows))
            } else {
                viewModel.displayLoadingTextComplete()
            }
        })

        viewModel.isLoadingYesterdayShows.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.displayLoadingText(getString(R.string.splash_screen_loading_text_yesterday_schedule))
            } else {
                viewModel.displayLoadingTextComplete()
            }
        })

        viewModel.isLoadingTodayShows.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.displayLoadingText(getString(R.string.splash_screen_loading_text_today_schedule))
            } else {
                viewModel.displayLoadingTextComplete()
            }
        })

        viewModel.isLoadingTomorrowShows.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.displayLoadingText(getString(R.string.splash_screen_loading_text_tomorrow_schedule))
            } else {
                viewModel.displayLoadingTextComplete()
                viewModel.showDashboard()
            }
        })

        viewModel.isLoadingTraktRecommendations.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.displayLoadingText(getString(R.string.splash_screen_loading_text_recommended_shows))
            } else {
                viewModel.displayLoadingTextComplete()
                viewModel.showDashboard()
            }
        })

        viewModel.navigateToDashboard.observe(viewLifecycleOwner, Observer {
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
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}