package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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

    private val viewModel: ShowSeasonsBottomSheetViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@ShowSeasonsBottomSheetFragment,
            ShowSeasonsBottomSheetViewModel.Factory(
                activity.application,
                arguments?.getParcelable(ShowDetailFragment.ARG_SHOW_DETAIL)
            )
        ).get(ShowSeasonsBottomSheetViewModel::class.java)
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

        _adapter?.submitShowSeasonsList(showSeasons)
        watchedProgress?.seasons?.let { _adapter?.submitWatchedProgressSeasonsList(it) }

        binding.root.findViewById<RecyclerView>(R.id.season_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = this@ShowSeasonsBottomSheetFragment.adapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.addToHistoryResponse.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.onAddToHistoryResponseReceived(it)
            }
        })

        viewModel.removeFromHistoryResponse.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.onRemoveFromHistoryResponseReceived(it)
            }
        })

        viewModel.addToCollectionResponse.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.onAddToCollectionResponseReceived(it)
            }
        })

        viewModel.removeFromCollectionResponse.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.onRemoveFromCollectionResponseReceived(it)
            }
        })

        viewModel.watchedProgress.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                it.seasons?.let { watchedProgressSeasons ->
                    adapter.submitWatchedProgressSeasonsList(
                        watchedProgressSeasons
                    )
                }
            }
        })

        viewModel.traktCollectionSeasons?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapter.submitCollectionSeasonsList(it)
            }
        })

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapter.isAuthorizedOnTrakt = it
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _adapter = null
    }

    override fun onShowSeasonAddToTraktHistoryClick(view: View, showSeason: ShowSeason) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_add_title))
            .setNegativeButton(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_positive)) { dialog, _ ->
                viewModel.onAddSeasonToHistoryClick(showSeason)
                dialog.dismiss()
            }
            .show()
    }

    override fun onShowSeasonRemoveFromTraktHistoryClick(view: View, showSeason: ShowSeason) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_remove_title))
            .setNegativeButton(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.show_detail_show_seasons_bottom_sheet_dialog_positive)) { dialog, _ ->
                viewModel.onRemoveSeasonFromHistoryClick(showSeason)
                dialog.dismiss()
            }
            .show()
    }

    override fun onShowSeasonAddToTraktCollectionClick(view: View, showSeason: ShowSeason) {
        viewModel.onAddSeasonToCollectionClick(showSeason)
    }

    override fun onShowSeasonRemoveFromTraktCollectionClick(view: View, showSeason: ShowSeason) {
        viewModel.onRemoveSeasonFromCollectionClick(showSeason)
    }

    companion object {
        const val TAG = "ShowSeasonsBottomSheet"
    }
}