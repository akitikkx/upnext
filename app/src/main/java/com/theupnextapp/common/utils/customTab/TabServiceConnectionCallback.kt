package com.theupnextapp.common.utils.customTab

import androidx.browser.customtabs.CustomTabsClient

interface TabServiceConnectionCallback {
    fun onTabServiceConnected(client: CustomTabsClient)

    fun onTabServiceDisconnected()
}