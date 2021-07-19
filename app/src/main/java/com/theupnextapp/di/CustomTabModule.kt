package com.theupnextapp.di

import com.theupnextapp.common.utils.customTab.CustomTabComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CustomTabModule {

    @Provides
    @Singleton
    fun provideCustomTabComponent() : CustomTabComponent = CustomTabComponent()
}