package com.theupnextapp.ui.traktAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.theupnextapp.MainActivity
import com.theupnextapp.databinding.FragmentTraktAccountBinding
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TraktAccountFragment : BaseFragment() {

    private var _binding: FragmentTraktAccountBinding? = null
    val binding get() = _binding!!

    private val viewModel by viewModels<TraktAccountViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTraktAccountBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}