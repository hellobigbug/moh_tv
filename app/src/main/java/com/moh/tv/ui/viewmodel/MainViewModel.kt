package com.moh.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moh.tv.data.local.entity.ChannelEntity
import com.moh.tv.data.local.entity.SourceEntity
import com.moh.tv.data.remote.SourceSyncManager
import com.moh.tv.data.remote.UpdateResult
import com.moh.tv.data.repository.ChannelRepository
import com.moh.tv.data.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val channels: List<ChannelEntity> = emptyList(),
    val filteredChannels: List<ChannelEntity> = emptyList(),
    val groups: List<String> = emptyList(),
    val selectedGroup: String = "全部",
    val favorites: List<ChannelEntity> = emptyList(),
    val recentWatched: List<ChannelEntity> = emptyList(),
    val sources: List<SourceEntity> = emptyList(),
    val selectedSourceId: Long? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isSearchingSources: Boolean = false,
    val discoveredSources: List<com.moh.tv.data.remote.DiscoveredSource> = emptyList(),
    val error: String? = null,
    val updateMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val sourceRepository: SourceRepository,
    private val sourceSyncManager: SourceSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _selectedGroup = MutableStateFlow("全部")
    val selectedGroup: StateFlow<String> = _selectedGroup.asStateFlow()

    init {
        initializeDefaultSources()
        loadData()
    }

    private fun initializeDefaultSources() {
        viewModelScope.launch {
            val sources = sourceRepository.getAllSources().first()
            if (sources.isEmpty()) {
                sourceRepository.addDefaultSources()
                // 添加默认源后立即同步第一个启用的源
                val newSources = sourceRepository.getEnabledSources().first()
                newSources.firstOrNull()?.let { source ->
                    sourceSyncManager.syncSingleSource(source)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            channelRepository.getAllChannels().collect { channels ->
                _uiState.update { state ->
                    state.copy(
                        channels = channels,
                        filteredChannels = filterChannels(channels, state.selectedGroup),
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            channelRepository.getAllGroups().collect { groups ->
                _uiState.update { it.copy(groups = listOf("全部") + groups) }
            }
        }

        viewModelScope.launch {
            channelRepository.getFavoriteChannels().collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }

        viewModelScope.launch {
            channelRepository.getRecentWatched().collect { recent ->
                _uiState.update { it.copy(recentWatched = recent) }
            }
        }

        viewModelScope.launch {
            sourceRepository.getAllSources().collect { sources ->
                val currentSelectedId = _uiState.value.selectedSourceId
                val shouldAutoSelect = currentSelectedId == null && sources.isNotEmpty()

                _uiState.update { it.copy(sources = sources) }

                if (shouldAutoSelect) {
                    val defaultSource = sources.firstOrNull { it.enabled }
                    if (defaultSource != null) {
                        _uiState.update { it.copy(selectedSourceId = defaultSource.id) }
                        sourceSyncManager.syncSingleSource(defaultSource)
                    }
                }
            }
        }
    }

    fun selectGroup(group: String) {
        _selectedGroup.value = group
        _uiState.update { state ->
            state.copy(
                selectedGroup = group,
                filteredChannels = filterChannels(state.channels, group)
            )
        }
    }

    private fun filterChannels(channels: List<ChannelEntity>, group: String): List<ChannelEntity> {
        return if (group == "全部") {
            channels
        } else {
            channels.filter { it.group == group }
        }
    }

    fun searchChannels(query: String) {
        if (query.isBlank()) {
            _uiState.update { state ->
                state.copy(filteredChannels = filterChannels(state.channels, state.selectedGroup))
            }
            return
        }

        viewModelScope.launch {
            channelRepository.searchChannels(query).collect { channels ->
                _uiState.update { it.copy(filteredChannels = channels) }
            }
        }
    }

    fun toggleFavorite(channelId: Long) {
        viewModelScope.launch {
            channelRepository.toggleFavorite(channelId)
        }
    }

    fun updateWatchTime(channelId: Long) {
        viewModelScope.launch {
            channelRepository.updateWatchTime(channelId)
        }
    }

    fun syncSources() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, updateMessage = null) }

            val result = sourceSyncManager.syncAllSources()

            _uiState.update {
                it.copy(
                    isUpdating = false,
                    updateMessage = if (result.success) {
                        "更新成功: 新增${result.added}个, 移除${result.removed}个"
                    } else {
                        "更新失败: ${result.message}"
                    }
                )
            }
        }
    }

    fun addSource(name: String, url: String, autoUpdate: Boolean = true) {
        viewModelScope.launch {
            val isValid = sourceSyncManager.validateSource(url)
            if (isValid) {
                sourceRepository.addSource(
                    SourceEntity(
                        name = name,
                        url = url,
                        enabled = true,
                        autoUpdate = autoUpdate
                    )
                )
                syncSources()
            } else {
                _uiState.update { it.copy(error = "无效的直播源地址") }
            }
        }
    }

    fun deleteSource(sourceId: Long) {
        viewModelScope.launch {
            sourceRepository.deleteSource(sourceId)
        }
    }

    fun toggleSourceEnabled(sourceId: Long, enabled: Boolean) {
        viewModelScope.launch {
            sourceRepository.toggleSourceEnabled(sourceId, enabled)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearUpdateMessage() {
        _uiState.update { it.copy(updateMessage = null) }
    }

    /**
     * 搜索GitHub上的可用IPTV源
     */
    fun searchGithubSources() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingSources = true, error = null) }

            try {
                val sources = sourceRepository.searchGithubSources()
                _uiState.update {
                    it.copy(
                        isSearchingSources = false,
                        discoveredSources = sources,
                        updateMessage = "发现 ${sources.size} 个潜在源"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSearchingSources = false,
                        error = "搜索失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 添加发现的GitHub源
     */
    fun addDiscoveredSource(source: com.moh.tv.data.remote.DiscoveredSource) {
        viewModelScope.launch {
            val success = sourceRepository.validateAndAddDiscoveredSource(source)
            if (success) {
                _uiState.update {
                    it.copy(updateMessage = "已添加源: ${source.name}")
                }
            } else {
                _uiState.update {
                    it.copy(error = "源验证失败，无法添加")
                }
            }
        }
    }

    fun clearDiscoveredSources() {
        _uiState.update { it.copy(discoveredSources = emptyList()) }
    }

    fun clearAllChannels() {
        viewModelScope.launch {
            channelRepository.deleteAllChannels()
            _uiState.update { 
                it.copy(updateMessage = "已清除所有频道数据") 
            }
        }
    }

    fun selectSource(sourceId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSourceId = sourceId) }
            val source = sourceRepository.getSourceById(sourceId)
            if (source != null && source.enabled) {
                sourceSyncManager.syncSingleSource(source)
            }
        }
    }

    fun autoSelectDefaultSource() {
        viewModelScope.launch {
            val sources = sourceRepository.getEnabledSources().first()
            if (sources.isNotEmpty() && _uiState.value.selectedSourceId == null) {
                val defaultSource = sources.first()
                _uiState.update { it.copy(selectedSourceId = defaultSource.id) }
            }
        }
    }
}
