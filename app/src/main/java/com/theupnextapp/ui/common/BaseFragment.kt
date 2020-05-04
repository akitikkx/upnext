package com.theupnextapp.ui.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.theupnextapp.MainActivity
import com.theupnextapp.common.utils.NetworkConnectivityUtil

open class BaseFragment : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.application?.let { application ->
            NetworkConnectivityUtil(application).observe(viewLifecycleOwner, Observer {
                if (it == false) {
                    (activity as MainActivity).displayConnectionErrorMessage()
                } else {
                    (activity as MainActivity).hideConnectionErrorMessage()
                }
            })
        }
    }
}