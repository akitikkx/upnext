package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.ShowInfo

@Entity(tableName = "shows_info")
data class DatabaseShowInfo constructor(
    @PrimaryKey
    var id: Int,
    val name : String?,
    val summary : String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val genres: String?,
    val language: String?,
    val averageRating: String?,
    val airDays: String?,
    val time: String?,
    val status : String?,
    val nextEpisodeLinkedId : Int?,
    val previousEpisodeLinkedId : Int?,
    val nextEpisodeId: Int?,
    val nextEpisodeAirdate: String?,
    val nextEpisodeAirstamp: String?,
    val nextEpisodeAirtime: String?,
    val nextEpisodeMediumImageUrl: String?,
    val nextEpisodeOriginalImageUrl: String?,
    val nextEpisodeName: String?,
    val nextEpisodeNumber: String?,
    val nextEpisodeRuntime: String?,
    val nextEpisodeSeason: String?,
    val nextEpisodeSummary: String?,
    val nextEpisodeUrl: String?,
    val previousEpisodeId: Int?,
    val previousEpisodeAirdate: String?,
    val previousEpisodeAirstamp: String?,
    val previousEpisodeAirtime: String?,
    val previousEpisodeMediumImageUrl: String?,
    val previousEpisodeOriginalImageUrl: String?,
    val previousEpisodeName: String?,
    val previousEpisodeNumber: String?,
    val previousEpisodeRuntime: String?,
    val previousEpisodeSeason: String?,
    val previousEpisodeSummary: String?,
    val previousEpisodeUrl: String?
)

fun DatabaseShowInfo.asDomainModel(): ShowInfo {
    return ShowInfo(
        id = id,
        name = name,
        summary = summary,
        mediumImageUrl = mediumImageUrl,
        originalImageUrl = originalImageUrl,
        genres = genres.toString(),
        language = language,
        averageRating = averageRating,
        airDays = airDays.toString(),
        time = time,
        status = status,
        nextEpisodeLinkedId = nextEpisodeLinkedId,
        previousEpisodeLinkedId = previousEpisodeLinkedId,
        nextEpisodeId = nextEpisodeId,
        nextEpisodeAirdate = nextEpisodeAirdate,
        nextEpisodeAirstamp = nextEpisodeAirstamp,
        nextEpisodeAirtime = nextEpisodeAirtime,
        nextEpisodeMediumImageUrl = nextEpisodeMediumImageUrl,
        nextEpisodeOriginalImageUrl = nextEpisodeOriginalImageUrl,
        nextEpisodeName = nextEpisodeName,
        nextEpisodeNumber = nextEpisodeNumber,
        nextEpisodeRuntime = nextEpisodeRuntime,
        nextEpisodeSeason = nextEpisodeSeason,
        nextEpisodeSummary = nextEpisodeSummary,
        nextEpisodeUrl = nextEpisodeUrl,
        previousEpisodeId = previousEpisodeId,
        previousEpisodeAirdate = previousEpisodeAirdate,
        previousEpisodeAirstamp = previousEpisodeAirstamp,
        previousEpisodeAirtime = previousEpisodeAirtime,
        previousEpisodeMediumImageUrl = previousEpisodeMediumImageUrl,
        previousEpisodeOriginalImageUrl = previousEpisodeOriginalImageUrl,
        previousEpisodeName = previousEpisodeName,
        previousEpisodeNumber = previousEpisodeNumber,
        previousEpisodeRuntime = previousEpisodeRuntime,
        previousEpisodeSeason = previousEpisodeSeason,
        previousEpisodeSummary = previousEpisodeSummary,
        previousEpisodeUrl = previousEpisodeUrl
    )

}