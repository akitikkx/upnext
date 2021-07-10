package com.theupnextapp.ui.traktAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.theupnextapp.MainActivity
import com.theupnextapp.databinding.FragmentTraktAccountBinding
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TraktAccountFragment : BaseFragment() {

    private var _binding: FragmentTraktAccountBinding? = null
    val binding get() = _binding!!

    @Inject
    lateinit var traktAccountViewModelFactory: TraktAccountViewModel.TraktAccountViewModelFactory

    private val viewModel by viewModels<TraktAccountViewModel>() {
        traktAccountViewModelFactory.create(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTraktAccountBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        if (arguments?.getParcelable<TraktConnectionArg>(MainActivity.EXTRA_TRAKT_URI) != null) {
            val connectionArg =
                arguments?.getParcelable<TraktConnectionArg>(MainActivity.EXTRA_TRAKT_URI)
            viewModel.onCodeReceived(connectionArg?.code)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.openCustomTab.observe(viewLifecycleOwner, {
            if (it) {
                (activity as MainActivity).connectToTrakt()
                viewModel.onCustomTabOpened()
            }
        })

        viewModel.traktAccessToken.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.onAccessTokenReceived(it)
            }
        })

        viewModel.prefTraktAccessToken.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.onPrefAccessTokenRetrieved(it)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}