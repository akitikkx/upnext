package com.theupnextapp.di

import android.content.Context
import androidx.room.Room
import com.theupnextapp.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RoomModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): UpnextDatabase {
        return Room
            .databaseBuilder(
                context,
                UpnextDatabase::class.java,
                "upnext"
            )
            .addMigrations(
                MIGRATION_14_15,
                MIGRATION_15_16,
                MIGRATION_16_17,
                MIGRATION_17_18,
                MIGRATION_18_19,
                MIGRATION_19_20,
                MIGRATION_20_21,
                MIGRATION_21_22,
                MIGRATION_22_23,
                MIGRATION_23_24
            )
            .build()
    }

    @Singleton
    @Provides
    fun provideUpnextDao(upnextDatabase: UpnextDatabase): UpnextDao {
        return upnextDatabase.upnextDao
    }
}