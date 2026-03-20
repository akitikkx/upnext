package com.theupnextapp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Bypass native Crashlytics / Analytics bindings that fail natively inside Robolectric
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options =
                FirebaseOptions.Builder()
                    .setApiKey("TestApiKeyTikiwa")
                    .setApplicationId("TestAppId12345")
                    .setProjectId("TestProjectId")
                    .build()
            FirebaseApp.initializeApp(this, options)
        }
    }
}
