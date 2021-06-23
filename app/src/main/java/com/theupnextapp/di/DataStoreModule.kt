package com.theupnextapp.di

import android.content.Context
import com.theupnextapp.repository.datastore.UpnextManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataStoreModule {

    @Provides
    @Singleton
    fun provideUpnextManager(@ApplicationContext applicationContext: Context): UpnextManager =
        UpnextManager(applicationContext)
}