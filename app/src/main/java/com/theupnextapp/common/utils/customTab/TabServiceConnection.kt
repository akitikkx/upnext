package com.theupnextapp.common.utils.customTab

import android.content.ComponentName
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import java.lang.ref.WeakReference

class TabServiceConnection(connectionCallback: TabServiceConnectionCallback) :
    CustomTabsServiceConnection() {

    private val mConnectionCallback: WeakReference<TabServiceConnectionCallback> =
        WeakReference<TabServiceConnectionCallback>(connectionCallback)

    override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        mConnectionCallback.get()?.onTabServiceConnected(client)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        mConnectionCallback.get()?.onTabServiceDisconnected()
    }
}