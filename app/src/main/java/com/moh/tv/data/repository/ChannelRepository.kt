package com.moh.tv.data.repository

import com.moh.tv.data.local.dao.*
import com.moh.tv.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val channelDao: ChannelDao,
    private val favoriteDao: FavoriteDao,
    private val recentWatchedDao: RecentWatchedDao
) {
    fun getAllChannels(): Flow<List<ChannelEntity>> = channelDao.getAllChannels()

    fun getChannelsByGroup(group: String): Flow<List<ChannelEntity>> =
        channelDao.getChannelsByGroup(group)

    fun getFavoriteChannels(): Flow<List<ChannelEntity>> = channelDao.getFavoriteChannels()

    fun getRecentWatched(limit: Int = 20): Flow<List<ChannelEntity>> =
        channelDao.getRecentWatched(limit)

    fun searchChannels(query: String): Flow<List<ChannelEntity>> =
        channelDao.searchChannels(query)

    fun getAllGroups(): Flow<List<String>> = channelDao.getAllGroups()

    suspend fun getChannelById(id: Long): ChannelEntity? = channelDao.getChannelById(id)

    suspend fun saveChannels(channels: List<ChannelEntity>) {
        channelDao.insertChannels(channels)
    }

    suspend fun saveChannel(channel: ChannelEntity): Long {
        return channelDao.insertChannel(channel)
    }

    suspend fun updateChannel(channel: ChannelEntity) {
        channelDao.updateChannel(channel)
    }

    suspend fun toggleFavorite(channelId: Long) {
        val isFav = favoriteDao.isFavorite(channelId)
        if (isFav) {
            favoriteDao.removeFavorite(channelId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(channelId))
        }
        channelDao.updateFavorite(channelId, !isFav)
    }

    suspend fun updateWatchTime(channelId: Long) {
        channelDao.updateWatchTime(channelId)
        recentWatchedDao.addRecent(RecentWatchedEntity(channelId))
        recentWatchedDao.keepRecentCount()
    }

    suspend fun deleteAllChannels() {
        channelDao.deleteAll()
    }

    fun getFavoriteIds(): Flow<List<Long>> = favoriteDao.getFavoriteIds()
}
