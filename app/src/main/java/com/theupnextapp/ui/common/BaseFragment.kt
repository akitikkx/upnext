package com.theupnextapp.ui.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialElevationScale
import com.theupnextapp.BuildConfig
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.common.utils.NetworkConnectivityUtil

open class BaseFragment : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.application?.let { application ->
            NetworkConnectivityUtil(application).observe(viewLifecycleOwner, {
                if (it == false) {
                    (activity as MainActivity).displayConnectionErrorMessage()
                } else {
                    (activity as MainActivity).hideConnectionErrorMessage()
                }
            })
        }
    }

    protected fun launchTraktWindow() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(resources.getString(R.string.library_connect_to_trakt_dialog_title))
            .setMessage(resources.getString(R.string.library_connect_to_trakt_dialog_message))
            .setNegativeButton(resources.getString(R.string.library_connect_to_trakt_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.library_connect_to_trakt_dialog_positive)) { dialog, _ ->
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("${TRAKT_API_URL}${TRAKT_OAUTH_ENDPOINT}?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}")
                )
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, activity?.packageName)
                startActivity(intent)
                dialog.dismiss()
            }
            .show()
    }

    protected fun getShowDetailNavigatorExtras(view: View): FragmentNavigator.Extras {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
        val showDetailTransitionName = getString(R.string.show_detail_transition_name)
        return FragmentNavigatorExtras(view to showDetailTransitionName)
    }

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
        const val TRAKT_API_URL = "https://api.trakt.tv"
        const val TRAKT_OAUTH_ENDPOINT = "/oauth/authorize"
    }
}