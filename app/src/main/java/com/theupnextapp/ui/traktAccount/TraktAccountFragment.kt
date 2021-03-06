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

package com.theupnextapp.ui.traktAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentTraktAccountBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.ui.common.BaseFragment
import com.theupnextapp.ui.theme.UpnextTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@AndroidEntryPoint
@Deprecated("Will be removed once Jetpack Navigation work has been completed")
class TraktAccountFragment : BaseFragment() {

    private var _binding: FragmentTraktAccountBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var traktAccountViewModelFactory: TraktAccountViewModel.TraktAccountViewModelFactory

    private val viewModel by viewModels<TraktAccountViewModel> {
        traktAccountViewModelFactory.create(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTraktAccountBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.composeContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                UpnextTheme {
                    TraktAccountScreen(
                        viewModel = viewModel,
                        onConnectToTraktClick = {
                            viewModel.onConnectToTraktClick()
                        },
                        onLogoutClick = {
                            viewModel.onDisconnectFromTraktClick()
                        },
                        onFavoriteClick = {
                            val directions =
                                TraktAccountFragmentDirections.actionTraktAccountFragmentToShowDetailFragment(
                                    ShowDetailArg(
                                        source = "favorites",
                                        showId = it.tvMazeID.toString(),
                                        showTitle = it.title,
                                        showImageUrl = it.originalImageUrl,
                                        showBackgroundUrl = it.mediumImageUrl
                                    )
                                )
                            findNavController().navigate(directions)
                        })
                }
            }
        }

        if (arguments?.getParcelable<TraktConnectionArg>(MainActivity.EXTRA_TRAKT_URI) != null) {
            val connectionArg =
                arguments?.getParcelable<TraktConnectionArg>(MainActivity.EXTRA_TRAKT_URI)
            viewModel.onCodeReceived(connectionArg?.code)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.openCustomTab.observe(viewLifecycleOwner) {
            if (it) {
                (activity as MainActivity).connectToTrakt()
                viewModel.onCustomTabOpened()
            }
        }

        viewModel.confirmDisconnectFromTrakt.observe(viewLifecycleOwner) {
            if (it == true) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(resources.getString(R.string.library_disconnect_from_trakt_dialog_title))
                    .setMessage(resources.getString(R.string.disconnect_from_trakt_dialog_message))
                    .setNegativeButton(resources.getString(R.string.library_disconnect_from_trakt_dialog_negative)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.library_disconnect_from_trakt_dialog_positive)) { dialog, _ ->
                        viewModel.onDisconnectConfirm()
                        dialog.dismiss()
                    }
                    .show()
                viewModel.onDisconnectFromTraktConfirmed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}