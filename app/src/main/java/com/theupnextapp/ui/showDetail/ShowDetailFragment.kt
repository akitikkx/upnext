package com.theupnextapp.ui.showDetail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowDetailBinding
import com.theupnextapp.domain.ShowCast

class ShowDetailFragment : Fragment(), ShowCastAdapter.ShowCastAdapterListener {

    private lateinit var binding: FragmentShowDetailBinding
    private var showCastAdapter: ShowCastAdapter? = null

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
        binding = FragmentShowDetailBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        showCastAdapter = ShowCastAdapter(this)

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
                viewModel.onShowInfoReceived(it)
            }
        })

        viewModel.watchlistRecord.observe(viewLifecycleOwner, Observer {
            viewModel.onWatchlistRecordReceived(it)
        })

        viewModel.showCast.observe(viewLifecycleOwner, Observer {
            viewModel.onShowCastInfoReceived(it)
            if (it != null) {
                showCastAdapter?.cast = it
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

    override fun onShowCastClick(view: View, castItem: ShowCast) {

    }

}