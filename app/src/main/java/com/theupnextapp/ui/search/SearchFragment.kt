package com.theupnextapp.ui.search

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.theupnextapp.databinding.FragmentSearchBinding
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import com.theupnextapp.R

class SearchFragment : Fragment(),
    OnQueryTextListener {

    private lateinit var binding: FragmentSearchBinding

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
        binding = FragmentSearchBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.search.setIconifiedByDefault(true)
        binding.search.isFocusable = true
        binding.search.isIconified = false
        binding.search.requestFocusFromTouch()
        binding.search.queryHint = "Start typing the show name here..."
        binding.search.setOnQueryTextListener(this)

        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuItem  = menu.findItem(R.id.searchFragment)
        menuItem.isVisible = false

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
}