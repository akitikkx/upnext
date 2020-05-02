package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowCastBottomSheetBinding

class ShowCastBottomSheetFragment: BottomSheetDialogFragment() {

    private lateinit var binding: FragmentShowCastBottomSheetBinding

    private val viewModel: ShowCastBottomSheetViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            ShowCastBottomSheetViewModel.Factory(activity.application, arguments?.getParcelable(ShowDetailFragment.ARG_SHOW_CAST))
        ).get(ShowCastBottomSheetViewModel::class.java)
    }

    override fun getTheme(): Int = R.style.BottomSheetTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowCastBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        return binding.root
    }

    companion object {
        const val TAG = "ShowCastBottomSheetFragment"
    }
}