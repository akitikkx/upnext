package com.theupnextapp.di

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FirebaseAnalyticsModule {

    @Singleton
    @Provides
    fun provideFirebaseAnalytics(application: Application) : FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(application)
    }
}