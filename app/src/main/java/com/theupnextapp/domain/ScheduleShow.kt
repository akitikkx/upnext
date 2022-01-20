package com.theupnextapp.domain

import androidx.recyclerview.widget.DiffUtil

data class ScheduleShow(
    val id: Int,
    val originalImage: String?,
    val mediumImage: String?,
    val language: String?,
    val name: String?,
    val officialSite: String?,
    val premiered: String?,
    val runtime: String?,
    val status: String?,
    val summary: String?,
    val type: String?,
    val updated: String?,
    val url: String?
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as ScheduleShow

        if (id != other.id) {
            return false
        }

        if (originalImage != other.originalImage) {
            return false
        }

        if (mediumImage != other.mediumImage) {
            return false
        }

        if (language != other.language) {
            return false
        }

        if (name != other.name) {
            return false
        }

        if (officialSite != other.officialSite) {
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

        if (url != other.url) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (originalImage?.hashCode() ?: 0)
        result = 31 * result + (mediumImage?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (officialSite?.hashCode() ?: 0)
        result = 31 * result + (premiered?.hashCode() ?: 0)
        result = 31 * result + (runtime?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (summary?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (updated?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }
}

class ScheduleShowItemDiffCallback(
    private val oldList: List<ScheduleShow>,
    private val newList: List<ScheduleShow>
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