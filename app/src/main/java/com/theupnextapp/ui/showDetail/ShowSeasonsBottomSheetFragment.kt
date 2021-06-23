package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowSeasonsBottomSheetBinding
import com.theupnextapp.domain.ShowSeason
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowSeasonsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentShowSeasonsBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var _adapter: ShowSeasonsAdapter? = null
    private val adapter get() = _adapter!!

    @Inject
    lateinit var assistedFactory: ShowSeasonsBottomSheetViewModel.ShowSeasonsBottomSheetViewModelFactory

    private val viewModel by viewModels<ShowSeasonsBottomSheetViewModel> {
        ShowSeasonsBottomSheetViewModel.provideFactory(
            assistedFactory,
            arguments?.getParcelable(ShowDetailFragment.ARG_SHOW_DETAIL)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowSeasonsBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _adapter = ShowSeasonsAdapter()
        val showSeasons: ArrayList<ShowSeason> =
            arguments?.getParcelableArrayList<ShowSeason>(ShowDetailFragment.ARG_SHOW_SEASONS) as ArrayList<ShowSeason>

        _adapter?.submitShowSeasonsList(showSeasons)

        binding.root.findViewById<RecyclerView>(R.id.season_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = this@ShowSeasonsBottomSheetFragment.adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _adapter = null
    }

    companion object {
        const val TAG = "ShowSeasonsBottomSheet"
    }
}