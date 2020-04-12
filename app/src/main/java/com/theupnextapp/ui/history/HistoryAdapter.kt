package com.theupnextapp.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktHistoryItemBinding
import com.theupnextapp.domain.TraktHistory

class HistoryAdapter(val listener: HistoryAdapterListener) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    var history: List<TraktHistory> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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
        holder.viewDataBindiing.also {
            it.listener = listener
            it.show = history[position]
        }
    }

    class HistoryAdapterListener(val block: (TraktHistory) -> Unit) {
        fun onClick(historyItem: TraktHistory) = block(historyItem)
    }

    class ViewHolder(val viewDataBindiing: TraktHistoryItemBinding) :
        RecyclerView.ViewHolder(viewDataBindiing.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_history_item
        }
    }

}