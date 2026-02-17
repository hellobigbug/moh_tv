package com.moh.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moh.tv.data.local.entity.ChannelEntity
import com.moh.tv.data.remote.EpgManager
import com.moh.tv.data.repository.ChannelRepository
import com.moh.tv.player.EnhancedPlayerManager
import com.moh.tv.player.EnhancedPlayerState
import com.moh.tv.player.VideoQuality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnhancedPlayerUiState(
    val currentChannel: ChannelEntity? = null,
    val nextChannel: ChannelEntity? = null,
    val prevChannel: ChannelEntity? = null,
    val playerState: EnhancedPlayerState = EnhancedPlayerState(),
    val showControls: Boolean = true,
    val showQualityMenu: Boolean = false,
    val showError: Boolean = false,
    val showChannelInfo: Boolean = false,
    val showNumberPad: Boolean = false,
    val showQuickSearch: Boolean = false,
    val errorMessage: String? = null,
    val currentProgram: String? = null,
    val nextProgram: String? = null,
    val programProgress: Float = 0f,
    val inputNumber: String = "",
    val searchQuery: String = "",
    val allChannels: List<ChannelEntity> = emptyList(),
    val filteredChannels: List<ChannelEntity> = emptyList()
)

@HiltViewModel
class EnhancedPlayerViewModel @Inject constructor(
    private val playerManager: EnhancedPlayerManager,
    private val channelRepository: ChannelRepository,
    private val epgManager: EpgManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedPlayerUiState())
    val uiState: StateFlow<EnhancedPlayerUiState> = _uiState.asStateFlow()

    private var controlsHideJob: Job? = null
    private var channelInfoHideJob: Job? = null
    private var epgUpdateJob: Job? = null
    private var currentIndex: Int = -1

    init {
        // 收集播放器状态
        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                _uiState.update { it.copy(playerState = state) }

                if (state.error != null && !state.error.contains("重试")) {
                    _uiState.update { it.copy(showError = true, errorMessage = state.error) }
                }
            }
        }

        // 加载所有频道
        viewModelScope.launch {
            channelRepository.getAllChannels().collect { channels ->
                _uiState.update {
                    it.copy(
                        allChannels = channels,
                        filteredChannels = channels
                    )
                }
            }
        }

        // 启动EPG更新
        startEpgUpdate()
    }

    fun playChannel(channel: ChannelEntity) {
        val channels = _uiState.value.allChannels
        val index = channels.indexOfFirst { it.id == channel.id }
        currentIndex = index

        val prevChannel = if (index > 0) channels.getOrNull(index - 1) else null
        val nextChannel = if (index < channels.size - 1) channels.getOrNull(index + 1) else null

        _uiState.update {
            it.copy(
                currentChannel = channel,
                prevChannel = prevChannel,
                nextChannel = nextChannel,
                showError = false,
                errorMessage = null,
                inputNumber = ""
            )
        }

        playerManager.play(channel.id, channel.url, channel.name)

        viewModelScope.launch {
            channelRepository.updateWatchTime(channel.id)
            updateEpgInfo(channel)
        }

        showChannelInfo()
        showControlsTemporarily()
    }

    /**
     * 通过数字键选台
     */
    fun inputNumber(number: Int) {
        when {
            number == -1 -> {
                // 清除
                _uiState.update { it.copy(inputNumber = "") }
            }
            number >= 0 && number <= 9 -> {
                val current = _uiState.value.inputNumber
                if (current.length < 3) {
                    _uiState.update { it.copy(inputNumber = current + number) }
                }
            }
        }
    }

    /**
     * 确认数字选台
     */
    fun confirmNumberSelection() {
        val number = _uiState.value.inputNumber.toIntOrNull() ?: return
        val channels = _uiState.value.allChannels

        // 支持频道号直接跳转
        if (number in 1..channels.size) {
            channels.getOrNull(number - 1)?.let { channel ->
                playChannel(channel)
                hideNumberPad()
            }
        }
    }

    /**
     * 快速搜索
     */
    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredChannels = if (query.isEmpty()) {
                    it.allChannels
                } else {
                    it.allChannels.filter { channel ->
                        channel.name.contains(query, ignoreCase = true)
                    }
                }
            )
        }
    }

    fun selectSearchedChannel(channel: ChannelEntity) {
        playChannel(channel)
        hideQuickSearch()
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

    fun setQuality(quality: VideoQuality) {
        playerManager.setQuality(quality)
        showControlsTemporarily()
    }

    fun switchToNextQuality() {
        playerManager.switchToNextQuality()
        showControlsTemporarily()
    }

    fun playNextChannel() {
        _uiState.value.nextChannel?.let { playChannel(it) }
    }

    fun playPrevChannel() {
        _uiState.value.prevChannel?.let { playChannel(it) }
    }

    // UI控制
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

    fun showNumberPad() {
        _uiState.update { it.copy(showNumberPad = true, inputNumber = "") }
    }

    fun hideNumberPad() {
        _uiState.update { it.copy(showNumberPad = false) }
    }

    fun showQuickSearch() {
        _uiState.update { it.copy(showQuickSearch = true, searchQuery = "") }
    }

    fun hideQuickSearch() {
        _uiState.update { it.copy(showQuickSearch = false) }
    }

    private fun showChannelInfo() {
        channelInfoHideJob?.cancel()
        _uiState.update { it.copy(showChannelInfo = true) }

        channelInfoHideJob = viewModelScope.launch {
            delay(5000)
            _uiState.update { it.copy(showChannelInfo = false) }
        }
    }

    private fun showControlsTemporarily() {
        controlsHideJob?.cancel()
        _uiState.update { it.copy(showControls = true) }

        controlsHideJob = viewModelScope.launch {
            delay(5000)
            if (_uiState.value.playerState.isPlaying && !_uiState.value.showQualityMenu) {
                _uiState.update { it.copy(showControls = false) }
            }
        }
    }

    fun retry() {
        _uiState.value.currentChannel?.let { channel ->
            _uiState.update { it.copy(showError = false, errorMessage = null) }
            playerManager.retry()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(showError = false, errorMessage = null) }
    }

    /**
     * 更新EPG信息
     */
    private suspend fun updateEpgInfo(channel: ChannelEntity) {
        try {
            val channelEpg = epgManager.getChannelEpg(channel)
            channelEpg?.let { epg ->
                val current = epg.getCurrentProgram()
                val next = epg.getNextProgram()

                _uiState.update {
                    it.copy(
                        currentProgram = current?.title,
                        nextProgram = next?.let { "${it.getTimeString()} ${it.title}" },
                        programProgress = current?.getProgress() ?: 0f
                    )
                }
            }
        } catch (e: Exception) {
            // EPG获取失败不影响播放
        }
    }

    /**
     * 启动EPG定时更新
     */
    private fun startEpgUpdate() {
        epgUpdateJob?.cancel()
        epgUpdateJob = viewModelScope.launch {
            while (isActive) {
                _uiState.value.currentChannel?.let { channel ->
                    updateEpgInfo(channel)
                }
                delay(60000) // 每分钟更新一次
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        controlsHideJob?.cancel()
        channelInfoHideJob?.cancel()
        epgUpdateJob?.cancel()
    }

    fun releasePlayer() {
        playerManager.release()
    }
}
