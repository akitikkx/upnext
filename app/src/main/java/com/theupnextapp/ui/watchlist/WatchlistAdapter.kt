package com.theupnextapp.ui.watchlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktWatchlistItemBinding
import com.theupnextapp.domain.TraktWatchlist

class WatchlistAdapter(val listener: WatchlistAdapterListener) : RecyclerView.Adapter<WatchlistAdapter.ViewHolder>() {

    var watchlist: List<TraktWatchlist> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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
            it.traktWatchlistPoster.transitionName = "watchlist_${watchlist[position].originalImageUrl}"
            it.show = watchlist[position]
            it.listener = listener
        }
    }

    interface WatchlistAdapterListener {
        fun onWatchlistShowClick(view : View, watchlistItem: TraktWatchlist)
    }

    class ViewHolder(val viewDataBinding: TraktWatchlistItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_watchlist_item
        }
    }
}