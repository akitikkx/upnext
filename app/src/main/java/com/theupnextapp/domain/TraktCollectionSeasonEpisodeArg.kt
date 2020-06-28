package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class TraktCollectionSeasonEpisodeArg(
    val collection: TraktCollectionArg?,
    val collectionSeason: TraktCollectionSeason?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(TraktCollectionArg::class.java.classLoader),
        parcel.readParcelable(TraktCollectionSeason::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(collection, flags)
        parcel.writeParcelable(collectionSeason, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktCollectionSeasonEpisodeArg> {
        override fun createFromParcel(parcel: Parcel): TraktCollectionSeasonEpisodeArg {
            return TraktCollectionSeasonEpisodeArg(parcel)
        }

        override fun newArray(size: Int): Array<TraktCollectionSeasonEpisodeArg?> {
            return arrayOfNulls(size)
        }
    }

}