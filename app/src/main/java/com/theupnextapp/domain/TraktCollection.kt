package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class TraktCollection(
    var id: Long,
    val title: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val year: Int?,
    val slug: String?,
    val imdbID: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvrageID: Int?,
    val tvMazeID: Int?,
    val lastCollectedAt: String?,
    val lastUpdatedAt: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(mediumImageUrl)
        parcel.writeString(originalImageUrl)
        parcel.writeValue(year)
        parcel.writeString(slug)
        parcel.writeString(imdbID)
        parcel.writeValue(tmdbID)
        parcel.writeValue(traktID)
        parcel.writeValue(tvdbID)
        parcel.writeValue(tvrageID)
        parcel.writeValue(tvMazeID)
        parcel.writeString(lastCollectedAt)
        parcel.writeString(lastUpdatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktCollection> {
        override fun createFromParcel(parcel: Parcel): TraktCollection {
            return TraktCollection(parcel)
        }

        override fun newArray(size: Int): Array<TraktCollection?> {
            return arrayOfNulls(size)
        }
    }

}