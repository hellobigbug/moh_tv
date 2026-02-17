package com.moh.tv.data.repository

import com.moh.tv.data.local.dao.SourceDao
import com.moh.tv.data.local.dao.UpdateLogDao
import com.moh.tv.data.local.entity.SourceEntity
import com.moh.tv.data.local.entity.UpdateLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceRepository @Inject constructor(
    private val sourceDao: SourceDao,
    private val updateLogDao: UpdateLogDao
) {
    fun getEnabledSources(): Flow<List<SourceEntity>> = sourceDao.getEnabledSources()

    fun getAllSources(): Flow<List<SourceEntity>> = sourceDao.getAllSources()

    fun getRecentLogs(limit: Int = 10): Flow<List<UpdateLogEntity>> =
        updateLogDao.getRecentLogs(limit)

    suspend fun getSourceById(id: Long): SourceEntity? = sourceDao.getSourceById(id)

    suspend fun addSource(source: SourceEntity): Long {
        return sourceDao.insertSource(source)
    }

    suspend fun updateSource(source: SourceEntity) {
        sourceDao.updateSource(source)
    }

    suspend fun deleteSource(id: Long) {
        sourceDao.deleteById(id)
    }

    suspend fun toggleSourceEnabled(id: Long, enabled: Boolean) {
        sourceDao.updateEnabled(id, enabled)
    }

    suspend fun updateLastUpdate(id: Long) {
        sourceDao.updateLastUpdate(id)
    }

    suspend fun saveUpdateLog(log: UpdateLogEntity): Long {
        return updateLogDao.insertLog(log)
    }

    suspend fun addDefaultSources() {
        val defaultSources = listOf(
            SourceEntity(
                name = "FanMingMing Live",
                url = "https://mirror.ghproxy.com/https://raw.githubusercontent.com/fanmingming/live/main/tv/m3u/ipv6.m3u",
                enabled = true,
                autoUpdate = true
            ),
            SourceEntity(
                name = "IPTV-org",
                url = "https://github.com/iptv-org/iptv/raw/master/streams/cn.m3u",
                enabled = false,
                autoUpdate = true
            )
        )
        sourceDao.insertSources(defaultSources)
    }
}
