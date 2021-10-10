package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class ShowDetailArg(
    val source: String? = null,
    val showId: Int?,
    val showTitle: String?,
    val showImageUrl: String?,
    val showBackgroundUrl: String?,
    val imdbID: String? = null,
    val isAuthorizedOnTrakt: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(source)
        parcel.writeValue(showId)
        parcel.writeString(showTitle)
        parcel.writeString(showImageUrl)
        parcel.writeString(showBackgroundUrl)
        parcel.writeString(imdbID)
        parcel.writeByte(if (isAuthorizedOnTrakt) 1 else 0)
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