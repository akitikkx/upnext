package com.theupnextapp.navigation

import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSeasonEpisodesArg
import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object Dashboard : Destinations

    @Serializable
    data object Search : Destinations

    @Serializable
    data object Explore : Destinations

    @Serializable
    data class TraktAccount(val code: String? = null) : Destinations

    @Serializable
    data object Settings : Destinations

    @Serializable
    data class ShowDetail(val source: String? = null, val showId: String?, val showTitle: String?, val showImageUrl: String?, val showBackgroundUrl: String?, val imdbID: String? = null, val isAuthorizedOnTrakt: Boolean? = false, val showTraktId: Int? = null) : Destinations {
        fun toArg() = ShowDetailArg(source, showId, showTitle, showImageUrl, showBackgroundUrl, imdbID, isAuthorizedOnTrakt, showTraktId)
    }

    @Serializable
    data class ShowSeasons(val source: String? = null, val showId: String?, val showTitle: String?, val showImageUrl: String?, val showBackgroundUrl: String?, val imdbID: String? = null, val isAuthorizedOnTrakt: Boolean? = false, val showTraktId: Int? = null) : Destinations {
        fun toArg() = ShowDetailArg(source, showId, showTitle, showImageUrl, showBackgroundUrl, imdbID, isAuthorizedOnTrakt, showTraktId)
    }

    @Serializable
    data class ShowSeasonEpisodes(val showId: Int?, val seasonNumber: Int?, val imdbID: String? = null, val isAuthorizedOnTrakt: Boolean? = false, val showTraktId: Int? = null) : Destinations {
        fun toArg() = ShowSeasonEpisodesArg(showId, seasonNumber, imdbID, isAuthorizedOnTrakt, showTraktId)
    }

    @Serializable
    data object EmptyDetail : Destinations
}
