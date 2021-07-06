package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class ShowSeasonEpisodesArg(
    val showId: Int?,
    val seasonNumber: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(showId)
        parcel.writeValue(seasonNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowSeasonEpisodesArg> {
        override fun createFromParcel(parcel: Parcel): ShowSeasonEpisodesArg {
            return ShowSeasonEpisodesArg(parcel)
        }

        override fun newArray(size: Int): Array<ShowSeasonEpisodesArg?> {
            return arrayOfNulls(size)
        }
    }
}