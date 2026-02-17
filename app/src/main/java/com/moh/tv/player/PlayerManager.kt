package com.moh.tv.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val currentQuality: Int = -1,
    val availableQualities: List<Int> = emptyList(),
    val error: String? = null
)

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var currentChannelId: Long = -1
    private var currentUrl: String = ""

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updateState {
                copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    isPlaying = exoPlayer?.isPlaying == true,
                    duration = exoPlayer?.duration ?: 0L
                )
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState { copy(isPlaying = isPlaying) }
        }

        override fun onPlayerError(error: PlaybackException) {
            updateState {
                copy(
                    error = error.message ?: "播放错误",
                    isPlaying = false,
                    isBuffering = false
                )
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updateState { copy(currentPosition = newPosition.positionMs) }
        }
    }

    @OptIn(UnstableApi::class)
    fun initialize() {
        if (exoPlayer != null) return

        trackSelector = DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector!!)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context))
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build().apply {
                addListener(playerListener)
                playWhenReady = true
            }
    }

    fun play(channelId: Long, url: String) {
        currentChannelId = channelId
        currentUrl = url

        initialize()

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .build()

        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }

        updateState { PlayerState() }
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

    fun setQuality(quality: Int) {
        trackSelector?.let { selector ->
            val params = selector.buildUponParameters()
            if (quality == -1) {
                params.clearVideoSizeConstraints()
            } else {
                params.setMaxVideoSize(quality, quality)
            }
            selector.setParameters(params)
            updateState { copy(currentQuality = quality) }
        }
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun updatePosition() {
        exoPlayer?.let {
            updateState {
                copy(
                    currentPosition = it.currentPosition,
                    duration = it.duration.coerceAtLeast(0)
                )
            }
        }
    }

    fun release() {
        exoPlayer?.apply {
            removeListener(playerListener)
            release()
        }
        exoPlayer = null
        trackSelector = null
        updateState { PlayerState() }
    }

    private fun updateState(update: PlayerState.() -> PlayerState) {
        _playerState.value = _playerState.value.update()
    }
}
