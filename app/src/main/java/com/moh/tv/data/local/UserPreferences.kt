package com.moh.tv.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.moh.tv.player.VideoQuality
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // 偏好设置键
    companion object {
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
        val AUTO_PLAY_ON_STARTUP = booleanPreferencesKey("auto_play_on_startup")
        val SHOW_CHANNEL_LOGO = booleanPreferencesKey("show_channel_logo")
        val ENABLE_EPG = booleanPreferencesKey("enable_epg")
        val REMEMBER_LAST_CHANNEL = booleanPreferencesKey("remember_last_channel")
        val LAST_CHANNEL_ID = longPreferencesKey("last_channel_id")
        val VOLUME_LEVEL = floatPreferencesKey("volume_level")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CHANNEL_SORT_ORDER = stringPreferencesKey("channel_sort_order")
        val AUTO_UPDATE_ENABLED = booleanPreferencesKey("auto_update_enabled")
        val UPDATE_INTERVAL_HOURS = intPreferencesKey("update_interval_hours")
        val HARDWARE_ACCELERATION = booleanPreferencesKey("hardware_acceleration")
        val BUFFER_SIZE = intPreferencesKey("buffer_size_mb")
        val SHOW_CURRENT_PROGRAM = booleanPreferencesKey("show_current_program")
        val AUTO_HIDE_CONTROLS = booleanPreferencesKey("auto_hide_controls")
        val CONTROLS_TIMEOUT_SECONDS = intPreferencesKey("controls_timeout_seconds")
        val ENABLE_CHANNEL_NUMBER_SHORTCUT = booleanPreferencesKey("enable_channel_number_shortcut")
        val ENABLE_SWIPE_TO_CHANGE_CHANNEL = booleanPreferencesKey("enable_swipe_to_change_channel")
        val SHOW_WATCH_HISTORY = booleanPreferencesKey("show_watch_history")
        val MAX_HISTORY_ITEMS = intPreferencesKey("max_history_items")
        val ENABLE_SMART_RECOMMEND = booleanPreferencesKey("enable_smart_recommend")
        val LAST_PLAYBACK_POSITION = longPreferencesKey("last_playback_position")
    }

    // 默认设置
    data class Settings(
        val defaultQuality: VideoQuality = VideoQuality.AUTO,
        val autoPlayOnStartup: Boolean = true,
        val showChannelLogo: Boolean = true,
        val enableEpg: Boolean = true,
        val rememberLastChannel: Boolean = true,
        val lastChannelId: Long = -1,
        val volumeLevel: Float = 1.0f,
        val playbackSpeed: Float = 1.0f,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val channelSortOrder: SortOrder = SortOrder.DEFAULT,
        val autoUpdateEnabled: Boolean = true,
        val updateIntervalHours: Int = 24,
        val hardwareAcceleration: Boolean = true,
        val bufferSizeMb: Int = 32
    )

    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }

    enum class SortOrder {
        DEFAULT, NAME_ASC, NAME_DESC, GROUP
    }

    // 获取所有设置
    val settings: Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
            defaultQuality = VideoQuality.allQualities.find {
                it.name == preferences[DEFAULT_QUALITY]
            } ?: VideoQuality.AUTO,
            autoPlayOnStartup = preferences[AUTO_PLAY_ON_STARTUP] ?: true,
            showChannelLogo = preferences[SHOW_CHANNEL_LOGO] ?: true,
            enableEpg = preferences[ENABLE_EPG] ?: true,
            rememberLastChannel = preferences[REMEMBER_LAST_CHANNEL] ?: true,
            lastChannelId = preferences[LAST_CHANNEL_ID] ?: -1,
            volumeLevel = preferences[VOLUME_LEVEL] ?: 1.0f,
            playbackSpeed = preferences[PLAYBACK_SPEED] ?: 1.0f,
            themeMode = ThemeMode.valueOf(
                preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name
            ),
            channelSortOrder = SortOrder.valueOf(
                preferences[CHANNEL_SORT_ORDER] ?: SortOrder.DEFAULT.name
            ),
            autoUpdateEnabled = preferences[AUTO_UPDATE_ENABLED] ?: true,
            updateIntervalHours = preferences[UPDATE_INTERVAL_HOURS] ?: 24,
            hardwareAcceleration = preferences[HARDWARE_ACCELERATION] ?: true,
            bufferSizeMb = preferences[BUFFER_SIZE] ?: 32
        )
    }

    // 更新设置的方法
    suspend fun updateDefaultQuality(quality: VideoQuality) {
        dataStore.edit { it[DEFAULT_QUALITY] = quality.name }
    }

    suspend fun updateAutoPlayOnStartup(enabled: Boolean) {
        dataStore.edit { it[AUTO_PLAY_ON_STARTUP] = enabled }
    }

    suspend fun updateShowChannelLogo(enabled: Boolean) {
        dataStore.edit { it[SHOW_CHANNEL_LOGO] = enabled }
    }

    suspend fun updateEnableEpg(enabled: Boolean) {
        dataStore.edit { it[ENABLE_EPG] = enabled }
    }

    suspend fun updateRememberLastChannel(enabled: Boolean) {
        dataStore.edit { it[REMEMBER_LAST_CHANNEL] = enabled }
    }

    suspend fun updateLastChannelId(channelId: Long) {
        dataStore.edit { it[LAST_CHANNEL_ID] = channelId }
    }

    suspend fun updateVolumeLevel(volume: Float) {
        dataStore.edit { it[VOLUME_LEVEL] = volume.coerceIn(0f, 1f) }
    }

    suspend fun updatePlaybackSpeed(speed: Float) {
        dataStore.edit { it[PLAYBACK_SPEED] = speed }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun updateChannelSortOrder(order: SortOrder) {
        dataStore.edit { it[CHANNEL_SORT_ORDER] = order.name }
    }

    suspend fun updateAutoUpdateEnabled(enabled: Boolean) {
        dataStore.edit { it[AUTO_UPDATE_ENABLED] = enabled }
    }

    suspend fun updateUpdateIntervalHours(hours: Int) {
        dataStore.edit { it[UPDATE_INTERVAL_HOURS] = hours.coerceIn(1, 168) }
    }

    suspend fun updateHardwareAcceleration(enabled: Boolean) {
        dataStore.edit { it[HARDWARE_ACCELERATION] = enabled }
    }

    suspend fun updateBufferSize(mb: Int) {
        dataStore.edit { it[BUFFER_SIZE] = mb.coerceIn(8, 128) }
    }

    // 重置所有设置
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
