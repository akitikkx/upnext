package com.theupnextapp.common.utils.customTab

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import com.theupnextapp.BuildConfig
import com.theupnextapp.ui.common.BaseFragment

class WebviewFallback : CustomTabFallback {

    override fun openUri(activity: Activity, uri: Uri) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("${BaseFragment.TRAKT_API_URL}${BaseFragment.TRAKT_OAUTH_ENDPOINT}?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}")
        )
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, activity.packageName)
        activity.startActivity(intent)
    }
}