package com.theupnextapp.ui.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.MostAnticipatedShowListItemBinding
import com.theupnextapp.domain.TraktMostAnticipated

class MostAnticipatedShowsAdapter(val listener: MostAnticipatedShowsAdapterListener) :
    RecyclerView.Adapter<MostAnticipatedShowsAdapter.ViewHolder>() {

    var mostAnticipatedShowsList: List<TraktMostAnticipated> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: MostAnticipatedShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = mostAnticipatedShowsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.listener = listener

            it.show = mostAnticipatedShowsList[position]
        }
    }

    interface MostAnticipatedShowsAdapterListener {
        fun onMostAnticipatedShowClick(view: View, mostAnticipated: TraktMostAnticipated)
    }

    class ViewHolder(val viewDataBinding: MostAnticipatedShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.most_anticipated_show_list_item
        }
    }
}