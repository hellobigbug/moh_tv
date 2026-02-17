package com.moh.tv.data.local.dao

import androidx.room.*
import com.moh.tv.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY `group`, name")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE `group` = :group ORDER BY name")
    fun getChannelsByGroup(group: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels ORDER BY lastWatched DESC LIMIT :limit")
    fun getRecentWatched(limit: Int = 20): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun searchChannels(query: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: Long): ChannelEntity?

    @Query("SELECT DISTINCT `group` FROM channels ORDER BY `group`")
    fun getAllGroups(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE channels SET lastWatched = :timestamp, watchCount = watchCount + 1 WHERE id = :id")
    suspend fun updateWatchTime(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM channels WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM channels")
    suspend fun deleteAll()
}

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources WHERE enabled = 1")
    fun getEnabledSources(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM sources ORDER BY name")
    fun getAllSources(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM sources WHERE id = :id")
    suspend fun getSourceById(id: Long): SourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<SourceEntity>)

    @Update
    suspend fun updateSource(source: SourceEntity)

    @Delete
    suspend fun deleteSource(source: SourceEntity)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE sources SET enabled = :enabled WHERE id = :id")
    suspend fun updateEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE sources SET lastUpdate = :timestamp WHERE id = :id")
    suspend fun updateLastUpdate(id: Long, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface UpdateLogDao {
    @Query("SELECT * FROM update_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 10): Flow<List<UpdateLogEntity>>

    @Insert
    suspend fun insertLog(log: UpdateLogEntity): Long

    @Query("DELETE FROM update_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)
}

@Dao
interface FavoriteDao {
    @Query("SELECT channelId FROM favorites")
    fun getFavoriteIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE channelId = :channelId")
    suspend fun removeFavorite(channelId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE channelId = :channelId)")
    suspend fun isFavorite(channelId: Long): Boolean
}

@Dao
interface RecentWatchedDao {
    @Query("SELECT * FROM recent_watched ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecentWatched(limit: Int = 20): Flow<List<RecentWatchedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecent(recent: RecentWatchedEntity)

    @Query("DELETE FROM recent_watched WHERE channelId NOT IN (SELECT channelId FROM recent_watched ORDER BY watchedAt DESC LIMIT :limit)")
    suspend fun keepRecentCount(limit: Int = 50)
}
