package com.theupnextapp.ui.showDetail

import android.content.Context
import android.os.Bundle
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
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentShowDetailBinding
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.ui.common.BaseFragment

class ShowDetailFragment : BaseFragment(), ShowCastAdapter.ShowCastAdapterListener {

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

        viewModel.watchlistRecord.observe(viewLifecycleOwner, Observer {
            viewModel.onWatchlistRecordReceived(it)
        })

        viewModel.showCast.observe(viewLifecycleOwner, Observer {
            viewModel.onShowCastInfoReceived(it)
            if (it != null) {
                showCastAdapter?.cast = it
            }
        })

        viewModel.showCastBottomSheet.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val showCastBottomSheet = ShowCastBottomSheetFragment()

                val args = Bundle()
                args.putParcelable(ARG_SHOW_CAST, it)
                showCastBottomSheet.arguments = args

                activity?.supportFragmentManager?.let { activity -> showCastBottomSheet.show(activity, ShowCastBottomSheetFragment.TAG) }
                viewModel.displayCastBottomSheetComplete()
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
        viewModel.onShowCastItemClicked(castItem)
    }

    companion object {
        const val ARG_SHOW_CAST = "show_cast"
    }

}