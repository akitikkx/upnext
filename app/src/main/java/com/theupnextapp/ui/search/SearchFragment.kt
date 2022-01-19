package com.theupnextapp.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.MainActivity
import com.theupnextapp.R
import com.theupnextapp.databinding.FragmentSearchBinding
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSearch
import com.theupnextapp.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : BaseFragment(),
    OnQueryTextListener, SearchAdapter.SearchAdapterListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var _searchAdapter: SearchAdapter? = null
    private val searchAdapter get() = _searchAdapter!!

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val viewModel by viewModels<SearchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.search.setIconifiedByDefault(true)
        binding.search.isFocusable = true
        binding.search.isIconified = false
        binding.search.requestFocusFromTouch()
        binding.search.queryHint = "Start typing the show name here..."
        binding.search.setOnQueryTextListener(this)

        _searchAdapter = SearchAdapter(this)

        binding.root.findViewById<RecyclerView>(R.id.search_list).apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = searchAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.searchResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Result.Success -> searchAdapter.submitList(it.data)
                is Result.GenericError -> {
                    it.error?.message?.let { errorMessage ->
                        Snackbar.make(
                            binding.root,
                            errorMessage, Snackbar.LENGTH_LONG
                        )
                    }
                }
                is Result.Loading -> {
                    binding.isLoading = it.status
                }
                is Result.NetworkError -> {}
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
        _searchAdapter = null
        (activity as MainActivity).hideKeyboard()
    }

    override fun onSearchItemClick(view: View, showSearch: ShowSearch) {
        val directions = SearchFragmentDirections.actionSearchFragmentToShowDetailFragment(
            ShowDetailArg(
                source = "search",
                showId = showSearch.id,
                showTitle = showSearch.name,
                showImageUrl = showSearch.originalImageUrl,
                showBackgroundUrl = showSearch.mediumImageUrl
            )
        )
        findNavController().navigate(directions, getShowDetailNavigatorExtras(view))

        val analyticsBundle = Bundle()
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_ID, showSearch.id.toString())
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, showSearch.name)
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "search_show")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, analyticsBundle)
    }
}