package com.theupnextapp.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.SearchItemBinding
import com.theupnextapp.domain.ShowSearch

class SearchAdapter(val listener: SearchAdapterListener) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private var searchResults: List<ShowSearch> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: SearchItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = searchResults.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.show = searchResults[position]
            it.listener = listener
        }
    }

    interface SearchAdapterListener {
        fun onSearchItemClick(view: View, showSearch: ShowSearch)
    }

    fun submitList(searchResultsList: List<ShowSearch>) {
        val oldSearchResultsList = searchResults
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            SearchItemDiffCallback(
                oldSearchResultsList,
                searchResultsList
            )
        )
        searchResults = searchResultsList
        diffResult.dispatchUpdatesTo(this)
    }

    class SearchItemDiffCallback(
        private val oldSearchResultsList: List<ShowSearch>,
        private val newSearchResultsList: List<ShowSearch>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldSearchResultsList[oldItemPosition].id == newSearchResultsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldSearchResultsList.size

        override fun getNewListSize(): Int = newSearchResultsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldSearchResultsList[oldItemPosition].equals(newSearchResultsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: SearchItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.search_item
        }
    }
}