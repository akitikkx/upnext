package com.theupnextapp.di

import android.content.Context
import com.theupnextapp.repository.datastore.TraktUserManager
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
    fun provideTraktDataStore(@ApplicationContext applicationContext: Context): TraktUserManager =
        TraktUserManager(applicationContext)
}