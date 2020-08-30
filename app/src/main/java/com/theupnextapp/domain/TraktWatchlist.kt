package com.theupnextapp.domain

data class TraktWatchlist(
    val id: Int?,
    val listed_at: String?,
    val rank: Int?,
    val title: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvrageID: Int?,
    val tvMazeID: Int?
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as TraktWatchlist

        if (id != other.id) {
            return false
        }

        if (listed_at != other.listed_at) {
            return false
        }

        if (rank != other.rank) {
            return false
        }

        if (title != other.title) {
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

        if (tvrageID != other.tvrageID) {
            return false
        }

        if (tvMazeID != other.tvMazeID) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (listed_at?.hashCode() ?: 0)
        result = 31 * result + (rank ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (mediumImageUrl?.hashCode() ?: 0)
        result = 31 * result + (originalImageUrl?.hashCode() ?: 0)
        result = 31 * result + (imdbID?.hashCode() ?: 0)
        result = 31 * result + (slug?.hashCode() ?: 0)
        result = 31 * result + (tmdbID ?: 0)
        result = 31 * result + (traktID ?: 0)
        result = 31 * result + (tvdbID ?: 0)
        result = 31 * result + (tvrageID ?: 0)
        result = 31 * result + (tvMazeID ?: 0)
        return result
    }
}