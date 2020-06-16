package com.theupnextapp.ui.helpContent.connectToTrakt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.databinding.FragmentConnectToTraktInfoBottomSheetBinding

class ConnectToTraktInfoBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentConnectToTraktInfoBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConnectToTraktInfoBottomSheetViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            ConnectToTraktInfoBottomSheetViewModel.Factory(
                activity.application
            )
        ).get(ConnectToTraktInfoBottomSheetViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConnectToTraktInfoBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ConnectToTraktInfoBottomSheetFragment"
    }
}