package com.theupnextapp.ui.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TrendingShowListItemBinding
import com.theupnextapp.domain.TraktTrending

class TrendingShowsAdapter(val listener: TrendingShowsAdapterListener) :
    RecyclerView.Adapter<TrendingShowsAdapter.ViewHolder>() {

    var trendingList: List<TraktTrending> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TrendingShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = trendingList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.listener = listener

            it.show = trendingList[position]
        }
    }

    interface TrendingShowsAdapterListener {
        fun onTrendingShowClick(view: View, traktTrending: TraktTrending)
    }

    class ViewHolder(val viewDataBinding: TrendingShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trending_show_list_item
        }
    }
}