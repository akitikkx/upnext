package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.RecommendedShows

@Entity(tableName = "recommended_shows")
data class DatabaseRecommendedShows constructor(
    @PrimaryKey
    var id: Int,
    val url: String?,
    val name: String?,
    val status: String?,
    val air_time: String?,
    val runtime: String?,
    val premiered: String?,
    val trailer_url: String?,
    val medium_image_url: String?,
    val original_image_url: String?,
    val create_date: String?,
    val update_date: String?,
    val local_image_url: String?
)

fun List<DatabaseRecommendedShows>.asDomainModel(): List<RecommendedShows> {
    return map {
        RecommendedShows(
            id = it.id,
            url = it.url,
            name = it.name,
            status = it.status,
            airTime = it.air_time,
            runtime = it.runtime,
            premiered = it.premiered,
            trailerUrl = it.trailer_url,
            mediumImageUrl = it.original_image_url,
            originalImageUrl = it.original_image_url,
            createDate = it.create_date,
            updateDate = it.update_date,
            localImageUrl = it.local_image_url
        )
    }
}