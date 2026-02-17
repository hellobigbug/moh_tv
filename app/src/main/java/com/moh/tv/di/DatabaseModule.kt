package com.moh.tv.di

import android.content.Context
import androidx.room.Room
import com.moh.tv.data.local.AppDatabase
import com.moh.tv.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mohtv_database"
        ).build()
    }

    @Provides
    fun provideChannelDao(database: AppDatabase): ChannelDao = database.channelDao()

    @Provides
    fun provideSourceDao(database: AppDatabase): SourceDao = database.sourceDao()

    @Provides
    fun provideUpdateLogDao(database: AppDatabase): UpdateLogDao = database.updateLogDao()

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideRecentWatchedDao(database: AppDatabase): RecentWatchedDao = database.recentWatchedDao()
}
