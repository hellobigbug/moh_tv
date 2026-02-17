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
                name = "IPTV-org 中国",
                url = "https://iptv-org.github.io/iptv/countries/cn.m3u",
                enabled = true,
                autoUpdate = true
            ),
            SourceEntity(
                name = "IPTV-org 全球",
                url = "https://iptv-org.github.io/iptv/index.m3u",
                enabled = false,
                autoUpdate = true
            ),
            SourceEntity(
                name = "Free-TV 全球",
                url = "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u",
                enabled = false,
                autoUpdate = true
            ),
            SourceEntity(
                name = "YanG-1989 大陆",
                url = "https://mirror.ghproxy.com/https://raw.githubusercontent.com/YanG-1989/m3u/main/Gather.m3u",
                enabled = false,
                autoUpdate = true
            ),
            SourceEntity(
                name = "YanG-1989 成人",
                url = "https://mirror.ghproxy.com/https://raw.githubusercontent.com/YanG-1989/m3u/main/Adult.m3u",
                enabled = false,
                autoUpdate = true
            ),
            SourceEntity(
                name = "M3U-China",
                url = "https://raw.githubusercontent.com/Kimentanxm/M3U-China/main/M3U-China.m3u",
                enabled = false,
                autoUpdate = true
            ),
            SourceEntity(
                name = "IPTV-Global 4K",
                url = "https://iptv-org.github.io/iptv/index.quality.m3u",
                enabled = false,
                autoUpdate = true
            ),
            SourceEntity(
                name = "EPGShare",
                url = "https://raw.githubusercontent.com/iptv-org/epg/master/iptv-org.github.io/iptv/index.m3u",
                enabled = false,
                autoUpdate = true
            ),
            SourceEntity(
                name = "Tvbox-开源直播",
                url = "https://mirror.ghproxy.com/https://raw.githubusercontent.com/tvbox2025/tvbox/main/live.txt",
                enabled = false,
                autoUpdate = true
            ),
            SourceEntity(
                name = "MyIPTV",
                url = "https://mirror.ghproxy.com/https://raw.githubusercontent.com/SP-Spacer/myiptv/main/myiptv.m3u",
                enabled = false,
                autoUpdate = true
            ),
            SourceEntity(
                name = "GuoXiaoBin 直播源",
                url = "https://mirror.ghproxy.com/https://raw.githubusercontent.com/guoxiaobin2020/live/main/ipv6.m3u",
                enabled = false,
                autoUpdate = true
            )
        )
        sourceDao.insertSources(defaultSources)
    }
}
