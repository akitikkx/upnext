package com.theupnextapp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TodayShowListItemBinding
import com.theupnextapp.domain.ScheduleShow

class TodayShowsAdapter(val listener: TodayShowsAdapterListener) :
    RecyclerView.Adapter<TodayShowsAdapter.ViewHolder>() {

    var todayShows: List<ScheduleShow> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TodayShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.todayShowPoster.transitionName = "today_${todayShows[position].image}"
            it.show = todayShows[position]
            it.listener = listener
        }
    }

    override fun getItemCount(): Int = todayShows.size

    interface TodayShowsAdapterListener {
        fun onTodayShowClick(view : View, scheduleShow: ScheduleShow)
    }

    class ViewHolder(val viewDataBinding: TodayShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.today_show_list_item
        }
    }
}