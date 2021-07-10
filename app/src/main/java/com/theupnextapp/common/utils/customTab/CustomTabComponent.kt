package com.theupnextapp.common.utils.customTab

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import timber.log.Timber

open class CustomTabComponent : TabServiceConnectionCallback {

    private var client: CustomTabsClient? = null

    private var connectionCallback: TabConnectionCallback? = null

    private var customTabSession: CustomTabsSession? = null

    private var correctPackage: String? = null

    private var tabServiceConnection: CustomTabsServiceConnection? = null

    override fun onTabServiceConnected(client: CustomTabsClient) {
        this.client = client
        this.client?.warmup(0L)
        connectionCallback?.onTabConnected()
    }

    override fun onTabServiceDisconnected() {
        this.client = null
        customTabSession = null
        connectionCallback?.onTabDisconnected()
    }

    fun openCustomTab(
        activity: Activity,
        customTabsIntent: CustomTabsIntent,
        uri: Uri,
        customTabFallback: CustomTabFallback
    ) {
        val packageName = detectCorrectPackage(activity)

        if (packageName == null) {
            customTabFallback.openUri(activity, uri)
        } else {
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(activity, uri)
        }
    }

    fun unBindCustomTabService(activity: Activity) {
        if (tabServiceConnection == null) return
        activity.unbindService(tabServiceConnection as CustomTabsServiceConnection)
        client = null
        customTabSession = null
        tabServiceConnection = null
    }

    fun getSession(): CustomTabsSession? {
        if (client == null) {
            customTabSession = null
        } else if (customTabSession == null) {
            customTabSession = client?.newSession(null)
        }
        return customTabSession
    }

    fun setConnectionCallback(connectionCallback: TabConnectionCallback?) {
        this.connectionCallback = connectionCallback
    }

    fun bindCustomService(activity: Activity) {
        if (client != null) return

        val packageName = detectCorrectPackage(activity) ?: return
        tabServiceConnection = TabServiceConnection(this)
        CustomTabsClient.bindCustomTabsService(
            activity, packageName,
            tabServiceConnection as TabServiceConnection
        )
    }

    fun mayLaunchUrl(uri: Uri?, bundle: Bundle?, otherLikelyBundle: List<Bundle>?): Boolean {
        if (client == null) return false
        val session: CustomTabsSession? = getSession()
        return session?.mayLaunchUrl(uri, bundle, otherLikelyBundle) ?: false
    }

    private fun detectCorrectPackage(context: Context): String? {
        if (correctPackage != null) {
            return correctPackage
        }
        val packageManager = context.packageManager
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
        val defaultViewHandlerInfo = packageManager.resolveActivity(activityIntent, 0)
        val defaultViewHandlerPackageName: String? =
            defaultViewHandlerInfo?.activityInfo?.packageName
        val resolvedActivityList = packageManager.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs = mutableListOf<String>()

        for (info: ResolveInfo in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            if (packageManager.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName)
            }
        }

        if (packagesSupportingCustomTabs.isNullOrEmpty()) {
            correctPackage = null
        } else if (packagesSupportingCustomTabs.size == 1) {
            correctPackage = packagesSupportingCustomTabs[0]
        } else if (!defaultViewHandlerPackageName.isNullOrEmpty() && !hasSpecificIntentHandlers(
                context,
                activityIntent
            ) && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)
        ) {
            correctPackage = defaultViewHandlerPackageName
        } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            correctPackage = STABLE_PACKAGE
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            correctPackage = BETA_PACKAGE
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            correctPackage = DEV_PACKAGE
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            correctPackage = LOCAL_PACKAGE
        }
        return correctPackage
    }

    private fun hasSpecificIntentHandlers(context: Context, intent: Intent): Boolean {
        try {
            val packageManager = context.packageManager
            val handlers =
                packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER)
            if (handlers.isEmpty()) {
                return false
            }
            for (resolveInfo: ResolveInfo in handlers) {
                val filter: IntentFilter = resolveInfo.filter ?: continue
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue
                if (resolveInfo.activityInfo == null) continue
                return true
            }
        } catch (e: RuntimeException) {
            Timber.d(e)
        }
        return false
    }

    companion object {
        private const val ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService"
        private const val STABLE_PACKAGE = "com.android.chrome"
        private const val BETA_PACKAGE = "com.chrome.beta"
        private const val DEV_PACKAGE = "com.chrome.dev"
        private const val LOCAL_PACKAGE = "com.google.android.apps.chrome"
    }
}