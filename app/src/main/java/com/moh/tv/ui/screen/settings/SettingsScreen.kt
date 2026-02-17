package com.moh.tv.ui.screen.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.moh.tv.data.local.entity.SourceEntity
import com.moh.tv.ui.theme.AppleTVColors
import com.moh.tv.ui.theme.AppleTVShapes
import com.moh.tv.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSourceDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<SourceEntity?>(null) }
    var showQRScannerDialog by remember { mutableStateOf(false) }
    var showQRGeneratorDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleTVColors.Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppleTVSettingsHeader(
                title = "设置",
                onBack = onBack
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    AppleTVSettingsSection(title = "直播源管理") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "当前源数量: ${uiState.sources.size}",
                                    color = AppleTVColors.TextSecondary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AppleTVSettingsButton(
                                        text = "生成配置码",
                                        icon = Icons.Default.QrCode,
                                        onClick = { showQRGeneratorDialog = true }
                                    )
                                    AppleTVSettingsButton(
                                        text = "扫码添加",
                                        icon = Icons.Default.QrCodeScanner,
                                        onClick = { showQRScannerDialog = true }
                                    )
                                    AppleTVSettingsButton(
                                        text = "手动添加",
                                        icon = Icons.Default.Add,
                                        onClick = { showAddSourceDialog = true }
                                    )
                                }
                            }
                        }
                    }
                }
                
                items(uiState.sources, key = { it.id }) { source ->
                    AppleTVSourceItem(
                        source = source,
                        isSelected = uiState.selectedSourceId == source.id,
                        onSelect = { viewModel.selectSource(source.id) },
                        onToggle = { viewModel.toggleSourceEnabled(source.id, !source.enabled) },
                        onDelete = { showDeleteConfirmDialog = source }
                    )
                }
                
                item {
                    AppleTVSettingsSection(title = "数据管理") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AppleTVSettingsActionCard(
                                title = "手动更新所有源",
                                subtitle = "从所有启用的直播源获取最新频道列表",
                                icon = Icons.Default.Refresh,
                                isLoading = uiState.isUpdating,
                                onClick = { viewModel.syncSources() }
                            )
                            
                            AppleTVSettingsActionCard(
                                title = "清除所有频道数据",
                                subtitle = "删除所有已保存的频道信息",
                                icon = Icons.Default.DeleteSweep,
                                iconTint = AppleTVColors.Error,
                                onClick = { viewModel.clearAllChannels() }
                            )
                        }
                    }
                }
                
                item {
                    AppleTVSettingsSection(title = "内置直播源") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            BuiltInSourceItem(
                                name = "IPTV-org 中国频道",
                                description = "全球公开IPTV频道集合 - 中国区",
                                sourceUrl = "https://iptv-org.github.io/iptv/countries/cn.m3u",
                                onAdd = { viewModel.addSource("IPTV-org 中国", it) }
                            )
                            BuiltInSourceItem(
                                name = "IPTV-org 全球频道",
                                description = "全球公开IPTV频道集合 - 完整版",
                                sourceUrl = "https://iptv-org.github.io/iptv/index.m3u",
                                onAdd = { viewModel.addSource("IPTV-org 全球", it) }
                            )
                            BuiltInSourceItem(
                                name = "Free-TV 全球频道",
                                description = "免费电视频道M3U播放列表",
                                sourceUrl = "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u",
                                onAdd = { viewModel.addSource("Free-TV 全球", it) }
                            )
                        }
                    }
                }
                
                item {
                    AppleTVSettingsSection(title = "关于") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("版本", color = AppleTVColors.TextTertiary)
                                Text("v1.0.0", color = AppleTVColors.TextSecondary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("构建", color = AppleTVColors.TextTertiary)
                                Text("Apple TV Style", color = AppleTVColors.TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "MOH TV 是一款专为Android TV设计的直播客户端，" +
                                "支持M3U格式直播源，提供流畅的观看体验。",
                                color = AppleTVColors.TextTertiary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddSourceDialog) {
        AppleTVAddSourceDialog(
            onDismiss = { showAddSourceDialog = false },
            onConfirm = { name, url ->
                viewModel.addSource(name, url)
                showAddSourceDialog = false
            }
        )
    }
    
    if (showQRScannerDialog) {
        AppleTVQRScannerDialog(
            onDismiss = { showQRScannerDialog = false },
            onResult = { url ->
                viewModel.addSource("扫码添加", url)
                showQRScannerDialog = false
            }
        )
    }
    
    if (showQRGeneratorDialog) {
        AppleTVQRGeneratorDialog(
            sources = uiState.sources,
            onDismiss = { showQRGeneratorDialog = false }
        )
    }
    
    showDeleteConfirmDialog?.let { source ->
        AppleTVConfirmDialog(
            title = "确认删除",
            message = "确定要删除源 \"${source.name}\" 吗？",
            confirmText = "删除",
            onConfirm = {
                viewModel.deleteSource(source.id)
                showDeleteConfirmDialog = null
            },
            onDismiss = { showDeleteConfirmDialog = null }
        )
    }
}

@Composable
fun AppleTVSettingsHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var isBackFocused by remember { mutableStateOf(false) }
        
        val backScale by animateFloatAsState(
            targetValue = if (isBackFocused) 1.1f else 1f,
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "backScale"
        )
        
        Surface(
            modifier = Modifier
                .scale(backScale)
                .size(48.dp)
                .focusable()
                .onFocusEvent { isBackFocused = it.isFocused },
            shape = CircleShape,
            color = AppleTVColors.Surface,
            border = if (isBackFocused) {
                androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
            } else null,
            onClick = onBack
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = AppleTVColors.TextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = AppleTVColors.TextPrimary
        )
    }
}

@Composable
fun AppleTVSettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppleTVColors.TextTertiary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppleTVShapes.CardMedium,
            color = AppleTVColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun AppleTVSettingsButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    Surface(
        modifier = Modifier
            .scale(scale)
            .focusable()
            .onFocusEvent { isFocused = it.isFocused },
        shape = RoundedCornerShape(12.dp),
        color = AppleTVColors.Primary,
        border = if (isFocused) {
            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
        } else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AppleTVColors.OnPrimary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                color = AppleTVColors.OnPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AppleTVSourceItem(
    source: SourceEntity,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = when {
            isFocused -> 1.03f
            isSelected -> 1.01f
            else -> 1f
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> AppleTVColors.SelectedBackground
            isFocused -> AppleTVColors.SurfaceElevated
            else -> AppleTVColors.Surface
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> AppleTVColors.SelectedBorder
            isFocused -> AppleTVColors.FocusBorder
            else -> Color.Transparent
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "borderColor"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .focusable()
            .onFocusEvent { isFocused = it.isFocused },
        shape = AppleTVShapes.CardMedium,
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected || isFocused) 3.dp else 0.dp,
            color = borderColor
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AppleTVColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = AppleTVColors.OnPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = source.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) AppleTVColors.Primary else AppleTVColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AppleTVColors.Primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "当前",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = AppleTVColors.Primary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = source.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTVColors.TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var isToggleFocused by remember { mutableStateOf(false) }
                var isDeleteFocused by remember { mutableStateOf(false) }
                
                val toggleScale by animateFloatAsState(
                    targetValue = if (isToggleFocused) 1.1f else 1f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing),
                    label = "toggleScale"
                )
                
                val deleteScale by animateFloatAsState(
                    targetValue = if (isDeleteFocused) 1.1f else 1f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing),
                    label = "deleteScale"
                )
                
                Surface(
                    modifier = Modifier
                        .scale(toggleScale)
                        .focusable()
                        .onFocusEvent { isToggleFocused = it.isFocused },
                    shape = RoundedCornerShape(8.dp),
                    color = if (source.enabled) AppleTVColors.Secondary else AppleTVColors.SurfaceVariant,
                    border = if (isToggleFocused) {
                        androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                    } else null,
                    onClick = onToggle
                ) {
                    Text(
                        text = if (source.enabled) "已启用" else "已禁用",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (source.enabled) AppleTVColors.OnSecondary else AppleTVColors.TextTertiary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Surface(
                    modifier = Modifier
                        .scale(deleteScale)
                        .size(36.dp)
                        .focusable()
                        .onFocusEvent { isDeleteFocused = it.isFocused },
                    shape = CircleShape,
                    color = AppleTVColors.Error.copy(alpha = 0.2f),
                    border = if (isDeleteFocused) {
                        androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                    } else null,
                    onClick = onDelete
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = AppleTVColors.Error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppleTVSettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = AppleTVColors.Primary,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val bgColor by animateColorAsState(
        targetValue = if (isFocused) AppleTVColors.SurfaceElevated else AppleTVColors.SurfaceVariant,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .focusable()
            .onFocusEvent { isFocused = it.isFocused },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = if (isFocused) {
            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
        } else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = iconTint,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppleTVColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTVColors.TextTertiary
                )
            }
        }
    }
}

@Composable
fun BuiltInSourceItem(
    name: String,
    description: String,
    sourceUrl: String,
    onAdd: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .focusable()
            .onFocusEvent { isFocused = it.isFocused },
        shape = RoundedCornerShape(12.dp),
        color = AppleTVColors.SurfaceVariant,
        border = if (isFocused) {
            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
        } else null,
        onClick = { onAdd(sourceUrl) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppleTVColors.TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTVColors.TextTertiary
                )
            }
            
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = "添加",
                tint = AppleTVColors.Primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun AppleTVAddSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(600.dp)
                .clickable(enabled = false) { },
            shape = AppleTVShapes.CardLarge,
            color = AppleTVColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "添加直播源",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTVColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleTVColors.Primary,
                        unfocusedBorderColor = AppleTVColors.Border,
                        focusedLabelColor = AppleTVColors.Primary,
                        cursorColor = AppleTVColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("M3U地址") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleTVColors.Primary,
                        unfocusedBorderColor = AppleTVColors.Border,
                        focusedLabelColor = AppleTVColors.Primary,
                        cursorColor = AppleTVColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var isCancelFocused by remember { mutableStateOf(false) }
                    var isConfirmFocused by remember { mutableStateOf(false) }
                    
                    val cancelScale by animateFloatAsState(
                        targetValue = if (isCancelFocused) 1.05f else 1f,
                        animationSpec = tween(150, easing = FastOutSlowInEasing),
                        label = "cancelScale"
                    )
                    
                    val confirmScale by animateFloatAsState(
                        targetValue = if (isConfirmFocused) 1.05f else 1f,
                        animationSpec = tween(150, easing = FastOutSlowInEasing),
                        label = "confirmScale"
                    )
                    
                    Surface(
                        modifier = Modifier
                            .scale(cancelScale)
                            .focusable()
                            .onFocusEvent { isCancelFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        color = AppleTVColors.SurfaceVariant,
                        border = if (isCancelFocused) {
                            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                        } else null,
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "取消",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            color = AppleTVColors.TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Surface(
                        modifier = Modifier
                            .scale(confirmScale)
                            .focusable()
                            .onFocusEvent { isConfirmFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        color = AppleTVColors.Primary,
                        border = if (isConfirmFocused) {
                            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                        } else null,
                        onClick = {
                            if (name.isNotBlank() && url.isNotBlank()) {
                                onConfirm(name, url)
                            }
                        }
                    ) {
                        Text(
                            text = "添加",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            color = AppleTVColors.OnPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppleTVQRScannerDialog(
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(500.dp)
                .clickable(enabled = false) { },
            shape = AppleTVShapes.CardLarge,
            color = AppleTVColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = AppleTVColors.Primary,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "扫描二维码",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTVColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppleTVColors.SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = AppleTVColors.TextTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "摄像头预览区域\n(实际设备上显示扫描界面)",
                                color = AppleTVColors.TextTertiary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "将二维码对准摄像头进行扫描",
                        color = AppleTVColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "需要相机权限才能扫描二维码",
                        color = AppleTVColors.TextTertiary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var isRequestFocused by remember { mutableStateOf(false) }
                    
                    val requestScale by animateFloatAsState(
                        targetValue = if (isRequestFocused) 1.05f else 1f,
                        animationSpec = tween(150, easing = FastOutSlowInEasing),
                        label = "requestScale"
                    )
                    
                    Surface(
                        modifier = Modifier
                            .scale(requestScale)
                            .focusable()
                            .onFocusEvent { isRequestFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        color = AppleTVColors.Primary,
                        border = if (isRequestFocused) {
                            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                        } else null,
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text(
                            text = "授予权限",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            color = AppleTVColors.OnPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                var isCloseFocused by remember { mutableStateOf(false) }
                
                val closeScale by animateFloatAsState(
                    targetValue = if (isCloseFocused) 1.05f else 1f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing),
                    label = "closeScale"
                )
                
                Surface(
                    modifier = Modifier
                        .scale(closeScale)
                        .focusable()
                        .onFocusEvent { isCloseFocused = it.isFocused },
                    shape = RoundedCornerShape(12.dp),
                    color = AppleTVColors.SurfaceVariant,
                    border = if (isCloseFocused) {
                        androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                    } else null,
                    onClick = onDismiss
                ) {
                    Text(
                        text = "关闭",
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                        color = AppleTVColors.TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AppleTVConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确认",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(450.dp)
                .clickable(enabled = false) { },
            shape = AppleTVShapes.CardLarge,
            color = AppleTVColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppleTVColors.AccentOrange,
                    modifier = Modifier.size(56.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTVColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppleTVColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var isCancelFocused by remember { mutableStateOf(false) }
                    var isConfirmFocused by remember { mutableStateOf(false) }
                    
                    val cancelScale by animateFloatAsState(
                        targetValue = if (isCancelFocused) 1.05f else 1f,
                        animationSpec = tween(150, easing = FastOutSlowInEasing),
                        label = "cancelScale"
                    )
                    
                    val confirmScale by animateFloatAsState(
                        targetValue = if (isConfirmFocused) 1.05f else 1f,
                        animationSpec = tween(150, easing = FastOutSlowInEasing),
                        label = "confirmScale"
                    )
                    
                    Surface(
                        modifier = Modifier
                            .scale(cancelScale)
                            .focusable()
                            .onFocusEvent { isCancelFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        color = AppleTVColors.SurfaceVariant,
                        border = if (isCancelFocused) {
                            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                        } else null,
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "取消",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            color = AppleTVColors.TextPrimary
                        )
                    }
                    
                    Surface(
                        modifier = Modifier
                            .scale(confirmScale)
                            .focusable()
                            .onFocusEvent { isConfirmFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        color = AppleTVColors.Error,
                        border = if (isConfirmFocused) {
                            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                        } else null,
                        onClick = onConfirm
                    ) {
                        Text(
                            text = confirmText,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            color = AppleTVColors.OnError
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppleTVQRGeneratorDialog(
    sources: List<SourceEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val configJson = remember(sources) {
        buildString {
            append("{\"sources\":[")
            sources.forEachIndexed { index, source ->
                if (index > 0) append(",")
                append("{\"name\":\"${source.name}\",\"url\":\"${source.url}\",\"enabled\":${source.enabled}}")
            }
            append("]}")
        }
    }
    
    var isCloseFocused by remember { mutableStateOf(false) }
    
    val closeScale by animateFloatAsState(
        targetValue = if (isCloseFocused) 1.1f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "closeScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(600.dp)
                .wrapContentHeight(),
            shape = AppleTVShapes.CardLarge,
            color = AppleTVColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "配置二维码",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppleTVColors.TextPrimary
                    )
                    
                    Surface(
                        modifier = Modifier
                            .scale(closeScale)
                            .size(40.dp)
                            .focusable()
                            .onFocusEvent { isCloseFocused = it.isFocused },
                        shape = CircleShape,
                        color = AppleTVColors.SurfaceVariant,
                        border = if (isCloseFocused) {
                            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                        } else null,
                        onClick = onDismiss
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = AppleTVColors.TextPrimary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "使用其他设备扫描此二维码可导入当前配置",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppleTVColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "二维码生成区域\n(需集成QR库)",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.wrapContentSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AppleTVColors.SurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "配置信息:",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppleTVColors.TextTertiary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "共 ${sources.size} 个直播源",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppleTVColors.TextPrimary
                        )
                        Text(
                            text = "已启用: ${sources.count { it.enabled }} 个",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppleTVColors.TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                var isCopyFocused by remember { mutableStateOf(false) }
                
                val copyScale by animateFloatAsState(
                    targetValue = if (isCopyFocused) 1.05f else 1f,
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    label = "copyScale"
                )
                
                Surface(
                    modifier = Modifier
                        .scale(copyScale)
                        .focusable()
                        .onFocusEvent { isCopyFocused = it.isFocused },
                    shape = RoundedCornerShape(12.dp),
                    color = AppleTVColors.Primary,
                    border = if (isCopyFocused) {
                        androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                    } else null,
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("MOH TV Config", configJson)
                        clipboard.setPrimaryClip(clip)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = AppleTVColors.OnPrimary
                        )
                        Text(
                            text = "复制配置到剪贴板",
                            color = AppleTVColors.OnPrimary
                        )
                    }
                }
            }
        }
    }
}
