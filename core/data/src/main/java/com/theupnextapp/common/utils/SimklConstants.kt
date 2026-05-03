package com.theupnextapp.common.utils

import com.theupnextapp.core.data.BuildConfig

object SimklConstants {
    const val SIMKL_AUTH_URL =
        "https://simkl.com/oauth/authorize?response_type=code" +
            "&client_id=${BuildConfig.SIMKL_CLIENT_ID}" +
            "&redirect_uri=${BuildConfig.SIMKL_REDIRECT_URI}"
}
