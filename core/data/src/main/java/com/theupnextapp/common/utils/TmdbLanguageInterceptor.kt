package com.theupnextapp.common.utils

import com.theupnextapp.core.data.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale

class TmdbLanguageInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url
        val url = originalHttpUrl.newBuilder()
            .addQueryParameter("api_key", BuildConfig.TMDB_ACCESS_TOKEN)
            .addQueryParameter("language", Locale.getDefault().toLanguageTag())
            .build()
        val requestBuilder = original.newBuilder().url(url)
        return chain.proceed(requestBuilder.build())
    }
}
