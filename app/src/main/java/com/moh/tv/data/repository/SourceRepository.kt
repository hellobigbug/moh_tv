package com.moh.tv.data.repository

import com.moh.tv.data.local.dao.SourceDao
import com.moh.tv.data.local.dao.UpdateLogDao
import com.moh.tv.data.local.entity.SourceEntity
import com.moh.tv.data.local.entity.UpdateLogEntity
import com.moh.tv.data.remote.DiscoveredSource
import com.moh.tv.data.remote.GithubSourceSearcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceRepository @Inject constructor(
    private val sourceDao: SourceDao,
    private val updateLogDao: UpdateLogDao,
    private val githubSourceSearcher: GithubSourceSearcher
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
        val defaultSources = githubSourceSearcher.getReliableSources()
        sourceDao.insertSources(defaultSources)
    }

    /**
     * 搜索GitHub上的可用IPTV源
     */
    suspend fun searchGithubSources(): List<DiscoveredSource> {
        return githubSourceSearcher.searchGithubSources()
    }

    /**
     * 验证并添加发现的源
     */
    suspend fun validateAndAddDiscoveredSource(discovered: DiscoveredSource): Boolean {
        val isValid = githubSourceSearcher.validateSource(discovered.url)
        if (isValid) {
            addSource(
                SourceEntity(
                    name = discovered.name,
                    url = discovered.url,
                    enabled = false, // 默认不启用，让用户手动选择
                    autoUpdate = true
                )
            )
        }
        return isValid
    }
}
