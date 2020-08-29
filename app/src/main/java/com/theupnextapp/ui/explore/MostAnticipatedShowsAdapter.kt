package com.theupnextapp.ui.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.MostAnticipatedShowListItemBinding
import com.theupnextapp.domain.TraktMostAnticipated

class MostAnticipatedShowsAdapter(val listener: MostAnticipatedShowsAdapterListener) :
    RecyclerView.Adapter<MostAnticipatedShowsAdapter.ViewHolder>() {

    private var mostAnticipatedShowsList: List<TraktMostAnticipated> = ArrayList()

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

    fun submitList(anticipatedShowsList: List<TraktMostAnticipated>){
        val oldAnticipatedShowsList = mostAnticipatedShowsList
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            MostAnticipatedItemDiffCallback(
                oldAnticipatedShowsList,
                anticipatedShowsList
            )
        )
        mostAnticipatedShowsList = anticipatedShowsList
        diffResult.dispatchUpdatesTo(this)
    }

    class MostAnticipatedItemDiffCallback(
        private val oldAnticipatedShowsList: List<TraktMostAnticipated>,
        private val newAnticipatedShowsList: List<TraktMostAnticipated>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldAnticipatedShowsList[oldItemPosition].imdbID == newAnticipatedShowsList[newItemPosition].imdbID
        }

        override fun getOldListSize(): Int = oldAnticipatedShowsList.size

        override fun getNewListSize(): Int = newAnticipatedShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldAnticipatedShowsList[oldItemPosition].equals(newAnticipatedShowsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: MostAnticipatedShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.most_anticipated_show_list_item
        }
    }
}