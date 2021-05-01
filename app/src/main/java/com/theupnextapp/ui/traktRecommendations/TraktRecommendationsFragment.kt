package com.theupnextapp.ui.traktRecommendations

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentTraktRecommendationsBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktRecommendations
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TraktRecommendationsFragment : BaseFragment(),
    TraktRecommendationsAdapter.TraktRecommendationsAdapterListener {

    private var _binding: FragmentTraktRecommendationsBinding? = null
    private val binding get() = _binding!!

    private var _recommendedShowsAdapter: TraktRecommendationsAdapter? = null
    private val recommendedShowsAdapter get() = _recommendedShowsAdapter!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val viewModel by viewModels<TraktRecommendationsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTraktRecommendationsBinding.inflate(inflater)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        _recommendedShowsAdapter =
            TraktRecommendationsAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.recommendations_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = recommendedShowsAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, {
            if (it == false) {
                this.findNavController().navigate(
                    TraktRecommendationsFragmentDirections.actionTraktRecommendationsFragmentToLibraryFragment()
                )
            }
        })

        viewModel.traktRecommendationsList.observe(
            viewLifecycleOwner,
            { recommendedShows ->
                recommendedShows.apply {
                    if (!recommendedShows.isNullOrEmpty()) {
                        recommendedShowsAdapter.submitList(recommendedShows)
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _recommendedShowsAdapter = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onRecommendedShowClick(view: View, traktRecommendations: TraktRecommendations) {
        this.findNavController().navigate(
            TraktRecommendationsFragmentDirections.actionTraktRecommendationsFragmentToShowDetailFragment(
                ShowDetailArg(
                    source = "trakt_recommendations",
                    showId = traktRecommendations.tvMazeID,
                    showTitle = traktRecommendations.title,
                    showImageUrl = traktRecommendations.originalImageUrl
                )
            ),
            getShowDetailNavigatorExtras(view)
        )

        val analyticsBundle = Bundle()
        analyticsBundle.putString(
            FirebaseAnalytics.Param.ITEM_ID,
            traktRecommendations.tvMazeID.toString()
        )
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, traktRecommendations.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "trakt_recommended_show")

        Firebase.analytics.logEvent("recommended_shows_click", analyticsBundle)
    }

}