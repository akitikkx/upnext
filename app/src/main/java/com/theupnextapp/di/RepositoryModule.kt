/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.common.CrashlyticsHelper
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.TvMazeDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.datasource.TraktAccountDataSource
import com.theupnextapp.datasource.TraktAuthDataSource
import com.theupnextapp.datasource.TraktRecommendationsDataSource
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.DashboardRepositoryImpl
import com.theupnextapp.repository.SearchRepository
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.TraktRepositoryImpl
import com.theupnextapp.repository.WatchProgressRepository
import com.theupnextapp.repository.WatchProgressRepositoryImpl
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
        crashlyticsHelper: CrashlyticsHelper,
    ): ShowDetailRepository {
        return ShowDetailRepository(
            upnextDao = upnextDao,
            tvMazeService = tvMazeService,
            crashlytics = crashlyticsHelper,
        )
    }

    @Singleton
    @Provides
    fun provideTraktRepository(
        upnextDao: UpnextDao,
        traktDao: TraktDao,
        tvMazeService: TvMazeService,
        traktAuthDataSource: TraktAuthDataSource,
        traktRecommendationsDataSource: TraktRecommendationsDataSource,
        traktAccountDataSource: TraktAccountDataSource,
    ): TraktRepository {
        return TraktRepositoryImpl(
            upnextDao = upnextDao,
            traktDao = traktDao,
            tvMazeService = tvMazeService,
            traktAuthDataSource = traktAuthDataSource,
            traktRecommendationsDataSource = traktRecommendationsDataSource,
            traktAccountDataSource = traktAccountDataSource,
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
        crashlyticsHelper: CrashlyticsHelper,
    ): DashboardRepository {
        return DashboardRepositoryImpl(
            upnextDao = upnextDao,
            tvMazeDao = tvMazeDao,
            tvMazeService = tvMazeService,
            firebaseCrashlytics = crashlyticsHelper,
        )
    }

    @Singleton
    @Provides
    fun provideWatchProgressRepository(
        traktDao: TraktDao,
        traktService: TraktService,
        firebaseCrashlytics: FirebaseCrashlytics,
    ): WatchProgressRepository {
        return WatchProgressRepositoryImpl(
            traktDao = traktDao,
            traktService = traktService,
            firebaseCrashlytics = firebaseCrashlytics,
        )
    }

    @Singleton
    @Provides
    fun provideSettingsRepository(
        dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
    ): com.theupnextapp.repository.SettingsRepository {
        return com.theupnextapp.repository.SettingsRepositoryImpl(dataStore)
    }
}
