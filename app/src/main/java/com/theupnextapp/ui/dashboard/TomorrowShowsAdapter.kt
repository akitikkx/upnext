package com.theupnextapp.ui.dashboard

import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.ui.components.ListPosterCard

class TomorrowShowsAdapter : RecyclerView.Adapter<TomorrowShowsAdapter.ComposeViewHolder>() {

    private var tomorrowShows: List<ScheduleShow> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        return ComposeViewHolder(ComposeView(parent.context))
    }

    override fun getItemCount(): Int = tomorrowShows.size

    fun submitList(tomorrowShowsList: List<ScheduleShow>) {
        val oldTomorrowShowsList = tomorrowShows
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TomorrowShowItemDiffCallback(
                oldTomorrowShowsList,
                tomorrowShowsList
            )
        )
        tomorrowShows = tomorrowShowsList
        diffResult.dispatchUpdatesTo(this)
    }

    class TomorrowShowItemDiffCallback(
        private val oldTomorrowShowsList: List<ScheduleShow>,
        private val newTomorrowShowsList: List<ScheduleShow>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTomorrowShowsList[oldItemPosition].id == newTomorrowShowsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldTomorrowShowsList.size

        override fun getNewListSize(): Int = newTomorrowShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTomorrowShowsList[oldItemPosition].equals(newTomorrowShowsList[newItemPosition])
        }

    }

    override fun onViewRecycled(holder: ComposeViewHolder) {
        // Dispose the underlying Composition of the ComposeView
        // when RecyclerView has recycled this ViewHolder
        holder.composeView.disposeComposition()
    }

    override fun onBindViewHolder(holder: ComposeViewHolder, position: Int) {
        val item = tomorrowShows[position]
        holder.bind(item)
    }

    class ComposeViewHolder(composeView: ComposeView) :
        DashboardViewHolder<ScheduleShow>(composeView) {

        override val source: String = "tomorrow"

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun ComposableContainer(item: ScheduleShow) {
            MdcTheme {
                ListPosterCard(item) {
                    navigateToShow(item, composeView)
                }
            }
        }
    }
}