package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class ShowDetailArg(
    val showId: Int?,
    val showTitle: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(showId)
        parcel.writeString(showTitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowDetailArg> {
        override fun createFromParcel(parcel: Parcel): ShowDetailArg {
            return ShowDetailArg(parcel)
        }

        override fun newArray(size: Int): Array<ShowDetailArg?> {
            return arrayOfNulls(size)
        }
    }

}