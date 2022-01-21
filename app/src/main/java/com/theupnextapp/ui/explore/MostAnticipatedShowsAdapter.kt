package com.theupnextapp.ui.explore

import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.MostAnticipatedItemDiffCallback
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.ui.widgets.ListPosterCard
import com.theupnextapp.ui.common.ComposeAdapter
import com.theupnextapp.ui.common.ComposeViewHolder

class MostAnticipatedShowsAdapter :
    ComposeAdapter<TraktMostAnticipated, MostAnticipatedShowsAdapter.ViewHolder>() {

    override var list: List<TraktMostAnticipated> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ComposeView(parent.context))
    }

    override fun submitList(updateList: List<TraktMostAnticipated>){
        val oldAnticipatedShowsList = list
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            MostAnticipatedItemDiffCallback(
                oldAnticipatedShowsList,
                updateList
            )
        )
        list = updateList
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(composeView: ComposeView) :
        ComposeViewHolder<TraktMostAnticipated>(composeView) {

        override val source: String = "most_anticipated"

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun ComposableContainer(item: TraktMostAnticipated) {
            MdcTheme {
                ListPosterCard(
                    itemName = item.title,
                    itemUrl = item.originalImageUrl
                ) {
                    navigateFromAnticipatedToShowDetail(item, composeView)
                }
            }
        }
    }
}