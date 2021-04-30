package com.theupnextapp.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.UpnextRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideUpnextRepository(
        upnextDao: UpnextDao,
        firebaseCrashlytics: FirebaseCrashlytics
    ): UpnextRepository {
        return UpnextRepository(upnextDao, firebaseCrashlytics)
    }

    @Singleton
    @Provides
    fun provideTraktRepository(
        upnextDao: UpnextDao,
        firebaseCrashlytics: FirebaseCrashlytics
    ): TraktRepository {
        return TraktRepository(upnextDao, firebaseCrashlytics)
    }
}