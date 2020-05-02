package com.theupnextapp.ui.showDetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.ShowCastItemBinding
import com.theupnextapp.domain.ShowCast

class ShowCastAdapter(val listener: ShowCastAdapterListener) :
    RecyclerView.Adapter<ShowCastAdapter.ViewHolder>() {

    var cast: List<ShowCast> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: ShowCastItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = cast.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.listener = listener
            it.cast = cast[position]
        }
    }

    interface ShowCastAdapterListener {
        fun onShowCastClick(view: View, castItem: ShowCast)
    }

    class ViewHolder(val viewDataBinding: ShowCastItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.show_cast_item
        }

    }
}