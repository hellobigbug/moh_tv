package com.moh.tv.data.remote

import com.moh.tv.data.local.entity.SourceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubSourceSearcher @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // 预定义的可靠GitHub IPTV源列表
    private val knownReliableSources = listOf(
        SourceEntity(
            name = "IPTV-org 中国",
            url = "https://iptv-org.github.io/iptv/countries/cn.m3u",
            enabled = true,
            autoUpdate = true
        ),
        SourceEntity(
            name = "FanMingMing Live",
            url = "https://raw.githubusercontent.com/fanmingming/live/main/tv/m3u/ipv6.m3u",
            enabled = true,
            autoUpdate = true
        ),
        SourceEntity(
            name = "YanG-1989 聚合",
            url = "https://raw.githubusercontent.com/YanG-1989/m3u/main/Gather.m3u",
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
            name = "IPTV-org 香港",
            url = "https://iptv-org.github.io/iptv/countries/hk.m3u",
            enabled = false,
            autoUpdate = true
        ),
        SourceEntity(
            name = "IPTV-org 台湾",
            url = "https://iptv-org.github.io/iptv/countries/tw.m3u",
            enabled = false,
            autoUpdate = true
        ),
        SourceEntity(
            name = "IPTV-org 日本",
            url = "https://iptv-org.github.io/iptv/countries/jp.m3u",
            enabled = false,
            autoUpdate = true
        ),
        SourceEntity(
            name = "IPTV-org 韩国",
            url = "https://iptv-org.github.io/iptv/countries/kr.m3u",
            enabled = false,
            autoUpdate = true
        ),
        SourceEntity(
            name = "IPTV-org 美国",
            url = "https://iptv-org.github.io/iptv/countries/us.m3u",
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
            name = "GuoXiaoBin 直播",
            url = "https://raw.githubusercontent.com/guoxiaobin2020/live/main/ipv6.m3u",
            enabled = false,
            autoUpdate = true
        )
    )

    /**
     * 获取预定义的可靠源列表
     */
    fun getReliableSources(): List<SourceEntity> = knownReliableSources

    /**
     * 搜索GitHub上的IPTV源
     * 使用GitHub API搜索相关仓库
     */
    suspend fun searchGithubSources(): List<DiscoveredSource> = withContext(Dispatchers.IO) {
        val discovered = mutableListOf<DiscoveredSource>()

        try {
            // 搜索关键词列表
            val searchQueries = listOf(
                "iptv m3u china",
                "直播源 m3u",
                "电视直播 iptv"
            )

            for (query in searchQueries) {
                val results = searchGithubRepositories(query)
                discovered.addAll(results)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 去重并按星标数排序
        discovered.distinctBy { it.url }
            .sortedByDescending { it.stars }
            .take(20) // 只返回前20个
    }

    private fun searchGithubRepositories(query: String): List<DiscoveredSource> {
        val results = mutableListOf<DiscoveredSource>()

        try {
            // 使用GitHub搜索API
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val request = Request.Builder()
                .url("https://api.github.com/search/repositories?q=$encodedQuery&sort=stars&order=desc&per_page=10")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "MOHTV-App")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val items = json.optJSONArray("items") ?: return emptyList()

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val fullName = item.optString("full_name", "")
                val stars = item.optInt("stargazers_count", 0)
                val description = item.optString("description", "")

                // 尝试构建可能的raw文件URL
                val possibleUrls = generatePossibleUrls(fullName)

                results.add(
                    DiscoveredSource(
                        name = fullName,
                        description = description,
                        url = possibleUrls.firstOrNull() ?: "",
                        stars = stars,
                        possibleUrls = possibleUrls
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return results
    }

    private fun generatePossibleUrls(repoFullName: String): List<String> {
        val baseUrl = "https://raw.githubusercontent.com/$repoFullName"

        return listOf(
            "$baseUrl/main/tv.m3u",
            "$baseUrl/main/playlist.m3u",
            "$baseUrl/main/index.m3u",
            "$baseUrl/main/live.m3u",
            "$baseUrl/main/channels.m3u",
            "$baseUrl/master/tv.m3u",
            "$baseUrl/master/playlist.m3u",
            "$baseUrl/master/index.m3u",
            "$baseUrl/master/live.m3u",
            "$baseUrl/master/channels.m3u"
        )
    }

    /**
     * 验证源是否可用
     */
    suspend fun validateSource(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MOHTV/1.0")
                .head() // 使用HEAD请求减少数据传输
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 批量验证源并返回可用的源
     */
    suspend fun validateSources(sources: List<DiscoveredSource>): List<DiscoveredSource> {
        return sources.filter { validateSource(it.url) }
    }
}

data class DiscoveredSource(
    val name: String,
    val description: String,
    val url: String,
    val stars: Int,
    val possibleUrls: List<String> = emptyList()
)
