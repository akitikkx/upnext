package com.theupnextapp.network

import com.theupnextapp.database.*

fun NetworkRecommendedShowsResponse.asDatabaseModel(): Array<DatabaseRecommendedShows> {
    return data.map {
        DatabaseRecommendedShows(
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

fun YesterdayNetworkSchedule.asDatabaseModel(): DatabaseYesterdaySchedule {
    return DatabaseYesterdaySchedule(
        id = show.id.toString(),
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

fun TodayNetworkSchedule.asDatabaseModel(): DatabaseTodaySchedule {
    return DatabaseTodaySchedule(
        id = show.id.toString(),
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

fun TomorrowNetworkSchedule.asDatabaseModel(): DatabaseTomorrowSchedule {
    return DatabaseTomorrowSchedule(
        id = show.id.toString(),
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