package com.theupnextapp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TomorrowShowListItemBinding
import com.theupnextapp.domain.ScheduleShow

class TomorrowShowsAdapter(val listener: TomorrowShowsAdapterListener) :
    RecyclerView.Adapter<TomorrowShowsAdapter.ViewHolder>() {

    private var tomorrowShows: List<ScheduleShow> = ArrayList()

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
            it.show = tomorrowShows[position]
            it.listener = listener
        }
    }

    override fun getItemCount(): Int = tomorrowShows.size

    interface TomorrowShowsAdapterListener {
        fun onTomorrowShowClick(view : View, scheduleShow: ScheduleShow)
    }

    fun submitList(tomorrowShowsList: List<ScheduleShow>) {
        val oldTomorrowShowsList = tomorrowShows
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TomorrowShowItemDiffCallback(
                oldTomorrowShowsList,
                tomorrowShowsList
            )
        )
        tomorrowShows = tomorrowShowsList
        diffResult.dispatchUpdatesTo(this)
    }

    class TomorrowShowItemDiffCallback(
        private val oldTomorrowShowsList: List<ScheduleShow>,
        private val newTomorrowShowsList: List<ScheduleShow>
    ): DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTomorrowShowsList[oldItemPosition].id == newTomorrowShowsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldTomorrowShowsList.size

        override fun getNewListSize(): Int  = newTomorrowShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTomorrowShowsList[oldItemPosition].equals(newTomorrowShowsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: TomorrowShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.tomorrow_show_list_item
        }
    }
}