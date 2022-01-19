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
    val previousEpisodeLinkedId: Int?
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
        parcel.readValue(Int::class.java.classLoader) as? Int
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