package com.theupnextapp.navigation

import com.theupnextapp.domain.EpisodeDetailArg
import com.theupnextapp.domain.PersonDetailArg
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object Dashboard : Destinations

    @Serializable
    data object Schedule : Destinations

    @Serializable
    data object Search : Destinations

    @Serializable
    data object Explore : Destinations

    @Serializable
    data class Account(val code: String? = null) : Destinations

    @Serializable
    data object Settings : Destinations

    @Serializable
    data class ShowDetail(
        val source: String? = null,
        val showId: String?,
        val showTitle: String?,
        val showImageUrl: String?,
        val showBackgroundUrl: String?,
        val imdbID: String? = null,
        val isAuthorizedOnTrakt: Boolean? = false,
        val showTraktId: Int? = null,
        val tvdbID: Int? = null,
        val simklID: Int? = null,
    ) : Destinations {
        fun toArg() =
            ShowDetailArg(
                source,
                showId,
                showTitle,
                showImageUrl,
                showBackgroundUrl,
                imdbID,
                isAuthorizedOnTrakt,
                showTraktId,
                tvdbID,
                simklID,
            )
    }

    @Serializable
    data class ShowSeasons(
        val source: String? = null,
        val showId: String?,
        val showTitle: String?,
        val showImageUrl: String?,
        val showBackgroundUrl: String?,
        val imdbID: String? = null,
        val isAuthorizedOnTrakt: Boolean? = false,
        val showTraktId: Int? = null,
    ) : Destinations {
        fun toArg() =
            ShowDetailArg(
                source,
                showId,
                showTitle,
                showImageUrl,
                showBackgroundUrl,
                imdbID,
                isAuthorizedOnTrakt,
                showTraktId,
            )
    }

    @Serializable
    data class ShowSeasonEpisodes(
        val showId: Int?,
        val seasonNumber: Int?,
        val imdbID: String? = null,
        val isAuthorizedOnTrakt: Boolean? = false,
        val showTraktId: Int? = null,
        val showTitle: String? = null,
        val showImageUrl: String? = null,
        val showBackgroundUrl: String? = null,
    ) : Destinations {
        fun toArg() =
            ShowSeasonEpisodesArg(
                showId = showId,
                seasonNumber = seasonNumber,
                imdbID = imdbID,
                isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                showTraktId = showTraktId,
                showTitle = showTitle,
                showImageUrl = showImageUrl,
                showBackgroundUrl = showBackgroundUrl,
            )
    }

    @Serializable
    data class EpisodeDetail(
        val showTraktId: Int,
        val seasonNumber: Int,
        val episodeNumber: Int,
        val showTitle: String? = null,
        val showId: Int? = null,
        val imdbID: String? = null,
        val isAuthorizedOnTrakt: Boolean? = false,
        val showImageUrl: String? = null,
        val showBackgroundUrl: String? = null,
        val episodeImageUrl: String? = null,
    ) : Destinations {
        fun toArg() =
            EpisodeDetailArg(
                showTraktId = showTraktId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                showTitle = showTitle,
                showId = showId,
                imdbID = imdbID,
                isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                showImageUrl = showImageUrl,
                showBackgroundUrl = showBackgroundUrl,
                episodeImageUrl = episodeImageUrl,
            )
    }

    @Serializable
    data class PersonDetail(
        val personId: String,
        val personName: String,
        val personImageUrl: String? = null,
    ) : Destinations {
        fun toArg() =
            PersonDetailArg(
                personId = personId,
                personName = personName,
                personImageUrl = personImageUrl,
            )
    }

    @Serializable
    data object EmptyDetail : Destinations
}
