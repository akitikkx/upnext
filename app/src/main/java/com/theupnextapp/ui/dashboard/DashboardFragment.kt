/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentDashboardBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
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

        binding.lifecycleOwner = viewLifecycleOwner

        binding.composeContainer.apply {
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

                        val analyticsBundle = Bundle()
                        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, it.id.toString())
                        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, it.name)
                        analyticsBundle.putString(
                            FirebaseAnalytics.Param.CONTENT_TYPE,
                            "dashboard_show"
                        )
                        firebaseAnalytics.logEvent(
                            FirebaseAnalytics.Event.SELECT_ITEM,
                            analyticsBundle
                        )
                    }
                }
            }
        }

        return binding.root
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