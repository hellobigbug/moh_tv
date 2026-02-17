package com.moh.tv.data.remote

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

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun parseM3U(url: String): List<Channel> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MOHTV/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return emptyList()
            }

            val body = response.body?.string() ?: return emptyList()
            parseM3UContent(body)
        } catch (e: Exception) {
            e.printStackTrace()
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
