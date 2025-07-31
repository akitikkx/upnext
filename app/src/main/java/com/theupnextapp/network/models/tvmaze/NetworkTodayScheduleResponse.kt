package com.theupnextapp.network.models.tvmaze

import com.theupnextapp.database.DatabaseTodaySchedule

data class NetworkTodayScheduleResponse(
    val _links: NetworkShowEpisodeLinks,
    val airdate: String,
    val airstamp: String,
    val airtime: String,
    val id: Int, // Episode ID
    val image: NetworkScheduleImage?, // Changed from Any to NetworkScheduleImage?
    val name: String,
    val number: Int,
    val runtime: Int,
    val season: Int,
    val show: NetworkScheduleShow, // Contains the actual show details including its ID
    val summary: String,
    val url: String,
    val imdbId: String? // This field was present, keeping it.
)

// Extension function to map to Database model
fun NetworkTodayScheduleResponse.asDatabaseModel(): DatabaseTodaySchedule {
    return DatabaseTodaySchedule(
        id = this.id, // Episode ID
        showId = this.show.id, // Show ID from the nested 'show' object
        image = this.show.image?.original,
        mediumImage = this.show.image?.medium,
        language = this.show.language,
        name = this.name, // Episode name
        officialSite = this.show.officialSite,
        premiered = this.show.premiered,
        runtime = this.show.runtime.toString(),
        status = this.show.status,
        summary = this.summary, // Episode summary
        type = this.show.type,
        updated = this.show.updated.toString(),
        url = this.url // Episode URL
    )
}
