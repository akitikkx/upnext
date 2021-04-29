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
import com.theupnextapp.databinding.FragmentShowWatchedProgressBottomSheetBinding
import com.theupnextapp.domain.TraktShowWatchedProgress
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowWatchedProgressBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentShowWatchedProgressBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var _adapter: ShowWatchedProgressSeasonsAdapter? = null
    private val adapter get() = _adapter!!

    @Inject
    lateinit var assistedFactory: ShowWatchedProgressBottomSheetViewModel.ShowWatchedProgressBottomSheetViewModelFactory

    private val viewModel by viewModels<ShowWatchedProgressBottomSheetViewModel> {
        ShowWatchedProgressBottomSheetViewModel.provideFactory(
            assistedFactory,
            arguments?.getParcelable(ShowDetailFragment.ARG_WATCHED_PROGRESS),
            arguments?.getParcelable(ShowDetailFragment.ARG_SHOW_DETAIL)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowWatchedProgressBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _adapter = ShowWatchedProgressSeasonsAdapter()
        val watchedProgress: TraktShowWatchedProgress? =
            arguments?.getParcelable(ShowDetailFragment.ARG_WATCHED_PROGRESS)
        watchedProgress?.seasons?.let { _adapter?.submitWatchedProgressSeasonsList(it) }

        binding.root.findViewById<RecyclerView>(R.id.watched_progress_season_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = this@ShowWatchedProgressBottomSheetFragment.adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _adapter = null
    }

    companion object {
        const val TAG = "ShowWatchedProgressBottomSheetFragment"
    }
}