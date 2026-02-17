package com.moh.tv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.moh.tv.data.local.dao.*
import com.moh.tv.data.local.entity.*

@Database(
    entities = [
        ChannelEntity::class,
        SourceEntity::class,
        UpdateLogEntity::class,
        FavoriteEntity::class,
        RecentWatchedEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun sourceDao(): SourceDao
    abstract fun updateLogDao(): UpdateLogDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentWatchedDao(): RecentWatchedDao
}
