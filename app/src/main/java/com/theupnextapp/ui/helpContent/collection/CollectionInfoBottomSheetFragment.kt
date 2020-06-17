package com.theupnextapp.ui.helpContent.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.databinding.FragmentCollectionInfoBottomSheetBinding

class CollectionInfoBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCollectionInfoBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CollectionInfoBottomSheetViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            CollectionInfoBottomSheetViewModel.Factory(
                activity.application
            )
        ).get(CollectionInfoBottomSheetViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCollectionInfoBottomSheetBinding.inflate(inflater)

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
        const val TAG = "CollectionInfoBottomSheetFragment"
    }
}