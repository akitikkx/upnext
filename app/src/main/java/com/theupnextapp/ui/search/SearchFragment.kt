package com.theupnextapp.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentSearchBinding
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSearch
import com.theupnextapp.ui.common.BaseFragment

class SearchFragment : BaseFragment(),
    OnQueryTextListener, SearchAdapter.SearchAdapterListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var searchAdapter: SearchAdapter? = null

    private val viewModel: SearchViewModel by lazy {
        val activity = requireNotNull(activity) {
            "You can only access the viewModel after onActivityCreated"
        }
        ViewModelProviders.of(
            this,
            SearchViewModel.Factory(activity.application)
        ).get(SearchViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.search.setIconifiedByDefault(true)
        binding.search.isFocusable = true
        binding.search.isIconified = false
        binding.search.requestFocusFromTouch()
        binding.search.queryHint = "Start typing the show name here..."
        binding.search.setOnQueryTextListener(this)

        searchAdapter = SearchAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.search_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = searchAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.searchResults.observe(viewLifecycleOwner, Observer {
            searchAdapter?.searchResults = it
        })

        viewModel.navigateToSelectedShow.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController().navigate(
                    SearchFragmentDirections.actionSearchFragmentToShowDetailFragment(it)
                )
                val analyticsBundle = Bundle()
                analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, it.showId.toString())
                analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, it.showTitle)
                analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "search_show")

                Firebase.analytics.logEvent("search_show_click", analyticsBundle)
                viewModel.displayShowDetailsComplete()
            }
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.onQueryTextSubmit(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.onQueryTextChange(newText)
        return true
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = "Show search"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchAdapter = null
        (activity as MainActivity).hideKeyboard()
    }

    override fun onSearchItemClick(view: View, showSearch: ShowSearch) {
        viewModel.displayShowDetails(
            ShowDetailArg(
                source = "history",
                showId = showSearch.id,
                showTitle = showSearch.name,
                showImageUrl = showSearch.originalImageUrl
            )
        )
    }
}