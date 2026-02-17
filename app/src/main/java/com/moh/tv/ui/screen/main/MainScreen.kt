package com.moh.tv.ui.screen.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moh.tv.data.local.entity.ChannelEntity
import com.moh.tv.ui.components.*
import com.moh.tv.ui.theme.AppleTVColors
import com.moh.tv.ui.theme.AppleTVShapes
import com.moh.tv.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onChannelClick: (ChannelEntity) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TabItem("首页", Icons.Default.Home),
        TabItem("频道", Icons.Default.Tv),
        TabItem("收藏", Icons.Default.Favorite),
        TabItem("最近", Icons.Default.History)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                colors = listOf(
                    AppleTVColors.GradientStart,
                    AppleTVColors.GradientEnd
                )
            ))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppleTVTopBar(
                title = "MOH TV",
                onSettingsClick = onSettingsClick,
                onSyncClick = { viewModel.syncSources() },
                isUpdating = uiState.isUpdating
            )
            
            AppleTVTabBar(
                tabs = tabs,
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )
            
            when (selectedTab) {
                0 -> HomeTab(
                    uiState = uiState,
                    onGroupSelect = { viewModel.selectGroup(it) },
                    onChannelClick = onChannelClick,
                    onFavoriteClick = { viewModel.toggleFavorite(it) }
                )
                1 -> ChannelTab(
                    uiState = uiState,
                    onGroupSelect = { viewModel.selectGroup(it) },
                    onChannelClick = onChannelClick,
                    onFavoriteClick = { viewModel.toggleFavorite(it) }
                )
                2 -> FavoriteTab(
                    favorites = uiState.favorites,
                    onChannelClick = onChannelClick,
                    onFavoriteClick = { viewModel.toggleFavorite(it) }
                )
                3 -> RecentTab(
                    recentWatched = uiState.recentWatched,
                    onChannelClick = onChannelClick
                )
            }
        }
        
        uiState.updateMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                containerColor = AppleTVColors.SurfaceElevated,
                contentColor = AppleTVColors.TextPrimary,
                shape = AppleTVShapes.CardMedium
            ) {
                Text(message)
            }
        }
    }
    
    LaunchedEffect(uiState.updateMessage) {
        if (uiState.updateMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearUpdateMessage()
        }
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun AppleTVTopBar(
    title: String,
    onSettingsClick: () -> Unit,
    onSyncClick: () -> Unit,
    isUpdating: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppleTVColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Tv,
                    contentDescription = null,
                    tint = AppleTVColors.OnPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = AppleTVColors.TextPrimary
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppleTVIconButton(
                onClick = onSyncClick,
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AppleTVColors.Primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "更新",
                        tint = AppleTVColors.TextPrimary
                    )
                }
            }
            AppleTVIconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = AppleTVColors.TextPrimary
                )
            }
        }
    }
}

@Composable
fun AppleTVTabBar(
    tabs: List<TabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 8.dp)
            .focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            AppleTVTab(
                tab = tab,
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
fun AppleTVTab(
    tab: TabItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = when {
            isFocused -> 1.08f
            selected -> 1.02f
            else -> 1f
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            selected -> AppleTVColors.Primary
            isFocused -> AppleTVColors.SurfaceElevated
            else -> Color.Transparent
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            selected -> AppleTVColors.OnPrimary
            else -> AppleTVColors.TextSecondary
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "contentColor"
    )
    
    Surface(
        modifier = Modifier
            .scale(scale)
            .focusable()
            .onFocusEvent { focusState ->
                isFocused = focusState.isFocused
            },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (isFocused && !selected) {
            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
        } else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                tab.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = tab.title,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun HomeTab(
    uiState: MainUiState,
    onGroupSelect: (String) -> Unit,
    onChannelClick: (ChannelEntity) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    val scrollState = rememberLazyListState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.favorites.isNotEmpty()) {
            AppleTVSectionHeader(title = "我的收藏")
            ChannelRow(
                channels = uiState.favorites.take(10),
                onChannelClick = onChannelClick,
                onFavoriteClick = onFavoriteClick
            )
        }
        
        if (uiState.recentWatched.isNotEmpty()) {
            AppleTVSectionHeader(title = "最近观看")
            ChannelRow(
                channels = uiState.recentWatched.take(10),
                onChannelClick = onChannelClick,
                onFavoriteClick = onFavoriteClick
            )
        }
        
        AppleTVSectionHeader(
            title = "所有频道",
            action = {
                GroupSelectorCompact(
                    groups = uiState.groups,
                    selectedGroup = uiState.selectedGroup,
                    onGroupSelect = onGroupSelect
                )
            }
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppleTVColors.Primary)
            }
        } else if (uiState.filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Tv,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = AppleTVColors.TextTertiary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无频道，请先更新直播源",
                        color = AppleTVColors.TextTertiary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            ChannelRow(
                channels = uiState.filteredChannels,
                onChannelClick = onChannelClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@Composable
fun ChannelTab(
    uiState: MainUiState,
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
                CircularProgressIndicator(color = AppleTVColors.Primary)
            }
        } else if (uiState.filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Tv,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = AppleTVColors.TextTertiary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无频道，请先更新直播源",
                        color = AppleTVColors.TextTertiary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            ChannelGrid(
                channels = uiState.filteredChannels,
                onChannelClick = onChannelClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@Composable
fun GroupSelector(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groups) { group ->
            AppleTVChip(
                text = group,
                selected = group == selectedGroup,
                onClick = { onGroupSelect(group) }
            )
        }
    }
}

@Composable
fun GroupSelectorCompact(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        AppleTVButton(
            text = selectedGroup,
            onClick = { expanded = true },
            isPrimary = false
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(AppleTVColors.Surface)
                .border(1.dp, AppleTVColors.Border, RoundedCornerShape(12.dp))
        ) {
            groups.forEach { group ->
                DropdownMenuItem(
                    text = {
                        Text(
                            group,
                            color = if (group == selectedGroup) AppleTVColors.Primary else AppleTVColors.TextPrimary
                        )
                    },
                    onClick = {
                        onGroupSelect(group)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ChannelRow(
    channels: List<ChannelEntity>,
    onChannelClick: (ChannelEntity) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(channels, key = { it.id }) { channel ->
            AppleTVChannelCard(
                channel = channel,
                onClick = { onChannelClick(channel) },
                onFavoriteClick = { onFavoriteClick(channel.id) }
            )
        }
    }
}

@Composable
fun ChannelGrid(
    channels: List<ChannelEntity>,
    onChannelClick: (ChannelEntity) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    val rows = channels.chunked(5)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        rows.forEach { rowChannels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                rowChannels.forEach { channel ->
                    AppleTVChannelCard(
                        channel = channel,
                        onClick = { onChannelClick(channel) },
                        onFavoriteClick = { onFavoriteClick(channel.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(5 - rowChannels.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AppleTVChannelCard(
    channel: ChannelEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 16.dp else 4.dp,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "elevation"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) AppleTVColors.SurfaceElevated else AppleTVColors.Surface,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    
    Card(
        modifier = modifier
            .width(280.dp)
            .height(160.dp)
            .scale(scale)
            .shadow(elevation, AppleTVShapes.CardMedium, spotColor = if (isFocused) Color.Black.copy(alpha = 0.5f) else Color.Transparent)
            .focusable()
            .onFocusEvent { focusState ->
                isFocused = focusState.isFocused
            },
        shape = AppleTVShapes.CardMedium,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isFocused) {
            androidx.compose.foundation.BorderStroke(3.dp, AppleTVColors.FocusBorder)
        } else null,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppleTVColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                if (channel.group.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = channel.group,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTVColors.TextTertiary
                    )
                }
            }
            
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.align(Alignment.BottomEnd),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (channel.isFavorite) AppleTVColors.AccentPink.copy(alpha = 0.2f) else Color.Transparent
                )
            ) {
                Icon(
                    imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (channel.isFavorite) AppleTVColors.AccentPink else AppleTVColors.TextTertiary
                )
            }
        }
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
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AppleTVColors.SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = AppleTVColors.TextTertiary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "暂无收藏",
                    color = AppleTVColors.TextSecondary,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击频道卡片上的心形图标添加收藏",
                    color = AppleTVColors.TextTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else {
        AppleTVSectionHeader(title = "我的收藏 (${favorites.size})")
        ChannelGrid(
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
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AppleTVColors.SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = AppleTVColors.TextTertiary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "暂无观看记录",
                    color = AppleTVColors.TextSecondary,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "观看过的频道将显示在这里",
                    color = AppleTVColors.TextTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else {
        AppleTVSectionHeader(title = "最近观看 (${recentWatched.size})")
        ChannelGrid(
            channels = recentWatched,
            onChannelClick = onChannelClick,
            onFavoriteClick = { }
        )
    }
}
