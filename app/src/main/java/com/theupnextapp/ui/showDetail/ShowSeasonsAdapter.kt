package com.theupnextapp.ui.showDetail

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.ShowSeasonItemBinding
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.domain.TraktWatchedShowProgressSeason

class ShowSeasonsAdapter(val listener: ShowSeasonsAdapterListener) :
    RecyclerView.Adapter<ShowSeasonsAdapter.ViewHolder>() {

    private var showSeasons: List<ShowSeason> = ArrayList()

    private var traktWatchedProgress: List<TraktWatchedShowProgressSeason> = ArrayList()

    private var traktCollectionSeasons: List<TraktCollectionSeason> = ArrayList()

    var isAuthorizedOnTrakt: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: ShowSeasonItemBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                ViewHolder.LAYOUT,
                parent,
                false
            )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = showSeasons.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            val showSeason = showSeasons[position]

            var watchedSeason: TraktWatchedShowProgressSeason? = null

            var collectedSeason: TraktCollectionSeason? = null

            it.listener = listener

            it.showSeason = showSeason

            if (!traktWatchedProgress.isNullOrEmpty()) {
                traktWatchedProgress?.forEach { season ->
                    if (season.number == showSeason.seasonNumber && season.aired == season.completed) {
                        watchedSeason = season
                    }
                }
            }

            if (!traktCollectionSeasons.isNullOrEmpty()) {
                traktCollectionSeasons?.forEach { season ->
                    if (season.seasonNumber == showSeason.seasonNumber) {
                        collectedSeason = season
                    }
                }
            }

            it.watchedSeason = watchedSeason

            it.collectedSeason = collectedSeason

            if (isAuthorizedOnTrakt) {
                it.seasonOptionsMenu.visibility = View.VISIBLE

                if (watchedSeason != null) {
                    it.showSeasonTraktWatchedTag.visibility = View.VISIBLE
                } else {
                    it.showSeasonTraktWatchedTag.visibility = View.GONE
                }

                if (collectedSeason != null) {
                    it.showSeasonTraktCollectedTag.visibility = View.VISIBLE
                } else {
                    it.showSeasonTraktCollectedTag.visibility = View.GONE
                }
            } else {
                it.seasonOptionsMenu.visibility = View.GONE
            }
        }
    }

    interface ShowSeasonsAdapterListener {

        fun onShowSeasonAddToTraktHistoryClick(view: View, showSeason: ShowSeason)

        fun onShowSeasonRemoveFromTraktHistoryClick(view: View, showSeason: ShowSeason)

        fun onShowSeasonTraktOptionsMenuClick(
            view: View,
            showSeason: ShowSeason,
            watchedSeason: TraktWatchedShowProgressSeason?,
            collectedSeason: TraktCollectionSeason?
        ) {
            val seasonWatched = watchedSeason != null

            val popupMenu = PopupMenu(view.context, view)
            if (seasonWatched) {
                popupMenu.inflate(R.menu.show_season_watched_menu)
                popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
                        when (menuItem?.itemId) {
                            R.id.menu_remove_from_history -> {
                                onShowSeasonRemoveFromTraktHistoryClick(view, showSeason)
                                return true
                            }
                            else -> return false
                        }
                    }
                })
            } else {
                popupMenu.inflate(R.menu.show_season_not_watched_menu)
                popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
                        when (menuItem?.itemId) {
                            R.id.menu_add_to_history -> {
                                onShowSeasonAddToTraktHistoryClick(view, showSeason)
                                return true
                            }
                            else -> return false
                        }
                    }
                })
            }
            popupMenu.show()
        }

        fun onShowSeasonAddToTraktCollectionClick(view: View, showSeason: ShowSeason)

        fun onShowSeasonRemoveFromTraktCollectionClick(view: View, showSeason: ShowSeason)
    }

    fun submitShowSeasonsList(showSeasonsList: List<ShowSeason>) {
        val oldShowSeasonsList = showSeasons
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            ShowSeasonItemDiffCallback(
                oldShowSeasonsList,
                showSeasonsList
            )
        )
        showSeasons = showSeasonsList
        diffResult.dispatchUpdatesTo(this)
    }

    fun submitCollectionSeasonsList(collectionSeasonsList: List<TraktCollectionSeason>) {
        val oldCollectionSeasonsList = traktCollectionSeasons
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            CollectionSeasonItemCallback(
                oldCollectionSeasonsList,
                collectionSeasonsList
            )
        )
        traktCollectionSeasons = collectionSeasonsList
        diffResult.dispatchUpdatesTo(this)
    }

    fun submitWatchedProgressSeasonsList(watchedProgressSeasonsList: List<TraktWatchedShowProgressSeason>) {
        val oldWatchedProgressSeasonsList = traktWatchedProgress
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            WatchedProgressSeasonItemCallback(
                oldWatchedProgressSeasonsList,
                watchedProgressSeasonsList
            )
        )
        traktWatchedProgress = watchedProgressSeasonsList
        diffResult.dispatchUpdatesTo(this)
    }

    class WatchedProgressSeasonItemCallback(
        private val oldWatchedProgressSeasonsList: List<TraktWatchedShowProgressSeason>,
        private val newWatchedProgressSeasonsList: List<TraktWatchedShowProgressSeason>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldWatchedProgressSeasonsList[oldItemPosition].number == newWatchedProgressSeasonsList[newItemPosition].number
        }

        override fun getOldListSize(): Int = oldWatchedProgressSeasonsList.size

        override fun getNewListSize(): Int = newWatchedProgressSeasonsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldWatchedProgressSeasonsList[oldItemPosition].equals(
                newWatchedProgressSeasonsList?.get(newItemPosition)
            )
        }

    }

    class CollectionSeasonItemCallback(
        private val oldCollectionSeasonsList: List<TraktCollectionSeason>,
        private val newCollectionSeasonsList: List<TraktCollectionSeason>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldCollectionSeasonsList[oldItemPosition].id == newCollectionSeasonsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldCollectionSeasonsList.size

        override fun getNewListSize(): Int = newCollectionSeasonsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldCollectionSeasonsList[oldItemPosition].equals(newCollectionSeasonsList[newItemPosition])
        }

    }

    class ShowSeasonItemDiffCallback(
        private val oldShowSeasonsList: List<ShowSeason>,
        private val newShowSeasonsList: List<ShowSeason>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldShowSeasonsList[oldItemPosition].id == newShowSeasonsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldShowSeasonsList.size

        override fun getNewListSize(): Int = newShowSeasonsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldShowSeasonsList[oldItemPosition].equals(newShowSeasonsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: ShowSeasonItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {

        companion object {
            @LayoutRes
            val LAYOUT = R.layout.show_season_item
        }

    }
}