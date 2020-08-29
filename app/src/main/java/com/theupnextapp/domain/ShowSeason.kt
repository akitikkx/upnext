package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class ShowSeason(
    val id: Int?,
    val name: String?,
    val seasonNumber: Int?,
    val episodeCount: Int?,
    val premiereDate: String?,
    val endDate: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeValue(seasonNumber)
        parcel.writeValue(episodeCount)
        parcel.writeString(premiereDate)
        parcel.writeString(endDate)
        parcel.writeString(mediumImageUrl)
        parcel.writeString(originalImageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowSeason> {
        override fun createFromParcel(parcel: Parcel): ShowSeason {
            return ShowSeason(parcel)
        }

        override fun newArray(size: Int): Array<ShowSeason?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as ShowSeason

        if (id != other.id) {
            return false
        }

        if (name != other.name) {
            return false
        }

        if (seasonNumber != other.seasonNumber) {
            return false
        }

        if (episodeCount != other.episodeCount) {
            return false
        }

        if (premiereDate != other.premiereDate) {
            return false
        }

        if (endDate != other.endDate) {
            return false
        }

        if (mediumImageUrl != other.mediumImageUrl) {
            return false
        }

        if (originalImageUrl != other.originalImageUrl) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (seasonNumber ?: 0)
        result = 31 * result + (episodeCount ?: 0)
        result = 31 * result + (premiereDate?.hashCode() ?: 0)
        result = 31 * result + (endDate?.hashCode() ?: 0)
        result = 31 * result + (mediumImageUrl?.hashCode() ?: 0)
        result = 31 * result + (originalImageUrl?.hashCode() ?: 0)
        return result
    }
}