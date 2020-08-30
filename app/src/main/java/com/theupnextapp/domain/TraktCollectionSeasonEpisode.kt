package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class TraktCollectionSeasonEpisode(
    var id: Long,
    var imdbID: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val collectedAt: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(imdbID)
        parcel.writeValue(seasonNumber)
        parcel.writeValue(episodeNumber)
        parcel.writeString(collectedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktCollectionSeasonEpisode> {
        override fun createFromParcel(parcel: Parcel): TraktCollectionSeasonEpisode {
            return TraktCollectionSeasonEpisode(parcel)
        }

        override fun newArray(size: Int): Array<TraktCollectionSeasonEpisode?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as TraktCollectionSeasonEpisode

        if (id != other.id) {
            return false
        }

        if (imdbID != other.imdbID) {
            return false
        }

        if (seasonNumber != other.seasonNumber) {
            return false
        }

        if (episodeNumber != other.episodeNumber) {
            return false
        }

        if (collectedAt != other.collectedAt) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (imdbID?.hashCode() ?: 0)
        result = 31 * result + (seasonNumber ?: 0)
        result = 31 * result + (episodeNumber ?: 0)
        result = 31 * result + (collectedAt?.hashCode() ?: 0)
        return result
    }
}