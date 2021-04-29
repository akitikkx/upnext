package com.theupnextapp.di

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
    fun provideUpnextRepository(upnextDao: UpnextDao): UpnextRepository {
        return UpnextRepository(upnextDao)
    }

    @Singleton
    @Provides
    fun provideTraktRepository(upnextDao: UpnextDao): TraktRepository {
        return TraktRepository(upnextDao)
    }
}