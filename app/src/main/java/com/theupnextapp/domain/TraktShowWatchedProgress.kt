package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class TraktShowWatchedProgress(
    val episodesAired: Int,
    val episodesWatched: Int,
    val lastWatchedAt: String?,
    val nextEpisodeToWatch: TraktWatchedShowProgressNextEpisode?,
    val previousEpisodeWatched: TraktWatchedShowProgressLastEpisode?,
    val seasons: List<TraktWatchedShowProgressSeason>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readParcelable(TraktWatchedShowProgressNextEpisode::class.java.classLoader),
        parcel.readParcelable(TraktWatchedShowProgressLastEpisode::class.java.classLoader),
        parcel.createTypedArrayList(TraktWatchedShowProgressSeason)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(episodesAired)
        parcel.writeInt(episodesWatched)
        parcel.writeString(lastWatchedAt)
        parcel.writeParcelable(nextEpisodeToWatch, flags)
        parcel.writeParcelable(previousEpisodeWatched, flags)
        parcel.writeTypedList(seasons)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktShowWatchedProgress> {
        override fun createFromParcel(parcel: Parcel): TraktShowWatchedProgress {
            return TraktShowWatchedProgress(parcel)
        }

        override fun newArray(size: Int): Array<TraktShowWatchedProgress?> {
            return arrayOfNulls(size)
        }
    }

}

data class TraktWatchedShowProgressNextEpisode(
    val completed: Boolean?,
    val last_watched_at: String?,
    val number: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(completed)
        parcel.writeString(last_watched_at)
        parcel.writeValue(number)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktWatchedShowProgressNextEpisode> {
        override fun createFromParcel(parcel: Parcel): TraktWatchedShowProgressNextEpisode {
            return TraktWatchedShowProgressNextEpisode(parcel)
        }

        override fun newArray(size: Int): Array<TraktWatchedShowProgressNextEpisode?> {
            return arrayOfNulls(size)
        }
    }
}

data class TraktWatchedShowProgressLastEpisode(
    val ids: TraktWatchedShowProgressIdsX?,
    val number: Int?,
    val season: Int?,
    val title: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(TraktWatchedShowProgressIdsX::class.java.classLoader),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(ids, flags)
        parcel.writeValue(number)
        parcel.writeValue(season)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktWatchedShowProgressLastEpisode> {
        override fun createFromParcel(parcel: Parcel): TraktWatchedShowProgressLastEpisode {
            return TraktWatchedShowProgressLastEpisode(parcel)
        }

        override fun newArray(size: Int): Array<TraktWatchedShowProgressLastEpisode?> {
            return arrayOfNulls(size)
        }
    }
}

data class TraktWatchedShowProgressIdsX(
    val imdb: String?,
    val tmdb: String?,
    val trakt: Int?,
    val tvdb: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imdb)
        parcel.writeString(tmdb)
        parcel.writeValue(trakt)
        parcel.writeValue(tvdb)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktWatchedShowProgressIdsX> {
        override fun createFromParcel(parcel: Parcel): TraktWatchedShowProgressIdsX {
            return TraktWatchedShowProgressIdsX(parcel)
        }

        override fun newArray(size: Int): Array<TraktWatchedShowProgressIdsX?> {
            return arrayOfNulls(size)
        }
    }
}

data class TraktWatchedShowProgressSeason(
    val aired: Int,
    val completed: Int,
    val episodes: List<TraktWatchedShowProgressSeasonEpisode>?,
    val number: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.createTypedArrayList(TraktWatchedShowProgressSeasonEpisode),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(aired)
        parcel.writeInt(completed)
        parcel.writeTypedList(episodes)
        parcel.writeValue(number)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktWatchedShowProgressSeason> {
        override fun createFromParcel(parcel: Parcel): TraktWatchedShowProgressSeason {
            return TraktWatchedShowProgressSeason(parcel)
        }

        override fun newArray(size: Int): Array<TraktWatchedShowProgressSeason?> {
            return arrayOfNulls(size)
        }
    }

}

data class TraktWatchedShowProgressSeasonEpisode(
    val completed: Boolean?,
    val last_watched_at: String?,
    val number: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(completed)
        parcel.writeString(last_watched_at)
        parcel.writeValue(number)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktWatchedShowProgressSeasonEpisode> {
        override fun createFromParcel(parcel: Parcel): TraktWatchedShowProgressSeasonEpisode {
            return TraktWatchedShowProgressSeasonEpisode(parcel)
        }

        override fun newArray(size: Int): Array<TraktWatchedShowProgressSeasonEpisode?> {
            return arrayOfNulls(size)
        }
    }
}