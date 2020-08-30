package com.theupnextapp.domain

data class NewShows(
    var id: Int,
    val url: String?,
    val name: String?,
    val status: String?,
    val airTime: String?,
    val runtime: String?,
    val premiered: String?,
    val trailerUrl: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val createDate: String? = null,
    val updateDate: String?,
    val localImageUrl: String?
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as NewShows

        if (id != other.id) {
            return false
        }

        if (url != other.url) {
            return false
        }

        if (name != other.name) {
            return false
        }

        if (status != other.status) {
            return false
        }

        if (airTime != other.airTime) {
            return false
        }

        if (runtime != other.runtime) {
            return false
        }

        if (premiered != other.premiered) {
            return false
        }

        if (trailerUrl != other.trailerUrl) {
            return false
        }

        if (mediumImageUrl != other.mediumImageUrl) {
            return false
        }

        if (originalImageUrl != other.originalImageUrl) {
            return false
        }

        if (createDate != other.createDate) {
            return false
        }

        if (updateDate != other.updateDate) {
            return false
        }

        if (localImageUrl != other.localImageUrl) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (airTime?.hashCode() ?: 0)
        result = 31 * result + (runtime?.hashCode() ?: 0)
        result = 31 * result + (premiered?.hashCode() ?: 0)
        result = 31 * result + (trailerUrl?.hashCode() ?: 0)
        result = 31 * result + (mediumImageUrl?.hashCode() ?: 0)
        result = 31 * result + (originalImageUrl?.hashCode() ?: 0)
        result = 31 * result + (createDate?.hashCode() ?: 0)
        result = 31 * result + (updateDate?.hashCode() ?: 0)
        result = 31 * result + (localImageUrl?.hashCode() ?: 0)
        return result
    }
}