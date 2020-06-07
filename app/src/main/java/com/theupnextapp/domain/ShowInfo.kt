package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class ShowInfo(
    val airDays: String?,
    val averageRating: String?,
    var id: Int,
    val imdbID: String?,
    val genres: String?,
    val language: String?,
    val mediumImageUrl: String?,
    val name: String?,
    val originalImageUrl: String?,
    val summary: String?,
    val time: String?,
    val status: String?,
    val nextEpisodeLinkedId: Int?,
    val previousEpisodeLinkedId: Int?,
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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(airDays)
        parcel.writeString(averageRating)
        parcel.writeInt(id)
        parcel.writeString(imdbID)
        parcel.writeString(genres)
        parcel.writeString(language)
        parcel.writeString(mediumImageUrl)
        parcel.writeString(name)
        parcel.writeString(originalImageUrl)
        parcel.writeString(summary)
        parcel.writeString(time)
        parcel.writeString(status)
        parcel.writeValue(nextEpisodeLinkedId)
        parcel.writeValue(previousEpisodeLinkedId)
        parcel.writeValue(nextEpisodeId)
        parcel.writeString(nextEpisodeAirdate)
        parcel.writeString(nextEpisodeAirstamp)
        parcel.writeString(nextEpisodeAirtime)
        parcel.writeString(nextEpisodeMediumImageUrl)
        parcel.writeString(nextEpisodeOriginalImageUrl)
        parcel.writeString(nextEpisodeName)
        parcel.writeString(nextEpisodeNumber)
        parcel.writeString(nextEpisodeRuntime)
        parcel.writeString(nextEpisodeSeason)
        parcel.writeString(nextEpisodeSummary)
        parcel.writeString(nextEpisodeUrl)
        parcel.writeValue(previousEpisodeId)
        parcel.writeString(previousEpisodeAirdate)
        parcel.writeString(previousEpisodeAirstamp)
        parcel.writeString(previousEpisodeAirtime)
        parcel.writeString(previousEpisodeMediumImageUrl)
        parcel.writeString(previousEpisodeOriginalImageUrl)
        parcel.writeString(previousEpisodeName)
        parcel.writeString(previousEpisodeNumber)
        parcel.writeString(previousEpisodeRuntime)
        parcel.writeString(previousEpisodeSeason)
        parcel.writeString(previousEpisodeSummary)
        parcel.writeString(previousEpisodeUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowInfo> {
        override fun createFromParcel(parcel: Parcel): ShowInfo {
            return ShowInfo(parcel)
        }

        override fun newArray(size: Int): Array<ShowInfo?> {
            return arrayOfNulls(size)
        }
    }
}