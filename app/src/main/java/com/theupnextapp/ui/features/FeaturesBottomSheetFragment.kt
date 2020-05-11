package com.theupnextapp.ui.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentFeaturesBottomSheetBinding

class FeaturesBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFeaturesBottomSheetBinding

    private val viewModel: FeaturesBottomSheetViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            FeaturesBottomSheetViewModel.Factory(activity.application)
        ).get(FeaturesBottomSheetViewModel::class.java)
    }

    override fun getTheme(): Int = R.style.BottomSheetTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeaturesBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        return binding.root
    }

    companion object {
        const val TAG = "FeaturesBottomSheetFragment"
    }
}