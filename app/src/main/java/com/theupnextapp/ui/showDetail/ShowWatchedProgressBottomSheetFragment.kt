package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowWatchedProgressBottomSheetBinding
import com.theupnextapp.domain.TraktShowWatchedProgress

class ShowWatchedProgressBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentShowWatchedProgressBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var _adapter: ShowWatchedProgressSeasonsAdapter? = null
    private val adapter get() = _adapter!!

    private val viewModel: ShowWatchedProgressBottomSheetViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@ShowWatchedProgressBottomSheetFragment,
            ShowWatchedProgressBottomSheetViewModel.Factory(
                activity.application,
                arguments?.getParcelable(ShowDetailFragment.ARG_WATCHED_PROGRESS),
                arguments?.getParcelable(ShowDetailFragment.ARG_SHOW_DETAIL)
            )
        ).get(ShowWatchedProgressBottomSheetViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowWatchedProgressBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _adapter = ShowWatchedProgressSeasonsAdapter()
        val watchedProgress: TraktShowWatchedProgress? =
            arguments?.getParcelable(ShowDetailFragment.ARG_WATCHED_PROGRESS)
        _adapter?.seasons = watchedProgress?.seasons

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