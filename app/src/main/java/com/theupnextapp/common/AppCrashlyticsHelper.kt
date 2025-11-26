package com.theupnextapp.common

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class AppCrashlyticsHelper
    @Inject
    constructor() : CrashlyticsHelper {
        override fun log(message: String) {
            FirebaseCrashlytics.getInstance().log(message)
        }

        override fun recordException(e: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
