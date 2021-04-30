package com.theupnextapp.ui.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentWatchlistBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktWatchlist
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WatchlistFragment : BaseFragment(), WatchlistAdapter.WatchlistAdapterListener {

    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private var watchlistAdapter: WatchlistAdapter? = null

    private val viewModel by viewModels<WatchlistViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchlistBinding.inflate(inflater)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        watchlistAdapter = WatchlistAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.watch_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = watchlistAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        viewModel.traktWatchlist.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                watchlistAdapter?.submitList(it)
            }
        })

    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.title_trakt_watchlist)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        watchlistAdapter = null
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onWatchlistShowClick(view: View, watchlistItem: TraktWatchlist) {
        val directions = WatchlistFragmentDirections.actionWatchlistFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "watchlist",
                showId = watchlistItem.tvMazeID,
                showTitle = watchlistItem.title,
                showImageUrl = watchlistItem.originalImageUrl
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, watchlistItem.tvMazeID.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, watchlistItem.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "watchlist_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }

    override fun onWatchlistItemDeleteClick(view: View, watchlistItem: TraktWatchlist) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(
                resources.getString(
                    R.string.remove_from_watchlist_confirm,
                    watchlistItem.title
                )
            )
            .setNegativeButton(resources.getString(R.string.remove_from_watchlist_dialog_button_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.remove_from_watchlist_dialog_button_positive)) { dialog, _ ->
                viewModel.onWatchlistItemDeleteClick(watchlistItem)
                dialog.dismiss()
            }
            .show()
    }
}