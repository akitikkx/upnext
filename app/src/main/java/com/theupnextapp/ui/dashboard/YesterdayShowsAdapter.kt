package com.theupnextapp.ui.dashboard

import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.ui.components.ListPosterCard

class YesterdayShowsAdapter :
    DashboardAdapter<ScheduleShow, YesterdayShowsAdapter.ComposeViewHolder>() {

    override var list: List<ScheduleShow> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        return ComposeViewHolder(ComposeView(parent.context))
    }

    override fun submitList(updateList: List<ScheduleShow>) {
        // old list is equal to the current list
        val oldList = list
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            YesterdayShowItemDiffCallback(
                oldList,
                updateList
            )
        )
        list = updateList
        diffResult.dispatchUpdatesTo(this)
    }

    class YesterdayShowItemDiffCallback(
        private val oldYesterdayShowsList: List<ScheduleShow>,
        private val newYesterdayShowsList: List<ScheduleShow>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldYesterdayShowsList[oldItemPosition].id == newYesterdayShowsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldYesterdayShowsList.size

        override fun getNewListSize(): Int = newYesterdayShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldYesterdayShowsList[oldItemPosition].equals(newYesterdayShowsList[newItemPosition])
        }
    }

    class ComposeViewHolder(composeView: ComposeView) :
        DashboardViewHolder<ScheduleShow>(composeView) {

        override val source: String = "yesterday"

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun ComposableContainer(item: ScheduleShow) {
            MdcTheme {
                ListPosterCard(
                    itemName = item.name,
                    itemUrl = item.originalImage
                ) {
                    navigateFromDashboardToShowDetail(item, composeView)
                }
            }
        }
    }
}