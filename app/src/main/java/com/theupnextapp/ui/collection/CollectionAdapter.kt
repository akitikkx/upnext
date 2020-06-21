package com.theupnextapp.ui.collection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktCollectionItemBinding
import com.theupnextapp.domain.TraktCollection

class CollectionAdapter(val listener: CollectionAdapterListener) :
    RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

    var traktCollection: List<TraktCollection> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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

    class ViewHolder(val viewDataBinding: TraktCollectionItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_collection_item
        }
    }
}