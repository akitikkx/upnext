/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowDetailBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@AndroidEntryPoint
class ShowDetailFragment : BaseFragment() {

    private var _binding: FragmentShowDetailBinding? = null
    private val binding get() = _binding!!

    private var _imdbID: String? = null

    private var _isAuthorizedOnTrakt: Boolean = false

    private val args by navArgs<ShowDetailFragmentArgs>()

    private val viewModel by viewModels<ShowDetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val settingsItem = menu.findItem(R.id.menu_settings)
        if (settingsItem != null) {
            settingsItem.isVisible = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowDetailBinding.inflate(inflater)

        binding.composeContainer.apply {
            // Dispose of the Composition when the view's
            // LifecycleOwner is destroyed
            // https://developer.android.com/jetpack/compose/interop/interop-apis
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    ShowDetailScreen(
                        onSeasonsClick = {
                            viewModel.onSeasonsClick()
                        }
                    )
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel.showCastBottomSheet.observe(viewLifecycleOwner) {
//            if (it != null) {
//                val showCastBottomSheet = ShowCastBottomSheetFragment()
//
//                val args = Bundle()
//                args.putParcelable(ARG_SHOW_CAST, it)
//                showCastBottomSheet.arguments = args
//
//                activity?.supportFragmentManager?.let { fragmentManager ->
//                    showCastBottomSheet.show(
//                        fragmentManager,
//                        ShowCastBottomSheetFragment.TAG
//                    )
//                }
//                viewModel.displayCastBottomSheetComplete()
//            }
//        }

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner) {
            _isAuthorizedOnTrakt = it
        }

        viewModel.navigateToSeasons.observe(viewLifecycleOwner) {
            if (it) {
                val directions =
                    ShowDetailFragmentDirections.actionShowDetailFragmentToShowSeasonsFragment(
                        ShowDetailArg(
                            showId = args.show.showId,
                            showTitle = args.show.showTitle,
                            showImageUrl = args.show.showImageUrl,
                            showBackgroundUrl = args.show.showBackgroundUrl,
                            imdbID = _imdbID,
                            isAuthorizedOnTrakt = _isAuthorizedOnTrakt
                        )
                    )
                findNavController().navigate(directions)
                viewModel.onSeasonsNavigationComplete()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = args.show.showTitle
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_SHOW_CAST = "show_cast"
    }
}