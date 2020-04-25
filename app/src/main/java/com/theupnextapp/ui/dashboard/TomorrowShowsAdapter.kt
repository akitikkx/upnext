package com.theupnextapp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TomorrowShowListItemBinding
import com.theupnextapp.domain.ScheduleShow

class TomorrowShowsAdapter(val listener: TomorrowShowsAdapterListener) :
    RecyclerView.Adapter<TomorrowShowsAdapter.ViewHolder>() {

    var tomorrowShows: List<ScheduleShow> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TomorrowShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.tomorrowShowPoster.transitionName = "tomorrow_${tomorrowShows[position].image}"
            it.show = tomorrowShows[position]
            it.listener = listener
        }
    }

    override fun getItemCount(): Int = tomorrowShows.size

    interface TomorrowShowsAdapterListener {
        fun onTomorrowShowClick(view : View, scheduleShow: ScheduleShow)
    }

    class ViewHolder(val viewDataBinding: TomorrowShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.tomorrow_show_list_item
        }
    }
}