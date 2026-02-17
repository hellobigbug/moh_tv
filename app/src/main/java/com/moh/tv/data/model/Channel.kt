package com.moh.tv.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String,
    val url: String,
    @SerialName("group")
    val group: String = "",
    val logo: String = "",
    @SerialName("epg")
    val epgUrl: String = "",
    val quality: Int = 0,
    val status: Int = 1
)

@Serializable
data class ChannelGroup(
    val name: String,
    val channels: List<Channel> = emptyList()
)

@Serializable
data class IptvSource(
    val name: String,
    val url: String,
    val enabled: Boolean = true,
    val autoUpdate: Boolean = true,
    val updateInterval: Long = 24 * 60 * 60 * 1000L
)

@Serializable
data class IptvSourceConfig(
    val sources: List<IptvSource> = emptyList(),
    val lastUpdate: Long = 0L
)

@Serializable
data class UpdateLog(
    val timestamp: Long,
    val added: Int = 0,
    val removed: Int = 0,
    val updated: Int = 0,
    val message: String = ""
)
