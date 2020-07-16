package com.theupnextapp.ui.history

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentHistoryBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktHistory
import com.theupnextapp.ui.common.BaseFragment

class HistoryFragment : BaseFragment(), HistoryAdapter.HistoryAdapterListener {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private var historyAdapter: HistoryAdapter? = null

    private val viewModel: HistoryViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProvider(
            this@HistoryFragment,
            HistoryViewModel.Factory(activity.application)
        ).get(HistoryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        historyAdapter = HistoryAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.history_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = historyAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                this.findNavController().navigate(
                    HistoryFragmentDirections.actionHistoryFragmentToLibraryFragment()
                )
            }
        })

        viewModel.historyTableUpdate.observe(viewLifecycleOwner, Observer {
            viewModel.onHistoryTableUpdateReceived(it)
        })

        viewModel.traktHistory.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                historyAdapter?.history = it
            }
        })

        viewModel.navigateToSelectedShow.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController().navigate(
                    HistoryFragmentDirections.actionHistoryFragmentToShowDetailFragment(it)
                )
                viewModel.displayShowDetailsComplete()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.title_trakt_history)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        historyAdapter = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onHistoryShowClick(view: View, historyItem: TraktHistory) {
        viewModel.displayShowDetails(
            ShowDetailArg(
                source = "history",
                showId = historyItem.tvMazeID,
                showTitle = historyItem.showTitle,
                showImageUrl = historyItem.originalImageUrl
            )
        )
    }

    override fun onHistoryRemoveClick(view: View, historyItem: TraktHistory) {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage("Remove ${historyItem.showTitle} from your history?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes") { dialog, _ ->
                viewModel.onRemoveClick(historyItem)
                dialog.dismiss()
            }
            .show()
    }
}