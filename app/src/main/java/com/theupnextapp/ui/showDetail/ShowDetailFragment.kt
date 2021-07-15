package com.theupnextapp.ui.showDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowDetailBinding
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.network.models.trakt.Distribution
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowDetailFragment : BaseFragment(), ShowCastAdapter.ShowCastAdapterListener {

    private var _binding: FragmentShowDetailBinding? = null
    private val binding get() = _binding!!

    private var _showCastAdapter: ShowCastAdapter? = null
    private val showCastAdapter get() = _showCastAdapter!!

    private var _showSeasons: List<ShowSeason>? = null
    private var showInfo: ShowInfo? = null

    private var _showRatingsAdapter: ShowRatingsAdapter? = null
    private val showRatingsAdapter get() = _showRatingsAdapter

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

        _showRatingsAdapter = ShowRatingsAdapter()

        binding.root.findViewById<RecyclerView>(R.id.cast_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = showCastAdapter
        }

        binding.traktShowRatings.ratingsList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = showRatingsAdapter
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

        viewModel.showSeasons.observe(viewLifecycleOwner, {
            if (it != null) {
                _showSeasons = it
            }
        })

        viewModel.navigateToSeasons.observe(viewLifecycleOwner, {
            if (it) {
                val directions =
                    ShowDetailFragmentDirections.actionShowDetailFragmentToShowSeasonsFragment(
                        ShowDetailArg(
                            showId = args.show.showId,
                            showTitle = args.show.showTitle,
                            showImageUrl = args.show.showImageUrl,
                            showBackgroundUrl = args.show.showBackgroundUrl
                        )
                    )
                findNavController().navigate(directions)
                viewModel.onSeasonsNavigationComplete()
            }
        })

        viewModel.showRating.observe(viewLifecycleOwner, {
            showRatingsAdapter?.setVotes(it.votes)

            val distributionList = mutableListOf<Distribution>()
            it.distribution?.forEach { (key,value) ->
                val distribution = Distribution(
                    score = key,
                    value = value
                )
                distributionList.add(distribution)
            }
            showRatingsAdapter?.submitList(distributionList.asReversed())
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
        _showRatingsAdapter = null
    }

    override fun onShowCastClick(view: View, castItem: ShowCast) {
        viewModel.onShowCastItemClicked(castItem)
    }

    companion object {
        const val ARG_SHOW_CAST = "show_cast"
        const val ARG_SHOW_DETAIL = "show_detail"
        const val ARG_SHOW_SEASONS = "show_seasons"
    }

}