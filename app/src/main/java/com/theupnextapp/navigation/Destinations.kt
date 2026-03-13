package com.theupnextapp.navigation

import com.theupnextapp.domain.EpisodeDetailArg
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
    data class TraktAccount(val code: String? = null) : Destinations

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
            )
    }

    @Serializable
    data object EmptyDetail : Destinations
}
