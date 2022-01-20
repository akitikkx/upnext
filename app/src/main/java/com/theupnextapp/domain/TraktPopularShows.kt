package com.theupnextapp.domain

import androidx.recyclerview.widget.DiffUtil

data class TraktPopularShows(
    val id: Int?,
    val title: String?,
    val year: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as TraktPopularShows

        if (id != other.id) {
            return false
        }

        if (title != other.title) {
            return false
        }

        if (year != other.year) {
            return false
        }

        if (mediumImageUrl != other.mediumImageUrl) {
            return false
        }

        if (originalImageUrl != other.originalImageUrl) {
            return false
        }

        if (imdbID != other.imdbID) {
            return false
        }

        if (slug != other.slug) {
            return false
        }

        if (tmdbID != other.tmdbID) {
            return false
        }

        if (traktID != other.traktID) {
            return false
        }

        if (tvdbID != other.tvdbID) {
            return false
        }

        if (tvMazeID != other.tvMazeID) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (year?.hashCode() ?: 0)
        result = 31 * result + (mediumImageUrl?.hashCode() ?: 0)
        result = 31 * result + (originalImageUrl?.hashCode() ?: 0)
        result = 31 * result + (imdbID?.hashCode() ?: 0)
        result = 31 * result + (slug?.hashCode() ?: 0)
        result = 31 * result + (tmdbID ?: 0)
        result = 31 * result + (traktID ?: 0)
        result = 31 * result + (tvdbID ?: 0)
        result = 31 * result + (tvMazeID ?: 0)
        return result
    }
}

class PopularShowItemDiffCallback(
    private val oldPopularShowsList: List<TraktPopularShows>,
    private val newPopularShowsList: List<TraktPopularShows>
): DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldPopularShowsList[oldItemPosition].imdbID == newPopularShowsList[newItemPosition].imdbID
    }

    override fun getOldListSize(): Int = oldPopularShowsList.size

    override fun getNewListSize(): Int  = newPopularShowsList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldPopularShowsList[oldItemPosition].equals(newPopularShowsList[newItemPosition])
    }
}