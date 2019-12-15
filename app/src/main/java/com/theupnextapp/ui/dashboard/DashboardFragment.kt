package com.theupnextapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentDashboardBinding
import com.theupnextapp.domain.RecommendedShows

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    private var recommendedShowsAdapter: RecommendedShowsAdapter? = null

    private val viewModel: DashboardViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            DashboardViewModel.Factory(activity.application)
        ).get(DashboardViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        recommendedShowsAdapter = RecommendedShowsAdapter(RecommendedShowsAdapter.RecommendedShowsAdapterListener {
            // TODO handle click of show
        })

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        binding.root.findViewById<RecyclerView>(R.id.recommended_shows_list).apply {
            layoutManager = linearLayoutManager
            adapter = recommendedShowsAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.recommendedShowsList.observe(viewLifecycleOwner, Observer<List<RecommendedShows>>{ recommendedShows ->
            recommendedShows.apply {
                recommendedShowsAdapter?.recommendedShows = recommendedShows
            }
        })
    }
}