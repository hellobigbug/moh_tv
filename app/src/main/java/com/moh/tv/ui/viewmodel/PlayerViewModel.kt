package com.moh.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moh.tv.data.local.entity.ChannelEntity
import com.moh.tv.data.repository.ChannelRepository
import com.moh.tv.player.PlayerManager
import com.moh.tv.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val currentChannel: ChannelEntity? = null,
    val nextChannel: ChannelEntity? = null,
    val prevChannel: ChannelEntity? = null,
    val playerState: PlayerState = PlayerState(),
    val showControls: Boolean = true,
    val showQualityMenu: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val channelRepository: ChannelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var positionUpdateJob: Job? = null
    private var controlsHideJob: Job? = null
    private var allChannels: List<ChannelEntity> = emptyList()
    private var currentIndex: Int = -1

    init {
        viewModelScope.launch {
            channelRepository.getAllChannels().collect { channels ->
                allChannels = channels
            }
        }

        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                _uiState.update { it.copy(playerState = state) }

                if (state.error != null) {
                    _uiState.update { it.copy(showError = true, errorMessage = state.error) }
                }
            }
        }
    }

    fun playChannel(channel: ChannelEntity) {
        val index = allChannels.indexOfFirst { it.id == channel.id }
        currentIndex = index

        val prevChannel = if (index > 0) allChannels.getOrNull(index - 1) else null
        val nextChannel = if (index < allChannels.size - 1) allChannels.getOrNull(index + 1) else null

        _uiState.update {
            it.copy(
                currentChannel = channel,
                prevChannel = prevChannel,
                nextChannel = nextChannel,
                showError = false,
                errorMessage = null
            )
        }

        playerManager.play(channel.id, channel.url)
        viewModelScope.launch {
            channelRepository.updateWatchTime(channel.id)
        }

        startPositionUpdate()
        showControlsTemporarily()
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
        showControlsTemporarily()
    }

    fun play() {
        playerManager.play()
    }

    fun pause() {
        playerManager.pause()
    }

    fun seekTo(position: Long) {
        playerManager.seekTo(position)
    }

    fun seekForward() {
        playerManager.seekForward()
        showControlsTemporarily()
    }

    fun seekBack() {
        playerManager.seekBack()
        showControlsTemporarily()
    }

    fun setVolume(volume: Float) {
        playerManager.setVolume(volume)
        showControlsTemporarily()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
        showControlsTemporarily()
    }

    fun setQuality(quality: Int) {
        playerManager.setQuality(quality)
        showControlsTemporarily()
    }

    fun playNextChannel() {
        _uiState.value.nextChannel?.let { playChannel(it) }
    }

    fun playPrevChannel() {
        _uiState.value.prevChannel?.let { playChannel(it) }
    }

    fun showControls() {
        _uiState.update { it.copy(showControls = true) }
        showControlsTemporarily()
    }

    fun hideControls() {
        _uiState.update { it.copy(showControls = false) }
    }

    fun toggleControls() {
        _uiState.update { it.copy(showControls = !it.showControls) }
        if (_uiState.value.showControls) {
            showControlsTemporarily()
        }
    }

    fun showQualityMenu() {
        _uiState.update { it.copy(showQualityMenu = true) }
    }

    fun hideQualityMenu() {
        _uiState.update { it.copy(showQualityMenu = false) }
    }

    fun retry() {
        _uiState.value.currentChannel?.let { channel ->
            _uiState.update { it.copy(showError = false, errorMessage = null) }
            playerManager.play(channel.id, channel.url)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(showError = false, errorMessage = null) }
    }

    private fun showControlsTemporarily() {
        controlsHideJob?.cancel()
        _uiState.update { it.copy(showControls = true) }

        controlsHideJob = viewModelScope.launch {
            delay(5000)
            if (_uiState.value.playerState.isPlaying) {
                _uiState.update { it.copy(showControls = false) }
            }
        }
    }

    private fun startPositionUpdate() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                playerManager.updatePosition()
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        positionUpdateJob?.cancel()
        controlsHideJob?.cancel()
    }

    fun releasePlayer() {
        positionUpdateJob?.cancel()
        controlsHideJob?.cancel()
        playerManager.release()
    }
}
