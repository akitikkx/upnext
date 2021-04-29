package com.theupnextapp.ui.history

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentHistoryBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktHistory
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : BaseFragment(), HistoryAdapter.HistoryAdapterListener {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private var historyAdapter: HistoryAdapter? = null

    private val viewModel by viewModels<HistoryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, {
            if (it == false) {
                this.findNavController().navigate(
                    HistoryFragmentDirections.actionHistoryFragmentToLibraryFragment()
                )
            }
        })

        viewModel.traktHistory.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                historyAdapter?.submitList(it)
            }
        })

        viewModel.navigateToSelectedShow.observe(viewLifecycleOwner, {
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

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
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