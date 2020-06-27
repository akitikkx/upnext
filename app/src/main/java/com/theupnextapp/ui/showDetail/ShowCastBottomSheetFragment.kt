package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.databinding.FragmentShowCastBottomSheetBinding

class ShowCastBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentShowCastBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShowCastBottomSheetViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@ShowCastBottomSheetFragment,
            ShowCastBottomSheetViewModel.Factory(
                activity.application,
                arguments?.getParcelable(ShowDetailFragment.ARG_SHOW_CAST)
            )
        ).get(ShowCastBottomSheetViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowCastBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ShowCastBottomSheetFragment"
    }
}