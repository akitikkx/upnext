package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowSeasonsBottomSheetBinding
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.TraktShowWatchedProgress

class ShowSeasonsBottomSheetFragment : BottomSheetDialogFragment(),
    ShowSeasonsAdapter.ShowSeasonsAdapterListener {

    private var _binding: FragmentShowSeasonsBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var _adapter: ShowSeasonsAdapter? = null
    private val adapter get() = _adapter!!

    private val viewModel: ShowSeasonsViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            ShowSeasonsViewModel.Factory(
                activity.application,
                arguments?.getParcelable(ShowDetailFragment.ARG_SHOW_DETAIL)
            )
        ).get(ShowSeasonsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowSeasonsBottomSheetBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _adapter = ShowSeasonsAdapter(this)
        val showSeasons: ArrayList<ShowSeason> =
            arguments?.getParcelableArrayList<ShowSeason>(ShowDetailFragment.ARG_SHOW_SEASONS) as ArrayList<ShowSeason>
        val watchedProgress: TraktShowWatchedProgress? =
            arguments?.getParcelable(ShowDetailFragment.ARG_WATCHED_PROGRESS)

        _adapter?.showSeasons = showSeasons
        _adapter?.watchedProgress = watchedProgress?.seasons

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

    override fun onShowSeasonAddClick(view: View, showSeason: ShowSeason) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_add_title))
            .setNegativeButton(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_positive)) { dialog, _ ->
                viewModel.onAddSeasonClick(showSeason)
                dialog.dismiss()
            }
            .show()
    }

    override fun onShowSeasonRemoveClick(view: View, showSeason: ShowSeason) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_remove_title))
            .setNegativeButton(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_positive)) { dialog, _ ->
                viewModel.onRemoveSeasonClick(showSeason)
                dialog.dismiss()
            }
            .show()
    }
}