package com.theupnextapp.ui.showDetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.ShowCastItemBinding
import com.theupnextapp.domain.ShowCast

class ShowCastAdapter(val listener: ShowCastAdapterListener) :
    RecyclerView.Adapter<ShowCastAdapter.ViewHolder>() {

    private var cast: List<ShowCast> = ArrayList()

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

    fun submitList(showCastList: List<ShowCast>) {
        val oldShowCastList = cast
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            ShowCastItemDiffCallback(
                oldShowCastList,
                showCastList
            )
        )
        cast = showCastList
        diffResult.dispatchUpdatesTo(this)
    }

    class ShowCastItemDiffCallback(
        private val oldShowCastList: List<ShowCast>,
        private val newShowCastList: List<ShowCast>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldShowCastList[oldItemPosition].id == newShowCastList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldShowCastList.size

        override fun getNewListSize(): Int = newShowCastList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldShowCastList[oldItemPosition].equals(newShowCastList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: ShowCastItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.show_cast_item
        }

    }
}