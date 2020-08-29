package com.theupnextapp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TodayShowListItemBinding
import com.theupnextapp.domain.ScheduleShow

class TodayShowsAdapter(val listener: TodayShowsAdapterListener) :
    RecyclerView.Adapter<TodayShowsAdapter.ViewHolder>() {

    var todayShows: List<ScheduleShow> = ArrayList()

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
            it.show = todayShows[position]
            it.listener = listener
        }
    }

    override fun getItemCount(): Int = todayShows.size

    interface TodayShowsAdapterListener {
        fun onTodayShowClick(view: View, scheduleShow: ScheduleShow)
    }

    fun submitList(todayShowsList: List<ScheduleShow>) {
        val oldItems = todayShows
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TodayShowItemDiffCallback(
                oldItems,
                todayShowsList
            )
        )
        todayShows = todayShowsList
        diffResult.dispatchUpdatesTo(this)
    }

    class TodayShowItemDiffCallback(
        private val oldTodayShowsList: List<ScheduleShow>,
        private val newTodayShowsList: List<ScheduleShow>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTodayShowsList[oldItemPosition].id == newTodayShowsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldTodayShowsList.size

        override fun getNewListSize(): Int = newTodayShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTodayShowsList[oldItemPosition].equals(newTodayShowsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: TodayShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.today_show_list_item
        }
    }
}