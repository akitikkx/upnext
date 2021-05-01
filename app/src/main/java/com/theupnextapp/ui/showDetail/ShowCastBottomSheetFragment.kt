package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.databinding.FragmentShowCastBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowCastBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentShowCastBottomSheetBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var assistedFactory: ShowCastBottomSheetViewModel.ShowCastBottomSheetViewModelFactory

    val viewModel by viewModels<ShowCastBottomSheetViewModel> {
        ShowCastBottomSheetViewModel.provideFactory(
            assistedFactory,
            arguments?.getParcelable(ShowDetailFragment.ARG_SHOW_CAST)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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