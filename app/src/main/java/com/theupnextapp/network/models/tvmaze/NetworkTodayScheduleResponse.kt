package com.theupnextapp.network.models.tvmaze

import com.theupnextapp.database.DatabaseTodaySchedule

data class NetworkTodayScheduleResponse(
    val _links: NetworkShowEpisodeLinks,
    val airdate: String,
    val airstamp: String,
    val airtime: String,
    val id: Int, 
    val image: NetworkScheduleImage?, 
    val name: String,
    val number: Int,
    val runtime: Int,
    val season: Int,
    val show: NetworkScheduleShow, 
    val summary: String,
    val url: String,
    val imdbId: String?, 
)

// Extension function to map to Database model
fun NetworkTodayScheduleResponse.asDatabaseModel(): DatabaseTodaySchedule {
    return DatabaseTodaySchedule(
        id = this.id, 
        showId = this.show.id, 
        image = this.show.image?.original,
        mediumImage = this.show.image?.medium,
        language = this.show.language,
        name = this.name, 
        officialSite = this.show.officialSite,
        premiered = this.show.premiered,
        runtime = this.show.runtime.toString(),
        status = this.show.status,
        summary = this.summary, 
        type = this.show.type,
        updated = this.show.updated.toString(),
        url = this.url, 
    )
}
