package com.moh.tv.data.remote

import com.moh.tv.data.local.entity.ChannelEntity
import com.moh.tv.data.local.entity.SourceEntity
import com.moh.tv.data.local.entity.UpdateLogEntity
import com.moh.tv.data.repository.ChannelRepository
import com.moh.tv.data.repository.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateResult(
    val added: Int = 0,
    val removed: Int = 0,
    val updated: Int = 0,
    val success: Boolean = true,
    val message: String = ""
)

@Singleton
class SourceSyncManager @Inject constructor(
    private val iptvParser: IptvParser,
    private val sourceRepository: SourceRepository,
    private val channelRepository: ChannelRepository
) {

    suspend fun syncAllSources(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val sources = sourceRepository.getEnabledSources().first()
            val allChannels = mutableListOf<ChannelEntity>()

            coroutineScope {
                val results = sources.map { source ->
                    async {
                        try {
                            val channels = iptvParser.parseM3U(source.url)
                            source to channels
                        } catch (e: Exception) {
                            source to emptyList()
                        }
                    }
                }.awaitAll()

                for ((source, channels) in results) {
                    val entities = channels.map { channel ->
                        ChannelEntity(
                            name = channel.name,
                            url = channel.url,
                            group = channel.group,
                            logo = channel.logo,
                            epgUrl = channel.epgUrl,
                            quality = channel.quality,
                            status = if (channel.url.isNotEmpty()) 1 else 0
                        )
                    }
                    allChannels.addAll(entities)
                    sourceRepository.updateLastUpdate(source.id)
                }
            }

            val oldChannels = channelRepository.getAllChannels().first()
            val oldUrls = oldChannels.map { it.url }.toSet()
            val newUrls = allChannels.map { it.url }.toSet()

            val added = newUrls - oldUrls
            val removed = oldUrls - newUrls

            channelRepository.deleteAllChannels()
            channelRepository.saveChannels(allChannels)

            val result = UpdateResult(
                added = added.size,
                removed = removed.size,
                updated = allChannels.size,
                success = true,
                message = "成功更新 ${allChannels.size} 个频道"
            )

            sourceRepository.saveUpdateLog(
                UpdateLogEntity(
                    timestamp = System.currentTimeMillis(),
                    added = added.size,
                    removed = removed.size,
                    updated = allChannels.size,
                    message = result.message
                )
            )

            result
        } catch (e: Exception) {
            e.printStackTrace()
            UpdateResult(success = false, message = e.message ?: "更新失败")
        }
    }

    suspend fun syncSingleSource(source: SourceEntity): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val channels = iptvParser.parseM3U(source.url)

            if (channels.isEmpty()) {
                return@withContext UpdateResult(
                    success = false,
                    message = "未能解析到任何频道，请检查源地址是否正确"
                )
            }

            val entities = channels.map { channel ->
                ChannelEntity(
                    name = channel.name,
                    url = channel.url,
                    group = channel.group.ifEmpty { "未分类" },
                    logo = channel.logo,
                    epgUrl = channel.epgUrl,
                    quality = channel.quality,
                    status = 1
                )
            }

            channelRepository.deleteAllChannels()
            channelRepository.saveChannels(entities)
            sourceRepository.updateLastUpdate(source.id)

            val groups = entities.map { it.group }.distinct().size
            
            UpdateResult(
                updated = entities.size,
                success = true,
                message = "成功更新 ${entities.size} 个频道，${groups} 个分类"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            UpdateResult(
                success = false,
                message = "同步失败: ${e.message ?: "网络错误"}"
            )
        }
    }

    suspend fun validateSource(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val channels = iptvParser.parseM3U(url)
            channels.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun backupSources(): String = withContext(Dispatchers.IO) {
        val sources = sourceRepository.getAllSources().first()
        buildString {
            sources.forEach { source ->
                appendLine("${source.name},${source.url},${source.enabled}")
            }
        }
    }

    suspend fun restoreSources(backup: String) = withContext(Dispatchers.IO) {
        val lines = backup.lines().filter { it.isNotBlank() }
        val sources = lines.mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size >= 3) {
                SourceEntity(
                    name = parts[0],
                    url = parts[1],
                    enabled = parts[2].toBoolean()
                )
            } else null
        }
        sources.forEach { sourceRepository.addSource(it) }
    }
}
