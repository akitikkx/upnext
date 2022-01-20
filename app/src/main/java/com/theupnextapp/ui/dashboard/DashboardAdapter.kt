package com.theupnextapp.ui.dashboard

import androidx.recyclerview.widget.RecyclerView

abstract class DashboardAdapter<T, VH: DashboardViewHolder<T>>: RecyclerView.Adapter<VH>() {

    abstract var list: List<T>

    override fun getItemCount(): Int = list.size

    override fun onViewRecycled(holder: VH) {
        // Dispose the underlying Composition of the ComposeView
        // when RecyclerView has recycled this ViewHolder
        holder.composeView.disposeComposition()
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    abstract fun submitList(updateList: List<T>)
}