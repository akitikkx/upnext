package com.theupnextapp.fake

import com.theupnextapp.common.CrashlyticsHelper

class FakeFirebaseCrashlytics : CrashlyticsHelper {
    private val recordedExceptions = mutableListOf<Throwable>()

    override fun recordException(e: Throwable) {
        recordedExceptions.add(e)
    }

    // Helper method for assertions in tests
    fun getRecordedExceptions(): List<Throwable> = recordedExceptions

    fun clear() {
        recordedExceptions.clear()
    }
}