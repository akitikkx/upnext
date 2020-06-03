package com.theupnextapp.network.models.upnext

import com.theupnextapp.database.DatabaseShowInfo
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeResponse

data class NetworkShowInfoCombined constructor(
    val showInfoResponse: NetworkShowInfoResponse,
    val previousEpisode: NetworkShowPreviousEpisodeResponse?,
    val nextEpisode : NetworkShowNextEpisodeResponse?
)

fun NetworkShowInfoCombined.asDatabaseModel(): DatabaseShowInfo {
    return DatabaseShowInfo(
        id = showInfoResponse.id,
        imdbID = showInfoResponse.externals.imdb,
        name = showInfoResponse.name,
        status = showInfoResponse.status,
        summary = showInfoResponse.summary,
        mediumImageUrl = showInfoResponse.image?.medium,
        originalImageUrl = showInfoResponse.image?.original,
        genres = showInfoResponse.genres.joinToString(),
        language = showInfoResponse.language,
        averageRating = showInfoResponse.rating?.average.toString(),
        airDays = showInfoResponse.schedule?.days?.joinToString(),
        time = showInfoResponse.schedule?.time,
        nextEpisodeLinkedId = showInfoResponse._links?.nextepisode?.href?.substring(31)?.let {
            Integer.parseInt(
                it
            )
        },
        previousEpisodeLinkedId = showInfoResponse._links?.nextepisode?.href?.substring(31)?.let {
            Integer.parseInt(
                it
            )
        },
        nextEpisodeId = nextEpisode?.id,
        nextEpisodeUrl = nextEpisode?.url,
        nextEpisodeSummary = nextEpisode?.summary,
        nextEpisodeSeason = nextEpisode?.season.toString(),
        nextEpisodeRuntime = nextEpisode?.runtime.toString(),
        nextEpisodeNumber = nextEpisode?.number.toString(),
        nextEpisodeName = nextEpisode?.name,
        nextEpisodeMediumImageUrl = nextEpisode?.image?.medium,
        nextEpisodeOriginalImageUrl = nextEpisode?.image?.original,
        nextEpisodeAirtime = nextEpisode?.airtime,
        nextEpisodeAirstamp = nextEpisode?.airstamp,
        nextEpisodeAirdate = nextEpisode?.airdate,
        previousEpisodeId = previousEpisode?.id,
        previousEpisodeUrl = previousEpisode?.url,
        previousEpisodeSummary = previousEpisode?.summary,
        previousEpisodeSeason = previousEpisode?.season.toString(),
        previousEpisodeRuntime = previousEpisode?.runtime.toString(),
        previousEpisodeNumber = previousEpisode?.number.toString(),
        previousEpisodeName = previousEpisode?.name,
        previousEpisodeMediumImageUrl = previousEpisode?.image?.medium,
        previousEpisodeOriginalImageUrl = previousEpisode?.image?.original,
        previousEpisodeAirtime = previousEpisode?.airtime,
        previousEpisodeAirstamp = previousEpisode?.airstamp,
        previousEpisodeAirdate = previousEpisode?.airdate
    )
}

fun NetworkShowInfoCombined.asDomainModel(): ShowInfo {
    return ShowInfo(
        id = showInfoResponse.id,
        imdbID = showInfoResponse.externals.imdb,
        name = showInfoResponse.name,
        summary = showInfoResponse.summary,
        status = showInfoResponse.status,
        mediumImageUrl = showInfoResponse.image?.medium,
        originalImageUrl = showInfoResponse.image?.original,
        genres = showInfoResponse.genres.joinToString(),
        language = showInfoResponse.language,
        averageRating = showInfoResponse.rating?.average.toString(),
        airDays = showInfoResponse.schedule?.days?.joinToString(),
        time = showInfoResponse.schedule?.time,
        nextEpisodeLinkedId = showInfoResponse._links?.nextepisode?.href?.substring(31)?.let {
            Integer.parseInt(
                it
            )
        },
        previousEpisodeLinkedId = showInfoResponse._links?.nextepisode?.href?.substring(31)?.let {
            Integer.parseInt(
                it
            )
        },
        nextEpisodeId = nextEpisode?.id,
        nextEpisodeUrl = nextEpisode?.url,
        nextEpisodeSummary = nextEpisode?.summary,
        nextEpisodeSeason = nextEpisode?.season.toString(),
        nextEpisodeRuntime = nextEpisode?.runtime.toString(),
        nextEpisodeNumber = nextEpisode?.number.toString(),
        nextEpisodeName = nextEpisode?.name,
        nextEpisodeMediumImageUrl = nextEpisode?.image?.medium,
        nextEpisodeOriginalImageUrl = nextEpisode?.image?.original,
        nextEpisodeAirtime = nextEpisode?.airtime,
        nextEpisodeAirstamp = nextEpisode?.airstamp,
        nextEpisodeAirdate = nextEpisode?.airdate,
        previousEpisodeId = previousEpisode?.id,
        previousEpisodeUrl = previousEpisode?.url,
        previousEpisodeSummary = previousEpisode?.summary,
        previousEpisodeSeason = previousEpisode?.season.toString(),
        previousEpisodeRuntime = previousEpisode?.runtime.toString(),
        previousEpisodeNumber = previousEpisode?.number.toString(),
        previousEpisodeName = previousEpisode?.name,
        previousEpisodeMediumImageUrl = previousEpisode?.image?.medium,
        previousEpisodeOriginalImageUrl = previousEpisode?.image?.original,
        previousEpisodeAirtime = previousEpisode?.airtime,
        previousEpisodeAirstamp = previousEpisode?.airstamp,
        previousEpisodeAirdate = previousEpisode?.airdate
    )
}