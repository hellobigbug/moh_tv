package com.moh.tv.data.remote

import android.util.Log
import com.moh.tv.data.model.Channel
import com.moh.tv.data.model.ChannelGroup
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IptvParser @Inject constructor() {

    companion object {
        private const val TAG = "IptvParser"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun parseM3U(url: String): List<Channel> {
        return try {
            Log.d(TAG, "开始解析: $url")
            
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MOHTV/1.0")
                .header("Accept", "*/*")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "请求失败: ${response.code} ${response.message}")
                return emptyList()
            }

            val body = response.body?.string()
            if (body.isNullOrEmpty()) {
                Log.e(TAG, "响应体为空")
                return emptyList()
            }

            Log.d(TAG, "响应大小: ${body.length} 字符")

            val channels = when {
                url.endsWith(".txt", ignoreCase = true) -> parseTxtContent(body)
                body.trimStart().startsWith("#EXTM3U", ignoreCase = true) -> parseM3UContent(body)
                body.contains("#EXTINF:") -> parseM3UContent(body)
                else -> parseTxtContent(body)
            }

            Log.d(TAG, "解析完成，共 ${channels.size} 个频道")
            channels
        } catch (e: Exception) {
            Log.e(TAG, "解析异常: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseM3UContent(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(content))
        var line: String?
        var currentInfo: MutableMap<String, String> = mutableMapOf()

        while (reader.readLine().also { line = it } != null) {
            line = line?.trim() ?: continue

            when {
                line!!.startsWith("#EXTINF:") -> {
                    currentInfo = parseExtInf(line!!)
                }
                line!!.isNotEmpty() && !line!!.startsWith("#") -> {
                    val url = line!!
                    if (url.startsWith("http")) {
                        channels.add(
                            Channel(
                                name = currentInfo["name"] ?: "Unknown",
                                url = url,
                                group = currentInfo["group"] ?: "",
                                logo = currentInfo["logo"] ?: "",
                                epgUrl = currentInfo["epg"] ?: ""
                            )
                        )
                        currentInfo = mutableMapOf()
                    }
                }
            }
        }
        return channels
    }

    private fun parseExtInf(line: String): MutableMap<String, String> {
        val info = mutableMapOf<String, String>()

        val attributes = line.removePrefix("#EXTINF:").split(",")

        if (attributes.size > 1) {
            info["name"] = attributes.last().trim()
        }

        val firstPart = attributes.firstOrNull() ?: return info

        val regex = """(\w+)=["']([^"']*)["']""".toRegex()
        val matches = regex.findAll(firstPart)

        for (match in matches) {
            val (key, value) = match.destructured
            when (key.lowercase()) {
                "tvg-logo", "logo" -> info["logo"] = value
                "group-title", "group" -> info["group"] = value
                "tvg-id" -> info["id"] = value
                "tvg-url" -> info["epg"] = value
            }
        }

        return info
    }

    private fun parseTxtContent(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()
        var currentGroup = "未分类"

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // 检查是否是分组标题（例如：央视台,#genre#）
            if (trimmed.contains("#genre#") || trimmed.contains(",#")) {
                currentGroup = trimmed.substringBefore(",#").substringBefore("#genre#").trim()
                continue
            }

            // 解析频道行（格式：频道名,URL）
            val parts = trimmed.split(",")
            if (parts.size >= 2) {
                val name = parts[0].trim()
                val url = parts[1].trim()

                if (url.startsWith("http")) {
                    channels.add(
                        Channel(
                            name = name,
                            url = url,
                            group = currentGroup,
                            logo = "",
                            epgUrl = ""
                        )
                    )
                }
            }
        }
        return channels
    }

    fun groupChannels(channels: List<Channel>): List<ChannelGroup> {
        return channels
            .groupBy { it.group.ifEmpty { "未分类" } }
            .map { (group, list) ->
                ChannelGroup(
                    name = group,
                    channels = list.sortedBy { it.name }
                )
            }
            .sortedBy { it.name }
    }
}
