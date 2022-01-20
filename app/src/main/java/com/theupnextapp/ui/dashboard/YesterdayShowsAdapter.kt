package com.theupnextapp.ui.dashboard

import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.components.ListPosterCard


class YesterdayShowsAdapter : RecyclerView.Adapter<YesterdayShowsAdapter.ComposeViewHolder>() {

    private var yesterdayShows: List<ScheduleShow> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        return ComposeViewHolder(ComposeView(parent.context))
    }

    override fun getItemCount(): Int = yesterdayShows.size

    fun submitList(yesterdayShowsList: List<ScheduleShow>) {
        // old list is equal to the current list
        val oldList = yesterdayShows
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            YesterdayShowItemDiffCallback(
                oldList,
                yesterdayShowsList
            )
        )
        yesterdayShows = yesterdayShowsList
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

    override fun onViewRecycled(holder: ComposeViewHolder) {
        // Dispose the underlying Composition of the ComposeView
        // when RecyclerView has recycled this ViewHolder
        holder.composeView.disposeComposition()
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onBindViewHolder(holder: ComposeViewHolder, position: Int) {
        val item = yesterdayShows[position]
        holder.bind(item)
    }

    class ComposeViewHolder(val composeView: ComposeView) : RecyclerView.ViewHolder(composeView) {
        init {
            // necessary to make the Compose view holder work in all scenarios
            // https://developer.android.com/jetpack/compose/interop/compose-in-existing-ui#compose-recyclerview
            composeView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
        }

        @ExperimentalMaterialApi
        fun bind(item: ScheduleShow) {
            composeView.setContent {
                MdcTheme {
                    ListPosterCard(item) {
                        navigateToShow(item, composeView)
                    }
                }
            }
        }

        private fun navigateToShow(item: ScheduleShow, view: View) {
            val direction = DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(
                ShowDetailArg(
                    source = "yesterday",
                    showId = item.id,
                    showTitle = item.name,
                    showImageUrl = item.originalImage,
                    showBackgroundUrl = item.mediumImage
                )
            )

            view.findNavController().navigate(direction)
        }
    }
}