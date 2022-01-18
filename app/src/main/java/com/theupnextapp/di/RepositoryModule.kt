package com.theupnextapp.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.TvMazeDao
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
        tvMazeDao: TvMazeDao,
        firebaseCrashlytics: FirebaseCrashlytics
    ): UpnextRepository {
        return UpnextRepository(
            upnextDao = upnextDao,
            tvMazeDao = tvMazeDao,
            firebaseCrashlytics = firebaseCrashlytics
        )
    }

    @Singleton
    @Provides
    fun provideTraktRepository(
        upnextDao: UpnextDao,
        traktDao: TraktDao,
        firebaseCrashlytics: FirebaseCrashlytics
    ): TraktRepository {
        return TraktRepository(
            upnextDao = upnextDao,
            traktDao = traktDao,
            firebaseCrashlytics = firebaseCrashlytics
        )
    }
}