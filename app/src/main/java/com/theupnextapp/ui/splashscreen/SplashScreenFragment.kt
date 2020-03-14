package com.theupnextapp.ui.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.theupnextapp.databinding.FragmentSplashScreenBinding

class SplashScreenFragment : Fragment() {

    private lateinit var binding: FragmentSplashScreenBinding

    private val viewModel: SplashScreenViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            SplashScreenViewModel.Factory(activity.application)
        ).get(SplashScreenViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSplashScreenBinding.inflate(inflater)

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
                viewModel.updateShows()
            }
        })

        viewModel.isNormalInstall.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.showDashboard()
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.displayLoadingText()
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
}