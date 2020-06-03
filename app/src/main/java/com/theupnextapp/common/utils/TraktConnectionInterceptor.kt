package com.theupnextapp.common.utils

import com.theupnextapp.BuildConfig
import okhttp3.*

class TraktConnectionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("trakt-api-version", "2")
            .addHeader("trakt-api-key", BuildConfig.TRAKT_CLIENT_ID)
            .build()
        return chain.proceed(request)
    }

}