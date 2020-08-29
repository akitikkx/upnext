package com.theupnextapp.ui.collection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktCollectionItemBinding
import com.theupnextapp.domain.TraktCollection

class CollectionAdapter(val listener: CollectionAdapterListener) :
    RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

    private var traktCollection: List<TraktCollection> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TraktCollectionItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = traktCollection.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.show = traktCollection[position]

            it.listener = listener
        }
    }

    interface CollectionAdapterListener {

        fun onCollectionClick(view: View, traktCollection: TraktCollection)

        fun onCollectionRemoveClick(view: View, traktCollection: TraktCollection)
    }

    fun submitList(collectionItemsList: List<TraktCollection>) {
        val oldCollectionItemsList = traktCollection
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            CollectionItemDiffCallback(
                oldCollectionItemsList,
                collectionItemsList
            )
        )
        traktCollection = collectionItemsList
        diffResult.dispatchUpdatesTo(this)
    }

    class CollectionItemDiffCallback(
        private var oldCollectionItemsList: List<TraktCollection>,
        private var newCollectionItemsList: List<TraktCollection>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldCollectionItemsList[oldItemPosition].id == newCollectionItemsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldCollectionItemsList.size

        override fun getNewListSize(): Int = newCollectionItemsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldCollectionItemsList[oldItemPosition].equals(newCollectionItemsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: TraktCollectionItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_collection_item
        }
    }
}