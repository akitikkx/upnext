package com.theupnextapp.ui.watchlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktWatchlistItemBinding
import com.theupnextapp.domain.TraktWatchlist

class WatchlistAdapter(val listener: WatchlistAdapterListener) :
    RecyclerView.Adapter<WatchlistAdapter.ViewHolder>() {

    private var watchlist: List<TraktWatchlist> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TraktWatchlistItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = watchlist.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.show = watchlist[position]
            it.listener = listener
        }
    }

    interface WatchlistAdapterListener {
        fun onWatchlistShowClick(view: View, watchlistItem: TraktWatchlist)

        fun onWatchlistItemDeleteClick(view: View, watchlistItem: TraktWatchlist)
    }

    fun submitList(watchlistList: List<TraktWatchlist>) {
        val oldWatchlistList = watchlist
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            WatchlistItemCallback(
                oldWatchlistList,
                watchlistList
            )
        )
        watchlist = watchlistList
        diffResult.dispatchUpdatesTo(this)
    }

    class WatchlistItemCallback(
        private val oldWatchlistList: List<TraktWatchlist>,
        private val newWatchlistList: List<TraktWatchlist>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldWatchlistList[oldItemPosition].id == newWatchlistList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldWatchlistList.size

        override fun getNewListSize(): Int = newWatchlistList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldWatchlistList[oldItemPosition].equals(newWatchlistList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: TraktWatchlistItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_watchlist_item
        }
    }
}