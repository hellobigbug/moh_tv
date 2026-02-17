package com.moh.tv.data.remote

import com.moh.tv.data.local.entity.ChannelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource

@Singleton
class EpgManager @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("GMT+8")
    }

    // EPG数据源列表
    private val epgSources = listOf(
        "https://epg.112114.xyz/pp.xml",
        "http://epg.51zmt.top:8000/e.xml",
        "https://raw.githubusercontent.com/fanmingming/live/main/e.xml"
    )

    data class EpgProgram(
        val title: String,
        val startTime: Long,
        val endTime: Long,
        val description: String = ""
    ) {
        fun isCurrent(): Boolean {
            val now = System.currentTimeMillis()
            return now in startTime..endTime
        }

        fun isUpcoming(): Boolean {
            return startTime > System.currentTimeMillis()
        }

        fun getProgress(): Float {
            val now = System.currentTimeMillis()
            if (now < startTime) return 0f
            if (now > endTime) return 1f
            return (now - startTime).toFloat() / (endTime - startTime)
        }

        fun getTimeString(): String {
            val start = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(startTime))
            val end = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(endTime))
            return "$start - $end"
        }
    }

    data class ChannelEpg(
        val channelName: String,
        val channelId: String,
        val programs: List<EpgProgram> = emptyList()
    ) {
        fun getCurrentProgram(): EpgProgram? {
            return programs.find { it.isCurrent() }
        }

        fun getNextProgram(): EpgProgram? {
            return programs.filter { it.isUpcoming() }.minByOrNull { it.startTime }
        }

        fun getTodayPrograms(): List<EpgProgram> {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis

            return programs.filter { it.startTime in startOfDay until endOfDay }
        }
    }

    private var cachedEpg: Map<String, ChannelEpg> = emptyMap()
    private var lastUpdateTime: Long = 0
    private val cacheValidDuration = 30 * 60 * 1000 // 30分钟

    /**
     * 获取频道的EPG信息
     */
    suspend fun getChannelEpg(channel: ChannelEntity): ChannelEpg? = withContext(Dispatchers.IO) {
        // 检查缓存
        if (System.currentTimeMillis() - lastUpdateTime > cacheValidDuration) {
            loadEpgData()
        }

        // 尝试多种方式匹配频道
        val channelKey = findChannelKey(channel.name)
        return@withContext cachedEpg[channelKey]
    }

    /**
     * 获取多个频道的EPG信息
     */
    suspend fun getChannelsEpg(channels: List<ChannelEntity>): Map<String, ChannelEpg> = withContext(Dispatchers.IO) {
        if (System.currentTimeMillis() - lastUpdateTime > cacheValidDuration) {
            loadEpgData()
        }

        return@withContext channels.mapNotNull { channel ->
            val key = findChannelKey(channel.name)
            cachedEpg[key]?.let { channel.name to it }
        }.toMap()
    }

    /**
     * 强制刷新EPG数据
     */
    suspend fun refreshEpg(): Boolean = withContext(Dispatchers.IO) {
        loadEpgData()
        cachedEpg.isNotEmpty()
    }

    private suspend fun loadEpgData() {
        for (epgUrl in epgSources) {
            try {
                val epgData = fetchEpgFromUrl(epgUrl)
                if (epgData.isNotEmpty()) {
                    cachedEpg = epgData
                    lastUpdateTime = System.currentTimeMillis()
                    break
                }
            } catch (e: Exception) {
                continue
            }
        }
    }

    private suspend fun fetchEpgFromUrl(url: String): Map<String, ChannelEpg> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "MOHTV/1.0")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext emptyMap()

        val xmlContent = response.body?.string() ?: return@withContext emptyMap()
        parseEpgXml(xmlContent)
    }

    private fun parseEpgXml(xmlContent: String): Map<String, ChannelEpg> {
        val channelEpgMap = mutableMapOf<String, ChannelEpg>()

        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val inputSource = InputSource(StringReader(xmlContent))
            val document: Document = builder.parse(inputSource)

            // 解析频道信息
            val channelNodes: NodeList = document.getElementsByTagName("channel")
            val channelIdToName = mutableMapOf<String, String>()

            for (i in 0 until channelNodes.length) {
                val channelElement = channelNodes.item(i) as? Element ?: continue
                val id = channelElement.getAttribute("id")
                val nameElement = channelElement.getElementsByTagName("display-name").item(0) as? Element
                val name = nameElement?.textContent ?: id
                channelIdToName[id] = name
            }

            // 解析节目信息
            val programmeNodes: NodeList = document.getElementsByTagName("programme")
            val programMap = mutableMapOf<String, MutableList<EpgProgram>>()

            for (i in 0 until programmeNodes.length) {
                val programmeElement = programmeNodes.item(i) as? Element ?: continue

                val channelId = programmeElement.getAttribute("channel")
                val start = programmeElement.getAttribute("start")
                val stop = programmeElement.getAttribute("stop")

                val titleElement = programmeElement.getElementsByTagName("title").item(0) as? Element
                val title = titleElement?.textContent ?: "未知节目"

                val descElement = programmeElement.getElementsByTagName("desc").item(0) as? Element
                val description = descElement?.textContent ?: ""

                try {
                    val startTime = parseEpgTime(start)
                    val endTime = parseEpgTime(stop)

                    val program = EpgProgram(
                        title = title,
                        startTime = startTime,
                        endTime = endTime,
                        description = description
                    )

                    programMap.getOrPut(channelId) { mutableListOf() }.add(program)
                } catch (e: Exception) {
                    // 跳过解析失败的节目
                }
            }

            // 构建频道EPG映射
            programMap.forEach { (channelId, programs) ->
                val channelName = channelIdToName[channelId] ?: channelId
                val normalizedName = normalizeChannelName(channelName)

                channelEpgMap[normalizedName] = ChannelEpg(
                    channelName = channelName,
                    channelId = channelId,
                    programs = programs.sortedBy { it.startTime }
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return channelEpgMap
    }

    private fun parseEpgTime(timeStr: String): Long {
        // EPG时间格式通常是 yyyyMMddHHmmss +0000
        val cleanTime = timeStr.take(14)
        return dateFormat.parse(cleanTime)?.time ?: 0L
    }

    private fun findChannelKey(channelName: String): String {
        val normalized = normalizeChannelName(channelName)

        // 直接匹配
        if (cachedEpg.containsKey(normalized)) {
            return normalized
        }

        // 模糊匹配
        return cachedEpg.keys.find { key ->
            key.contains(normalized) || normalized.contains(key)
        } ?: normalized
    }

    private fun normalizeChannelName(name: String): String {
        return name
            .replace(Regex("[\\s\\-_.]+"), "")
            .replace(Regex("CCTV[-\\s]?"), "CCTV")
            .replace(Regex("(卫视|电视台)"), "")
            .lowercase(Locale.getDefault())
    }
}
