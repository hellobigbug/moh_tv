package com.moh.tv.data.remote

import com.moh.tv.data.local.entity.SourceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoSourceDetector @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    data class SourceTestResult(
        val source: SourceEntity,
        val isAccessible: Boolean,
        val responseTime: Long,
        val channelCount: Int,
        val score: Double
    )

    /**
     * 自动检测并选择最佳源
     * 返回按质量排序的源列表
     */
    suspend fun detectBestSources(sources: List<SourceEntity>): List<SourceTestResult> = withContext(Dispatchers.IO) {
        val results = sources.map { source ->
            async {
                testSource(source)
            }
        }.awaitAll()

        // 按综合得分排序（可访问性 + 频道数量 + 响应速度）
        results.filter { it.isAccessible }
            .sortedByDescending { it.score }
    }

    /**
     * 测试单个源的质量
     */
    private suspend fun testSource(source: SourceEntity): SourceTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var isAccessible = false
        var channelCount = 0
        var responseTime = Long.MAX_VALUE

        try {
            val request = Request.Builder()
                .url(source.url)
                .header("User-Agent", "MOHTV/1.0")
                .build()

            val response = client.newCall(request).execute()
            responseTime = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                isAccessible = true

                // 估算频道数量
                channelCount = when {
                    body.contains("#EXTINF:") -> body.split("#EXTINF:").size - 1
                    body.contains("http") -> body.lines().count { it.trim().startsWith("http") }
                    else -> 0
                }
            }
        } catch (e: Exception) {
            // 测试失败
        }

        // 计算综合得分（响应时间越快、频道越多得分越高）
        val score = if (isAccessible) {
            val speedScore = (10000 - responseTime.coerceAtMost(10000)) / 100.0 * 0.3
            val channelScore = (channelCount.coerceAtMost(1000) / 1000.0) * 100 * 0.7
            speedScore + channelScore
        } else 0.0

        SourceTestResult(
            source = source,
            isAccessible = isAccessible,
            responseTime = responseTime,
            channelCount = channelCount,
            score = score
        )
    }

    /**
     * 获取推荐的最佳源（开箱即用）
     */
    suspend fun getRecommendedSource(sources: List<SourceEntity>): SourceEntity? {
        val tested = detectBestSources(sources)
        return tested.firstOrNull()?.source
    }

    /**
     * 快速检查源是否可用
     */
    suspend fun quickCheck(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MOHTV/1.0")
                .head()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
