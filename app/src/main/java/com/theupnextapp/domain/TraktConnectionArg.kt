package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class TraktConnectionArg(
    val code: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktConnectionArg> {
        override fun createFromParcel(parcel: Parcel): TraktConnectionArg {
            return TraktConnectionArg(parcel)
        }

        override fun newArray(size: Int): Array<TraktConnectionArg?> {
            return arrayOfNulls(size)
        }
    }
}