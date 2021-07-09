package com.theupnextapp.common.utils.customTab

import android.app.Activity
import android.net.Uri

interface CustomTabFallback {
    fun openUri(activity: Activity, uri: Uri)
}