package com.theupnextapp.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.TvMazeDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.SearchRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.ShowDetailRepository
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
    fun provideShowDetailRepository(
        upnextDao: UpnextDao,
        tvMazeService: TvMazeService,
        firebaseCrashlytics: FirebaseCrashlytics
    ): ShowDetailRepository {
        return ShowDetailRepository(
            upnextDao = upnextDao,
            tvMazeService = tvMazeService,
            firebaseCrashlytics = firebaseCrashlytics
        )
    }

    @Singleton
    @Provides
    fun provideTraktRepository(
        upnextDao: UpnextDao,
        traktDao: TraktDao,
        tvMazeService: TvMazeService,
        traktService: TraktService,
        firebaseCrashlytics: FirebaseCrashlytics
    ): TraktRepository {
        return TraktRepository(
            upnextDao = upnextDao,
            traktDao = traktDao,
            tvMazeService = tvMazeService,
            traktService = traktService,
            firebaseCrashlytics = firebaseCrashlytics
        )
    }

    @Singleton
    @Provides
    fun provideSearchRepository(tvMazeService: TvMazeService): SearchRepository {
        return SearchRepository(tvMazeService)
    }

    @Singleton
    @Provides
    fun provideDashboardRepository(
        upnextDao: UpnextDao,
        tvMazeDao: TvMazeDao,
        tvMazeService: TvMazeService,
        firebaseCrashlytics: FirebaseCrashlytics
    ): DashboardRepository {
        return DashboardRepository(
            upnextDao = upnextDao,
            tvMazeDao = tvMazeDao,
            tvMazeService = tvMazeService,
            firebaseCrashlytics = firebaseCrashlytics
        )
    }
}