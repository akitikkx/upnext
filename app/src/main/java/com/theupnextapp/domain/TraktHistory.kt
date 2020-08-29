package com.theupnextapp.domain

data class TraktHistory(
    val id: Int?,
    val showTitle: String?,
    val showYear: Int?,
    val episodeTitle: String?,
    val episodeSeasonNumber: Int?,
    val episodeNumber : Int?,
    val historyType: String?,
    val watchedAt: String?,
    val historyAction: String?,
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

        other as TraktHistory

        if (id != other.id) {
            return false
        }

        if (showTitle != other.showTitle) {
            return false
        }

        if (showYear != other.showYear) {
            return false
        }

        if (episodeTitle != other.episodeTitle) {
            return false
        }

        if (episodeSeasonNumber != other.episodeSeasonNumber) {
            return false
        }

        if (episodeNumber != other.episodeNumber) {
            return false
        }

        if (historyType != other.historyType) {
            return false
        }

        if (watchedAt != other.watchedAt) {
            return false
        }

        if (historyAction != other.historyAction) {
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
        var result = id ?: 0
        result = 31 * result + (showTitle?.hashCode() ?: 0)
        result = 31 * result + (showYear ?: 0)
        result = 31 * result + (episodeTitle?.hashCode() ?: 0)
        result = 31 * result + (episodeSeasonNumber ?: 0)
        result = 31 * result + (episodeNumber ?: 0)
        result = 31 * result + (historyType?.hashCode() ?: 0)
        result = 31 * result + (watchedAt?.hashCode() ?: 0)
        result = 31 * result + (historyAction?.hashCode() ?: 0)
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