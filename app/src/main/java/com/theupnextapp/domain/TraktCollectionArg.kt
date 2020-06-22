package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class TraktCollectionArg(
    val imdbID: String?,
    val title: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val lastCollectedAt: String?,
    val lastUpdatedAt: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imdbID)
        parcel.writeString(title)
        parcel.writeString(mediumImageUrl)
        parcel.writeString(originalImageUrl)
        parcel.writeString(lastCollectedAt)
        parcel.writeString(lastUpdatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktCollectionArg> {
        override fun createFromParcel(parcel: Parcel): TraktCollectionArg {
            return TraktCollectionArg(parcel)
        }

        override fun newArray(size: Int): Array<TraktCollectionArg?> {
            return arrayOfNulls(size)
        }
    }
}