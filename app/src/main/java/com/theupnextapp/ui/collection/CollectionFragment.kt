package com.theupnextapp.ui.collection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentCollectionBinding
import com.theupnextapp.domain.TraktCollection
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CollectionFragment : BaseFragment(), CollectionAdapter.CollectionAdapterListener {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    private var _adapter: CollectionAdapter? = null
    private val adapter get() = _adapter!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val viewModel by viewModels<CollectionViewModel>()

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
        _binding = FragmentCollectionBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        _adapter = CollectionAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.collection_list).apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = this@CollectionFragment.adapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isAuthorizedOnTrakt.observe(viewLifecycleOwner, {
            if (it == false) {
                this.findNavController().navigate(
                    CollectionFragmentDirections.actionCollectionFragmentToLibraryFragment()
                )
            }
        })

        viewModel.traktCollection.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                adapter.submitList(it)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.title_trakt_collection)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _adapter = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onCollectionClick(view: View, traktCollection: TraktCollection) {
        val directions =
            CollectionFragmentDirections.actionCollectionFragmentToCollectionSeasonsFragment(
                TraktCollectionArg(
                    imdbID = traktCollection.imdbID,
                    title = traktCollection.title,
                    mediumImageUrl = traktCollection.mediumImageUrl,
                    originalImageUrl = traktCollection.originalImageUrl,
                    lastCollectedAt = traktCollection.lastCollectedAt,
                    lastUpdatedAt = traktCollection.lastUpdatedAt
                )
            )
        findNavController().navigate(directions, getCollectionNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(
            FirebaseAnalytics.Param.ITEM_ID,
            traktCollection.tvMazeID.toString()
        )
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, traktCollection.title)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "collection_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }

    override fun onCollectionRemoveClick(view: View, traktCollection: TraktCollection) {

    }

    private fun getCollectionNavigatorExtras(view: View): FragmentNavigator.Extras {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.show_motion_duration_large).toLong()
        }
        return FragmentNavigatorExtras(view to getString(R.string.collection_item_transition_name))
    }
}