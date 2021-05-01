package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.common.utils.*
import com.theupnextapp.databinding.FragmentShowDetailBinding
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.TraktShowWatchedProgress
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowDetailFragment : BaseFragment(), ShowCastAdapter.ShowCastAdapterListener {

    private var _binding: FragmentShowDetailBinding? = null
    private val binding get() = _binding!!

    private var _showCastAdapter: ShowCastAdapter? = null
    private val showCastAdapter get() = _showCastAdapter!!

    private var _traktShowWatchedProgress: TraktShowWatchedProgress? = null
    private var _showSeasons: List<ShowSeason>? = null
    private var showInfo: ShowInfo? = null

    val args by navArgs<ShowDetailFragmentArgs>()

    @Inject
    lateinit var assistedFactory: ShowDetailViewModel.ShowDetailViewModelFactory

    private val viewModel by viewModels<ShowDetailViewModel> {
        ShowDetailViewModel.provideFactory(
            assistedFactory,
            args.show
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val settingsItem = menu.findItem(R.id.menu_settings)
        if (settingsItem != null) {
            settingsItem.isVisible = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowDetailBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _showCastAdapter = ShowCastAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.cast_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = showCastAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showInfo.observe(viewLifecycleOwner, {
            if (it != null) {
                showInfo = it
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if (it == true) {
                binding.showDetailProgressBar.visibility = ProgressBar.VISIBLE
            } else {
                binding.showDetailProgressBar.visibility = ProgressBar.GONE
            }
        })

        viewModel.watchlistRecord.observe(viewLifecycleOwner, {
            viewModel.onWatchlistRecordReceived(it)
        })

        viewModel.showCast.observe(viewLifecycleOwner, {
            viewModel.onShowCastInfoReceived(it)
            if (it != null) {
                showCastAdapter.submitList(it)
            }
        })

        viewModel.showCastBottomSheet.observe(viewLifecycleOwner, {
            if (it != null) {
                val showCastBottomSheet = ShowCastBottomSheetFragment()

                val args = Bundle()
                args.putParcelable(ARG_SHOW_CAST, it)
                showCastBottomSheet.arguments = args

                activity?.supportFragmentManager?.let { fragmentManager ->
                    showCastBottomSheet.show(
                        fragmentManager,
                        ShowCastBottomSheetFragment.TAG
                    )
                }
                viewModel.displayCastBottomSheetComplete()
            }
        })

        viewModel.addToWatchlistResponse.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.onAddToWatchlistResponseReceived(it)
            }
        })

        viewModel.removeFromWatchlistResponse.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.onRemoveFromWatchlistResponseReceived(it)
            }
        })

        viewModel.watchedProgress.observe(viewLifecycleOwner, {
            if (it != null) {
                _traktShowWatchedProgress = it
            }
        })

        viewModel.showSeasons.observe(viewLifecycleOwner, {
            if (it != null) {
                _showSeasons = it
            }
        })

        viewModel.showWatchedProgressBottomSheet.observe(viewLifecycleOwner, {
            if (it != null) {
                val watchedProgressBottomSheet = ShowWatchedProgressBottomSheetFragment()

                val args = Bundle()
                args.putParcelable(ARG_WATCHED_PROGRESS, it)
                args.putParcelable(ARG_SHOW_DETAIL, this@ShowDetailFragment.args.show)
                watchedProgressBottomSheet.arguments = args

                activity?.supportFragmentManager?.let { fragmentManager ->
                    watchedProgressBottomSheet.show(
                        fragmentManager,
                        ShowWatchedProgressBottomSheetFragment.TAG
                    )
                }
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_show_detail_watched_progress_empty),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })

        viewModel.showSeasonsBottomSheet.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                val args = Bundle()
                args.putParcelableArrayList(ARG_SHOW_SEASONS, ArrayList(it))
                args.putParcelable(ARG_WATCHED_PROGRESS, _traktShowWatchedProgress)
                args.putParcelable(ARG_SHOW_DETAIL, this@ShowDetailFragment.showInfo)

                showBottomSheet(
                    bottomSheetFragment = ShowSeasonsBottomSheetFragment(),
                    fragmentArguments = args,
                    fragmentManager = activity?.supportFragmentManager,
                    fragmentTag = ShowSeasonsBottomSheetFragment.TAG
                )
            } else {
                Feedback(requireContext()).showSnackBar(
                    view = binding.root,
                    type = FeedBackStatus.SHOW_SEASONS_EMPTY,
                    duration = Snackbar.LENGTH_LONG,
                    listener = null
                )
            }
        })

        viewModel.showWatchlistInfoBottomSheet.observe(viewLifecycleOwner, {
            if (it == true) {
                HelpContentComponent.showContent(
                    HelpContentType.WATCHLIST_INFO,
                    activity?.supportFragmentManager
                )
                Firebase.analytics.logEvent("watchlist_info_icon_click", null)
                viewModel.showWatchlistInfoBottomSheetComplete()
            }
        })

        viewModel.showCollectionInfoBottomSheet.observe(viewLifecycleOwner, {
            if (it == true) {
                HelpContentComponent.showContent(
                    HelpContentType.COLLECTION_INFO,
                    activity?.supportFragmentManager
                )
                Firebase.analytics.logEvent("collection_info_icon_click", null)
                viewModel.showCollectionInfoBottomSheetComplete()
            }
        })

        viewModel.launchTraktConnectWindow.observe(viewLifecycleOwner, {
            if (it) {
                launchTraktWindow()
                viewModel.launchConnectWindowComplete()
            }
        })

        viewModel.showConnectToTraktInfoBottomSheet.observe(viewLifecycleOwner, {
            if (it == true) {
                HelpContentComponent.showContent(
                    HelpContentType.CONNECT_TO_TRAKT_INFO,
                    activity?.supportFragmentManager
                )
                Firebase.analytics.logEvent("trakt_connection_info_icon_click", null)
                viewModel.showConnectToTraktInfoBottomSheetComplete()
            }
        })

        viewModel.showConnectionToTraktRequiredError.observe(viewLifecycleOwner, {
            if (it == true) {
                Feedback(requireContext()).showSnackBar(
                    view = binding.root,
                    type = FeedBackStatus.CONNECTION_TO_TRAKT_REQUIRED,
                    duration = Snackbar.LENGTH_LONG,
                    listener = { launchTraktWindow() })

                viewModel.showConnectionToTraktRequiredComplete()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = args.show.showTitle
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _showCastAdapter = null
    }

    override fun onShowCastClick(view: View, castItem: ShowCast) {
        viewModel.onShowCastItemClicked(castItem)
    }

    companion object {
        const val ARG_SHOW_CAST = "show_cast"
        const val ARG_SHOW_DETAIL = "show_detail"
        const val ARG_SHOW_SEASONS = "show_seasons"
        const val ARG_WATCHED_PROGRESS = "watched_progress"
    }

}