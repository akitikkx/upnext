package com.theupnextapp.network

import com.theupnextapp.database.DatabaseTodaySchedule
import com.theupnextapp.database.DatabaseTomorrowSchedule
import com.theupnextapp.database.DatabaseYesterdaySchedule
import com.theupnextapp.network.models.tvmaze.NetworkTodayScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTomorrowScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse

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