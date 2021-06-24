package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.ScheduleShow

@Entity(tableName = "schedule_tomorrow")
data class DatabaseTomorrowSchedule(
    @PrimaryKey
    val id: Int,
    val image: String?,
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
)

fun List<DatabaseTomorrowSchedule>.asDomainModel(): List<ScheduleShow> {
    return map {
        ScheduleShow(
            id = it.id,
            originalImage = it.image,
            mediumImage = it.mediumImage,
            language = it.language,
            name = it.name,
            officialSite = it.officialSite,
            premiered = it.premiered,
            runtime = it.runtime,
            status = it.status,
            summary = it.summary,
            type = it.type,
            updated = it.updated,
            url = it.url
        )
    }
}