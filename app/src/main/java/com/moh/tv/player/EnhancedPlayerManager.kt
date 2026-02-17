package com.moh.tv.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class EnhancedPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isLoading: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val currentQuality: VideoQuality = VideoQuality.AUTO,
    val availableQualities: List<VideoQuality> = emptyList(),
    val error: String? = null,
    val retryCount: Int = 0,
    val bandwidth: Long = 0L,
    val isLive: Boolean = true
)

data class VideoQuality(
    val name: String,
    val width: Int,
    val height: Int,
    val bitrate: Long
) {
    companion object {
        val AUTO = VideoQuality("自动", 0, 0, 0)
        val SD = VideoQuality("标清", 640, 480, 800000)
        val HD = VideoQuality("高清", 1280, 720, 2000000)
        val FHD = VideoQuality("全高清", 1920, 1080, 4000000)
        val UHD = VideoQuality("4K超清", 3840, 2160, 8000000)

        val allQualities = listOf(AUTO, SD, HD, FHD, UHD)
    }
}

@Singleton
class EnhancedPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _playerState = MutableStateFlow(EnhancedPlayerState())
    val playerState: StateFlow<EnhancedPlayerState> = _playerState.asStateFlow()

    private var currentChannelId: Long = -1
    private var currentUrl: String = ""
    private var retryJob: Job? = null
    private var positionUpdateJob: Job? = null

    // 重试配置
    private val maxRetries = 3
    private val retryDelayMs = 3000L

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                    updateState { copy(isBuffering = false, isLoading = false) }
                }
                Player.STATE_BUFFERING -> {
                    updateState { copy(isBuffering = true, isLoading = true) }
                }
                Player.STATE_READY -> {
                    updateState {
                        copy(
                            isBuffering = false,
                            isLoading = false,
                            retryCount = 0,
                            error = null,
                            duration = exoPlayer?.duration ?: 0L
                        )
                    }
                    updateAvailableQualities()
                }
                Player.STATE_ENDED -> {
                    updateState { copy(isPlaying = false) }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState { copy(isPlaying = isPlaying) }
        }

        override fun onPlayerError(error: PlaybackException) {
            handlePlaybackError(error)
        }

        override fun onTracksChanged(tracks: Tracks) {
            updateAvailableQualities()
        }
    }

    @OptIn(UnstableApi::class)
    fun initialize() {
        if (exoPlayer != null) return

        // 配置OkHttp客户端，优化网络性能
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setMaxVideoSizeSd()
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
                    .setAllowVideoNonSeamlessAdaptiveness(true)
            )
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        // 使用OkHttp数据源工厂
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)

        exoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector!!)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory as androidx.media3.datasource.DataSource.Factory))
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build().apply {
                addListener(playerListener)
                playWhenReady = true
            }

        // 启动位置更新任务
        startPositionUpdates()
    }

    fun play(channelId: Long, url: String, channelName: String = "") {
        currentChannelId = channelId
        currentUrl = url

        // 取消之前的重试任务
        retryJob?.cancel()

        initialize()

        updateState { EnhancedPlayerState(isLoading = true) }

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaId(channelId.toString())
            .build()

        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    /**
     * 处理播放错误，自动重试
     */
    private fun handlePlaybackError(error: PlaybackException) {
        val currentRetryCount = _playerState.value.retryCount

        if (currentRetryCount < maxRetries) {
            updateState {
                copy(
                    error = "连接失败，${retryDelayMs / 1000}秒后重试... (${currentRetryCount + 1}/$maxRetries)",
                    retryCount = currentRetryCount + 1,
                    isBuffering = false
                )
            }

            retryJob = scope.launch {
                delay(retryDelayMs)
                if (isActive) {
                    retry()
                }
            }
        } else {
            updateState {
                copy(
                    error = "播放失败: ${error.errorCodeName}，请尝试切换其他频道",
                    isBuffering = false,
                    isLoading = false
                )
            }
        }
    }

    fun retry() {
        if (currentUrl.isNotEmpty()) {
            play(currentChannelId, currentUrl)
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) pause() else play()
        }
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun seekForward(ms: Long = 10000) {
        exoPlayer?.let {
            val newPosition = (it.currentPosition + ms).coerceAtMost(it.duration)
            it.seekTo(newPosition)
        }
    }

    fun seekBack(ms: Long = 10000) {
        exoPlayer?.let {
            val newPosition = (it.currentPosition - ms).coerceAtLeast(0)
            it.seekTo(newPosition)
        }
    }

    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0f, 1f)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        updateState { copy(playbackSpeed = speed) }
    }

    /**
     * 设置视频清晰度
     */
    @OptIn(UnstableApi::class)
    fun setQuality(quality: VideoQuality) {
        trackSelector?.let { selector ->
            val params = when (quality) {
                VideoQuality.AUTO -> {
                    selector.buildUponParameters()
                        .clearVideoSizeConstraints()
                        .setAllowVideoMixedMimeTypeAdaptiveness(true)
                }
                else -> {
                    selector.buildUponParameters()
                        .setMaxVideoSize(quality.width, quality.height)
                        .setMinVideoSize(quality.width / 2, quality.height / 2)
                }
            }
            selector.setParameters(params)
            updateState { copy(currentQuality = quality) }
        }
    }

    /**
     * 更新可用清晰度列表
     */
    @OptIn(UnstableApi::class)
    private fun updateAvailableQualities() {
        exoPlayer?.let { player ->
            val tracks = player.currentTracks
            val videoQualities = mutableListOf<VideoQuality>()

            // 始终添加自动选项
            videoQualities.add(VideoQuality.AUTO)

            // 从轨道信息中提取可用清晰度
            tracks.groups.forEach { group ->
                if (group.type == C.TRACK_TYPE_VIDEO) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        format.width.takeIf { it > 0 }?.let { width ->
                            val height = format.height
                            val quality = when {
                                height >= 2160 -> VideoQuality.UHD
                                height >= 1080 -> VideoQuality.FHD
                                height >= 720 -> VideoQuality.HD
                                else -> VideoQuality.SD
                            }
                            if (!videoQualities.contains(quality)) {
                                videoQualities.add(quality)
                            }
                        }
                    }
                }
            }

            updateState { copy(availableQualities = videoQualities.sortedBy { it.bitrate }) }
        }
    }

    /**
     * 切换到下一个可用清晰度
     */
    fun switchToNextQuality() {
        val current = _playerState.value.currentQuality
        val available = _playerState.value.availableQualities

        if (available.size > 1) {
            val currentIndex = available.indexOf(current)
            val nextIndex = (currentIndex + 1) % available.size
            setQuality(available[nextIndex])
        }
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                updatePosition()
                delay(1000)
            }
        }
    }

    private fun updatePosition() {
        exoPlayer?.let {
            updateState {
                copy(
                    currentPosition = it.currentPosition,
                    duration = it.duration.coerceAtLeast(0)
                )
            }
        }
    }

    private fun updateState(update: EnhancedPlayerState.() -> EnhancedPlayerState) {
        _playerState.value = _playerState.value.update()
    }

    fun release() {
        retryJob?.cancel()
        positionUpdateJob?.cancel()
        scope.cancel()

        exoPlayer?.apply {
            removeListener(playerListener)
            release()
        }
        exoPlayer = null
        trackSelector = null
        _playerState.value = EnhancedPlayerState()
    }
}
