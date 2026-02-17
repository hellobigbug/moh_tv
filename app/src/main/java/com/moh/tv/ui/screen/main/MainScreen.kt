package com.moh.tv.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moh.tv.data.local.entity.ChannelEntity
import com.moh.tv.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    onChannelClick: (ChannelEntity) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("频道" to Icons.Default.Tv, "收藏" to Icons.Default.Favorite, "最近" to Icons.Default.History)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(
            title = "MOH TV",
            onSettingsClick = onSettingsClick,
            onSyncClick = { viewModel.syncSources() },
            isUpdating = uiState.isUpdating
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = { Icon(icon, contentDescription = title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ChannelTab(
                uiState = uiState,
                onGroupSelect = { viewModel.selectGroup(it) },
                onChannelClick = onChannelClick,
                onFavoriteClick = { viewModel.toggleFavorite(it) }
            )
            1 -> FavoriteTab(
                favorites = uiState.favorites,
                onChannelClick = onChannelClick,
                onFavoriteClick = { viewModel.toggleFavorite(it) }
            )
            2 -> RecentTab(
                recentWatched = uiState.recentWatched,
                onChannelClick = onChannelClick
            )
        }
    }

    LaunchedEffect(uiState.updateMessage) {
        if (uiState.updateMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearUpdateMessage()
        }
    }

    uiState.updateMessage?.let { message ->
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(message)
        }
    }
}

@Composable
fun TopBar(
    title: String,
    onSettingsClick: () -> Unit,
    onSyncClick: () -> Unit,
    isUpdating: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onSyncClick,
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "更新")
                }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "设置")
            }
        }
    }
}

@Composable
fun ChannelTab(
    uiState: com.moh.tv.ui.viewmodel.MainUiState,
    onGroupSelect: (String) -> Unit,
    onChannelClick: (ChannelEntity) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GroupSelector(
            groups = uiState.groups,
            selectedGroup = uiState.selectedGroup,
            onGroupSelect = onGroupSelect
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无频道，请先更新直播源",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            ChannelList(
                channels = uiState.filteredChannels,
                onChannelClick = onChannelClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelector(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelect: (String) -> Unit
) {
    var focusedIndex by remember { mutableStateOf(groups.indexOf(selectedGroup).coerceAtLeast(0)) }
    val listState = rememberLazyListState()

    LaunchedEffect(groups) {
        if (groups.isNotEmpty() && focusedIndex < groups.size) {
            listState.animateScrollToItem(focusedIndex)
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(groups) { group ->
            FilterChip(
                selected = group == selectedGroup,
                onClick = {
                    focusedIndex = groups.indexOf(group)
                    onGroupSelect(group)
                },
                label = { Text(group) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun ChannelList(
    channels: List<ChannelEntity>,
    onChannelClick: (ChannelEntity) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels, key = { it.id }) { channel ->
            ChannelItem(
                channel = channel,
                onClick = { onChannelClick(channel) },
                onFavoriteClick = { onFavoriteClick(channel.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelItem(
    channel: ChannelEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .focusable(isFocused)
            .background(
                if (isFocused) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
            ),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (channel.group.isNotEmpty()) {
                    Text(
                        text = channel.group,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (channel.isFavorite) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (channel.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { isFocused }.collect { /* Track focus changes */ }
    }
}

@Composable
fun FavoriteTab(
    favorites: List<ChannelEntity>,
    onChannelClick: (ChannelEntity) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无收藏",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        ChannelList(
            channels = favorites,
            onChannelClick = onChannelClick,
            onFavoriteClick = onFavoriteClick
        )
    }
}

@Composable
fun RecentTab(
    recentWatched: List<ChannelEntity>,
    onChannelClick: (ChannelEntity) -> Unit
) {
    if (recentWatched.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无观看记录",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentWatched, key = { it.id }) { channel ->
                ChannelItem(
                    channel = channel,
                    onClick = { onChannelClick(channel) },
                    onFavoriteClick = { }
                )
            }
        }
    }
}
