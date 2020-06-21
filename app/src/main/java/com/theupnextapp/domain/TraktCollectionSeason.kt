package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class TraktCollectionSeason(
    var id: Long,
    var imdbID: String?,
    val seasonNumber: Int?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(imdbID)
        parcel.writeValue(seasonNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktCollectionSeason> {
        override fun createFromParcel(parcel: Parcel): TraktCollectionSeason {
            return TraktCollectionSeason(parcel)
        }

        override fun newArray(size: Int): Array<TraktCollectionSeason?> {
            return arrayOfNulls(size)
        }
    }
}