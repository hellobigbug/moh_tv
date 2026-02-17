package com.moh.tv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.moh.tv.data.local.Converters

@Entity(tableName = "channels")
@TypeConverters(Converters::class)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val group: String,
    val logo: String = "",
    val epgUrl: String = "",
    val quality: Int = 0,
    val status: Int = 1,
    val isFavorite: Boolean = false,
    val lastWatched: Long = 0L,
    val watchCount: Int = 0
)

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val enabled: Boolean = true,
    val autoUpdate: Boolean = true,
    val updateInterval: Long = 24 * 60 * 60 * 1000L,
    val lastUpdate: Long = 0L
)

@Entity(tableName = "update_logs")
data class UpdateLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val added: Int = 0,
    val removed: Int = 0,
    val updated: Int = 0,
    val message: String = ""
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val channelId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_watched")
data class RecentWatchedEntity(
    @PrimaryKey
    val channelId: Long,
    val watchedAt: Long = System.currentTimeMillis()
)
