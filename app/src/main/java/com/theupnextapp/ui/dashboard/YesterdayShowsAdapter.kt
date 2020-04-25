package com.theupnextapp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.YesterdayShowListItemBinding
import com.theupnextapp.domain.ScheduleShow


class YesterdayShowsAdapter(val listener: YesterdayShowsAdapterListener) :
    RecyclerView.Adapter<YesterdayShowsAdapter.ViewHolder>() {

    var yesterdayShows: List<ScheduleShow> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: YesterdayShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.yesterdayShowPoster.transitionName = "yesterday_${yesterdayShows[position].image}"
            it.show = yesterdayShows[position]
            it.listener = listener
        }
    }

    override fun getItemCount(): Int = yesterdayShows.size

    interface YesterdayShowsAdapterListener {
        fun onYesterdayShowClick(view: View, yesterdayShow: ScheduleShow)
    }

    class ViewHolder(val viewDataBinding: YesterdayShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.yesterday_show_list_item
        }
    }
}