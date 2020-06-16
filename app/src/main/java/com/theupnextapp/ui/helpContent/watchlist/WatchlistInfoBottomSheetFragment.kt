package com.theupnextapp.ui.helpContent.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.databinding.FragmentWatchlistInfoBottomSheetBinding

class WatchlistInfoBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentWatchlistInfoBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WatchlistInfoBottomSheetViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            WatchlistInfoBottomSheetViewModel.Factory(
                activity.application
            )
        ).get(WatchlistInfoBottomSheetViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchlistInfoBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog?.dismiss()
    }

    companion object {
        const val TAG = "WatchlistInfoBottomSheetFragment"
    }
}