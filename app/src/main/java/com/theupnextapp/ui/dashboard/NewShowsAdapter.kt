package com.theupnextapp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.NewShowListItemBinding
import com.theupnextapp.domain.NewShows

class NewShowsAdapter(val listener: NewShowsAdapterListener) :
    RecyclerView.Adapter<NewShowsAdapter.ViewHolder>() {

    var newShows: List<NewShows> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: NewShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.show = newShows[position]
            it.listener = listener
        }
    }

    override fun getItemCount(): Int = newShows.size

    interface NewShowsAdapterListener {
        fun onNewShowClick(view : View, newShow: NewShows)
    }

    class ViewHolder(val viewDataBinding: NewShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.new_show_list_item
        }
    }
}