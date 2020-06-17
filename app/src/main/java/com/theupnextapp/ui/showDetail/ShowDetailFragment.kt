package com.theupnextapp.ui.showDetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.google.android.material.snackbar.Snackbar
import com.theupnextapp.BuildConfig
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.common.utils.*
import com.theupnextapp.databinding.FragmentShowDetailBinding
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.TraktShowWatchedProgress
import com.theupnextapp.ui.common.BaseFragment

class ShowDetailFragment : BaseFragment(), ShowCastAdapter.ShowCastAdapterListener {

    private var _binding: FragmentShowDetailBinding? = null
    private val binding get() = _binding!!

    private var _showCastAdapter: ShowCastAdapter? = null
    private val showCastAdapter get() = _showCastAdapter!!

    private var _traktShowWatchedProgress: TraktShowWatchedProgress? = null
    private var _showSeasons: List<ShowSeason>? = null
    private var showInfo: ShowInfo? = null

    val args by navArgs<ShowDetailFragmentArgs>()

    private val viewModel: ShowDetailViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            ShowDetailViewModel.Factory(activity.application, args.show)
        ).get(ShowDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.showInfo.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showInfo = it
            }
        })

        viewModel.watchlistRecord.observe(viewLifecycleOwner, Observer {
            viewModel.onWatchlistRecordReceived(it)
        })

        viewModel.showCast.observe(viewLifecycleOwner, Observer {
            viewModel.onShowCastInfoReceived(it)
            if (it != null) {
                showCastAdapter.cast = it
            }
        })

        viewModel.showCastBottomSheet.observe(viewLifecycleOwner, Observer {
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

        viewModel.addToWatchlistResponse.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.onAddToWatchlistResponseReceived(it)
            }
        })

        viewModel.removeFromWatchlistResponse.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.onRemoveFromWatchlistResponseReceived(it)
            }
        })

        viewModel.launchTraktConnectWindow.observe(viewLifecycleOwner, Observer {
            if (it) {
                connectToTraktWindow()
            }
        })

        viewModel.watchedProgress.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                _traktShowWatchedProgress = it
            }
        })

        viewModel.showSeasons.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                _showSeasons = it
            }
        })

        viewModel.showWatchedProgressBottomSheet.observe(viewLifecycleOwner, Observer {
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

        viewModel.showSeasonsBottomSheet.observe(viewLifecycleOwner, Observer {
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

        viewModel.invalidToken.observe(viewLifecycleOwner, Observer {
            if (it) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_trakt_invalid_token_response_received),
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.onInvalidTokenResponseReceived(it)
            }
        })

        viewModel.invalidGrant.observe(viewLifecycleOwner, Observer {
            if (it) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_trakt_invalid_grant_response_received),
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.onInvalidTokenResponseReceived(it)
            }
        })

        viewModel.showWatchlistInfoBottomSheet.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                HelpContentComponent.showContent(
                    HelpContentType.WATCHLIST_INFO,
                    activity?.supportFragmentManager
                )
                viewModel.showWatchlistInfoBottomSheetComplete()
            }
        })

        viewModel.showCollectionInfoBottomSheet.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                HelpContentComponent.showContent(
                    HelpContentType.COLLECTION_INFO,
                    activity?.supportFragmentManager
                )
                viewModel.showCollectionInfoBottomSheetComplete()
            }
        })

        viewModel.showConnectToTraktInfoBottomSheet.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                HelpContentComponent.showContent(
                    HelpContentType.CONNECT_TO_TRAKT_INFO,
                    activity?.supportFragmentManager
                )
                viewModel.showConnectToTraktInfoBottomSheetComplete()
            }
        })

        viewModel.showConnectionToTraktRequiredError.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Feedback(requireContext()).showSnackBar(
                    view = binding.root,
                    type = FeedBackStatus.CONNECTION_TO_TRAKT_REQUIRED,
                    duration = Snackbar.LENGTH_LONG,
                    listener = View.OnClickListener { connectToTraktWindow() })

                viewModel.showConnectionToTraktRequiredComplete()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = args.show.showTitle
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onDetach() {
        super.onDetach()
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

    private fun connectToTraktWindow() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("${TRAKT_API_URL}${TRAKT_OAUTH_ENDPOINT}?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}")
        )
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, activity?.packageName)
        startActivity(intent)
        viewModel.launchConnectWindowComplete()
    }

    companion object {
        const val ARG_SHOW_CAST = "show_cast"
        const val ARG_SHOW_DETAIL = "show_detail"
        const val ARG_SHOW_SEASONS = "show_seasons"
        const val ARG_WATCHED_PROGRESS = "watched_progress"
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
        const val TRAKT_API_URL = "https://api.trakt.tv"
        const val TRAKT_OAUTH_ENDPOINT = "/oauth/authorize"
    }

}