package com.theupnextapp.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FirebaseCrashlyticsModule {

    @Singleton
    @Provides
    fun provideFirebaseCrashlytics() : FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance()
    }
}