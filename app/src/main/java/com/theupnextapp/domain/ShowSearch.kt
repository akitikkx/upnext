package com.theupnextapp.domain

import androidx.recyclerview.widget.DiffUtil

data class ShowSearch(
    val genres: String?,
    val id: Int,
    val mediumImageUrl: String?,
    val name: String?,
    val originalImageUrl: String?,
    val language: String?,
    val premiered: String?,
    val runtime: String?,
    val status: String?,
    val summary: String?,
    val type: String?,
    val updated: String?
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as ShowSearch

        if (id != other.id) {
            return false
        }

        if (genres != other.genres) {
            return false
        }

        if (mediumImageUrl != other.mediumImageUrl) {
            return false
        }

        if (originalImageUrl != other.originalImageUrl) {
            return false
        }

        if (language != other.language) {
            return false
        }

        if (premiered != other.premiered) {
            return false
        }

        if (runtime != other.runtime) {
            return false
        }

        if (status != other.status) {
            return false
        }

        if (summary != other.summary) {
            return false
        }

        if (type != other.type) {
            return false
        }

        if (updated != other.updated) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = genres?.hashCode() ?: 0
        result = 31 * result + id
        result = 31 * result + (mediumImageUrl?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (originalImageUrl?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (premiered?.hashCode() ?: 0)
        result = 31 * result + (runtime?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (summary?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (updated?.hashCode() ?: 0)
        return result
    }
}

class SearchItemDiffCallback(
    private val oldList: List<ShowSearch>,
    private val newList: List<ShowSearch>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].equals(newList[newItemPosition])
    }

}