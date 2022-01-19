package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class ShowDetailSummary(
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
    val previousEpisodeHref: String?,
    val nextEpisodeHref: String?,
    val nextEpisodeLinkedId: Int?,
    val previousEpisodeLinkedId: Int?,
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
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

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
        parcel.writeString(previousEpisodeHref)
        parcel.writeString(nextEpisodeHref)
        parcel.writeValue(nextEpisodeLinkedId)
        parcel.writeValue(previousEpisodeLinkedId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowDetailSummary> {
        override fun createFromParcel(parcel: Parcel): ShowDetailSummary {
            return ShowDetailSummary(parcel)
        }

        override fun newArray(size: Int): Array<ShowDetailSummary?> {
            return arrayOfNulls(size)
        }
    }

}
