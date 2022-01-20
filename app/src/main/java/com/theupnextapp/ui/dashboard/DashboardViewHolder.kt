package com.theupnextapp.ui.dashboard

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.ui.explore.ExploreFragmentDirections

abstract class DashboardViewHolder<T>(val composeView: ComposeView) :
    RecyclerView.ViewHolder(composeView) {

    init {
        // necessary to make the Compose view holder work in all scenarios
        // https://developer.android.com/jetpack/compose/interop/compose-in-existing-ui#compose-recyclerview
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    abstract val source: String

    @Composable
    abstract fun ComposableContainer(item: T)

    fun bind(item: T) {
        composeView.setContent {
            ComposableContainer(item)
        }
    }

    fun navigateFromDashboardToShowDetail(item: ScheduleShow, view: View) {
        val direction = DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(
            ShowDetailArg(
                source = source,
                showId = item.id,
                showTitle = item.name,
                showImageUrl = item.originalImage,
                showBackgroundUrl = item.mediumImage
            )
        )

        view.findNavController().navigate(direction)
    }

    fun navigateFromTrendingToShowDetail(item: TraktTrendingShows, view: View) {
        val directions = ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
            ShowDetailArg(
                source = source,
                showId = item.tvMazeID,
                showTitle = item.title,
                showImageUrl = item.originalImageUrl,
                showBackgroundUrl = item.mediumImageUrl
            )
        )
        view.findNavController().navigate(directions)
    }

    fun navigateFromPopularToShowDetail(item: TraktPopularShows, view: View) {
        val directions = ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
            ShowDetailArg(
                source = source,
                showId = item.tvMazeID,
                showTitle = item.title,
                showImageUrl = item.originalImageUrl,
                showBackgroundUrl = item.mediumImageUrl
            )
        )
        view.findNavController().navigate(directions)
    }

    fun navigateFromAnticipatedToShowDetail(item: TraktMostAnticipated, view: View) {
        val directions = ExploreFragmentDirections.actionExploreFragmentToShowDetailFragment(
            ShowDetailArg(
                source = source,
                showId = item.tvMazeID,
                showTitle = item.title,
                showImageUrl = item.originalImageUrl,
                showBackgroundUrl = item.mediumImageUrl
            )
        )
        view.findNavController().navigate(directions)
    }
}