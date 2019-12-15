package com.theupnextapp.network

import com.theupnextapp.database.DatabaseNewShows
import com.theupnextapp.database.DatabaseRecommendedShows

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