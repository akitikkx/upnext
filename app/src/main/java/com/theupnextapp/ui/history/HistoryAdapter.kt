package com.theupnextapp.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktHistoryItemBinding
import com.theupnextapp.domain.TraktHistory

class HistoryAdapter(val listener: HistoryAdapterListener) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var history: List<TraktHistory> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TraktHistoryItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = history.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.listener = listener
            it.show = history[position]
        }
    }

    interface HistoryAdapterListener {
        fun onHistoryShowClick(view: View, historyItem: TraktHistory)

        fun onHistoryRemoveClick(view: View, historyItem: TraktHistory)
    }

    fun submitList(historyList: List<TraktHistory>) {
        val oldHistoryList = history
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            HistoryItemDiffCallback(
                oldHistoryList,
                historyList
            )
        )
        history = historyList
        diffResult.dispatchUpdatesTo(this)
    }

    class HistoryItemDiffCallback(
        private val oldHistoryList: List<TraktHistory>,
        private val newHistoryList: List<TraktHistory>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldHistoryList[oldItemPosition].id == newHistoryList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldHistoryList.size

        override fun getNewListSize(): Int = newHistoryList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldHistoryList[oldItemPosition].equals(newHistoryList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: TraktHistoryItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_history_item
        }
    }

}