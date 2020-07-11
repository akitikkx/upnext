package com.theupnextapp.network

import com.theupnextapp.database.*
import com.theupnextapp.network.models.tvmaze.NetworkTodayScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTomorrowScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse
import com.theupnextapp.network.models.upnext.NetworkNewShowsResponse

fun NetworkNewShowsResponse.asDatabaseModel(): Array<DatabaseNewShows> {
    return data.map {
        DatabaseNewShows(
            id = it.id,
            url = it.url,
            name = it.name,
            status = it.status,
            air_time = it.air_time,
            runtime = it.runtime,
            premiered = it.premiered,
            trailer_url = it.trailer_url,
            medium_image_url = it.medium_image_url,
            original_image_url = it.original_image_url,
            create_date = it.create_date,
            update_date = it.update_date,
            local_image_url = it.local_image_url
        )
    }.toTypedArray()
}

fun NetworkYesterdayScheduleResponse.asDatabaseModel(): DatabaseYesterdaySchedule {
    return DatabaseYesterdaySchedule(
        id = show.id,
        image = show.image?.original,
        language = show.language,
        name = show.name,
        officialSite = show.officialSite,
        premiered = show.premiered,
        runtime = show.runtime.toString(),
        status = show.status,
        summary = show.summary,
        type = show.type,
        updated = show.updated.toString(),
        url = show.url
    )
}

fun NetworkTodayScheduleResponse.asDatabaseModel(): DatabaseTodaySchedule {
    return DatabaseTodaySchedule(
        id = show.id,
        image = show.image?.original,
        language = show.language,
        name = show.name,
        officialSite = show.officialSite,
        premiered = show.premiered,
        runtime = show.runtime.toString(),
        status = show.status,
        summary = show.summary,
        type = show.type,
        updated = show.updated.toString(),
        url = show.url
    )
}

fun NetworkTomorrowScheduleResponse.asDatabaseModel(): DatabaseTomorrowSchedule {
    return DatabaseTomorrowSchedule(
        id = show.id,
        image = show.image?.original,
        language = show.language,
        name = show.name,
        officialSite = show.officialSite,
        premiered = show.premiered,
        runtime = show.runtime.toString(),
        status = show.status,
        summary = show.summary,
        type = show.type,
        updated = show.updated.toString(),
        url = show.url
    )
}