package com.theupnextapp.ui.helpContent.connectToTrakt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.databinding.FragmentConnectToTraktInfoBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectToTraktInfoBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentConnectToTraktInfoBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ConnectToTraktInfoBottomSheetViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectToTraktInfoBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog?.dismiss()
    }
}