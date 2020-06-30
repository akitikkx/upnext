package com.theupnextapp.ui.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.LibraryListItemBinding
import com.theupnextapp.domain.LibraryList

class LibraryAdapter(val listener: LibraryAdapterListener) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    var libraryList: List<LibraryList> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: LibraryListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = libraryList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.item = libraryList[position]

            it.listener = listener

            it.libraryLeftIcon.setBackgroundResource(libraryList[position].leftIcon)

            it.libraryRightIcon.setBackgroundResource(libraryList[position].rightIcon)
        }
    }

    interface LibraryAdapterListener {
        fun onLibraryItemClick(view: View, libraryList: LibraryList)
    }

    class ViewHolder(val viewDataBinding: LibraryListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.library_list_item
        }
    }
}