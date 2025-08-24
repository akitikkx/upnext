package com.theupnextapp.di

import com.theupnextapp.common.AppCrashlyticsHelper
import com.theupnextapp.common.CrashlyticsHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindCrashlyticsHelper(
        appCrashlyticsHelper: AppCrashlyticsHelper
    ): CrashlyticsHelper
}
