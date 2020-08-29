package com.theupnextapp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.YesterdayShowListItemBinding
import com.theupnextapp.domain.ScheduleShow


class YesterdayShowsAdapter(val listener: YesterdayShowsAdapterListener) :
    RecyclerView.Adapter<YesterdayShowsAdapter.ViewHolder>() {

    private var yesterdayShows: List<ScheduleShow> = ArrayList()

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
            it.show = yesterdayShows[position]
            it.listener = listener
        }
    }

    override fun getItemCount(): Int = yesterdayShows.size

    interface YesterdayShowsAdapterListener {
        fun onYesterdayShowClick(view: View, yesterdayShow: ScheduleShow)
    }

    fun submitList(yesterdayShowsList: List<ScheduleShow>) {
        // old list is equal to the current list
        val oldList = yesterdayShows
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            YesterdayShowItemDiffCallback(
                oldList,
                yesterdayShowsList
            )
        )
        yesterdayShows = yesterdayShowsList
        diffResult.dispatchUpdatesTo(this)
    }

    class YesterdayShowItemDiffCallback(
        private val oldYesterdayShowsList: List<ScheduleShow>,
        private val newYesterdayShowsList: List<ScheduleShow>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldYesterdayShowsList[oldItemPosition].id == newYesterdayShowsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldYesterdayShowsList.size

        override fun getNewListSize(): Int = newYesterdayShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldYesterdayShowsList[oldItemPosition].equals(newYesterdayShowsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: YesterdayShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.yesterday_show_list_item
        }
    }
}