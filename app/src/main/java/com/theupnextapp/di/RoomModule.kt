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

import android.content.Context
import androidx.room.Room
import com.theupnextapp.database.MIGRATION_14_15
import com.theupnextapp.database.MIGRATION_15_16
import com.theupnextapp.database.MIGRATION_16_17
import com.theupnextapp.database.MIGRATION_17_18
import com.theupnextapp.database.MIGRATION_18_19
import com.theupnextapp.database.MIGRATION_19_20
import com.theupnextapp.database.MIGRATION_20_21
import com.theupnextapp.database.MIGRATION_21_22
import com.theupnextapp.database.MIGRATION_22_23
import com.theupnextapp.database.MIGRATION_23_24
import com.theupnextapp.database.MIGRATION_24_25
import com.theupnextapp.database.MIGRATION_25_26
import com.theupnextapp.database.MIGRATION_26_27
import com.theupnextapp.database.MIGRATION_27_28
import com.theupnextapp.database.MIGRATION_28_29
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.TvMazeDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.database.UpnextDatabase
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
                MIGRATION_23_24,
                MIGRATION_24_25,
                MIGRATION_25_26,
                MIGRATION_26_27,
                MIGRATION_27_28,
                MIGRATION_28_29,
            )
            .build()
    }

    @Singleton
    @Provides
    fun provideUpnextDao(upnextDatabase: UpnextDatabase): UpnextDao {
        return upnextDatabase.upnextDao
    }

    @Singleton
    @Provides
    fun provideTraktDao(upnextDatabase: UpnextDatabase): TraktDao {
        return upnextDatabase.traktDao
    }

    @Singleton
    @Provides
    fun provideTvMazeDao(upnextDatabase: UpnextDatabase): TvMazeDao {
        return upnextDatabase.tvMazeDao
    }
}
