package com.theupnextapp.common.utils

import com.theupnextapp.BuildConfig

object TraktConstants {
    const val TRAKT_AUTH_URL =
        "https://trakt.tv/oauth/authorize?response_type=code" +
            "&client_id=${BuildConfig.TRAKT_CLIENT_ID}" +
            "&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}"
}
