package com.moh.tv.ui.screen.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.moh.tv.player.PlayerManager
import com.moh.tv.ui.theme.AppleTVColors
import com.moh.tv.ui.theme.AppleTVShapes
import com.moh.tv.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    channelId: Long,
    channelUrl: String,
    channelName: String,
    viewModel: PlayerViewModel = hiltViewModel(),
    playerManager: PlayerManager
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    var showControls by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }
    
    BackHandler {
        onBack()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    player = playerManager.getPlayer()
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { playerView ->
                playerView.player = playerManager.getPlayer()
            }
        )
        
        LaunchedEffect(channelId, channelUrl) {
            val channel = com.moh.tv.data.local.entity.ChannelEntity(
                id = channelId,
                name = channelName,
                url = channelUrl,
                group = ""
            )
            viewModel.playChannel(channel)
        }
        
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            )
        ) {
            AppleTVPlayerControls(
                channelName = uiState.currentChannel?.name ?: channelName,
                isPlaying = uiState.playerState.isPlaying,
                isBuffering = uiState.playerState.isBuffering,
                currentPosition = uiState.playerState.currentPosition,
                duration = uiState.playerState.duration,
                onPlayPause = { 
                    viewModel.togglePlayPause()
                    showControls = true
                },
                onSeek = { 
                    viewModel.seekTo(it)
                    showControls = true
                },
                onSeekForward = { 
                    viewModel.seekForward()
                    showControls = true
                },
                onSeekBack = { 
                    viewModel.seekBack()
                    showControls = true
                },
                onBack = onBack,
                onQualityClick = { 
                    viewModel.showQualityMenu()
                    showControls = true
                },
                onVolumeClick = { },
                onShowControls = { showControls = true }
            )
        }
        
        if (uiState.showQualityMenu) {
            AppleTVQualityMenuDialog(
                currentQuality = uiState.playerState.currentQuality,
                onQualitySelect = {
                    viewModel.setQuality(it)
                    viewModel.hideQualityMenu()
                },
                onDismiss = { viewModel.hideQualityMenu() }
            )
        }
        
        if (uiState.showError) {
            AppleTVErrorDialog(
                message = uiState.errorMessage ?: "播放错误",
                onRetry = { viewModel.retry() },
                onDismiss = { viewModel.dismissError() }
            )
        }
        
        if (uiState.playerState.isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AppleTVColors.Primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusable(enabled = true)
                .clickable { 
                    showControls = !showControls
                }
        ) {
            DisposableEffect(Unit) {
                onDispose {
                    viewModel.releasePlayer()
                }
            }
        }
    }
}

@Composable
fun AppleTVPlayerControls(
    channelName: String,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekForward: () -> Unit,
    onSeekBack: () -> Unit,
    onBack: () -> Unit,
    onQualityClick: () -> Unit,
    onVolumeClick: () -> Unit,
    onShowControls: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        AppleTVTopControl(
            channelName = channelName,
            onBack = onBack,
            onQualityClick = onQualityClick
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        AppleTVCenterControls(
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            onPlayPause = onPlayPause,
            onSeekForward = onSeekForward,
            onSeekBack = onSeekBack
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        AppleTVBottomControls(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek,
            onVolumeClick = onVolumeClick
        )
    }
}

@Composable
fun AppleTVTopControl(
    channelName: String,
    onBack: () -> Unit,
    onQualityClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                color = AppleTVColors.Surface.copy(alpha = 0.8f),
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
                        tint = AppleTVColors.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Text(
                text = channelName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppleTVColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        var isQualityFocused by remember { mutableStateOf(false) }
        
        val qualityScale by animateFloatAsState(
            targetValue = if (isQualityFocused) 1.1f else 1f,
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "qualityScale"
        )
        
        Surface(
            modifier = Modifier
                .scale(qualityScale)
                .focusable()
                .onFocusEvent { isQualityFocused = it.isFocused },
            shape = RoundedCornerShape(12.dp),
            color = AppleTVColors.Surface.copy(alpha = 0.8f),
            border = if (isQualityFocused) {
                androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
            } else null,
            onClick = onQualityClick
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "画质",
                    tint = AppleTVColors.TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "画质",
                    color = AppleTVColors.TextPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun AppleTVCenterControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onPlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var isBackFocused by remember { mutableStateOf(false) }
        var isPlayFocused by remember { mutableStateOf(false) }
        var isForwardFocused by remember { mutableStateOf(false) }
        
        val backScale by animateFloatAsState(
            targetValue = if (isBackFocused) 1.15f else 1f,
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "backScale"
        )
        
        val playScale by animateFloatAsState(
            targetValue = if (isPlayFocused) 1.15f else 1f,
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "playScale"
        )
        
        val forwardScale by animateFloatAsState(
            targetValue = if (isForwardFocused) 1.15f else 1f,
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "forwardScale"
        )
        
        Surface(
            modifier = Modifier
                .scale(backScale)
                .size(64.dp)
                .focusable()
                .onFocusEvent { isBackFocused = it.isFocused },
            shape = CircleShape,
            color = AppleTVColors.Surface.copy(alpha = 0.8f),
            border = if (isBackFocused) {
                androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
            } else null,
            onClick = onSeekBack
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Replay10,
                    contentDescription = "后退10秒",
                    tint = AppleTVColors.TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(48.dp))
        
        Surface(
            modifier = Modifier
                .scale(playScale)
                .size(88.dp)
                .focusable()
                .onFocusEvent { isPlayFocused = it.isFocused },
            shape = CircleShape,
            color = AppleTVColors.Primary,
            border = if (isPlayFocused) {
                androidx.compose.foundation.BorderStroke(3.dp, AppleTVColors.FocusBorder)
            } else null,
            onClick = onPlayPause
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        color = AppleTVColors.OnPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = AppleTVColors.OnPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(48.dp))
        
        Surface(
            modifier = Modifier
                .scale(forwardScale)
                .size(64.dp)
                .focusable()
                .onFocusEvent { isForwardFocused = it.isFocused },
            shape = CircleShape,
            color = AppleTVColors.Surface.copy(alpha = 0.8f),
            border = if (isForwardFocused) {
                androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
            } else null,
            onClick = onSeekForward
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Forward10,
                    contentDescription = "前进10秒",
                    tint = AppleTVColors.TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun AppleTVBottomControls(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    onVolumeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = formatTime(currentPosition),
                color = AppleTVColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            
            var isSliderFocused by remember { mutableStateOf(false)
            }
            
            val sliderScale by animateFloatAsState(
                targetValue = if (isSliderFocused) 1.02f else 1f,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "sliderScale"
            )
            
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                modifier = Modifier
                    .weight(1f)
                    .scale(sliderScale)
                    .focusable()
                    .onFocusEvent { isSliderFocused = it.isFocused },
                colors = SliderDefaults.colors(
                    thumbColor = AppleTVColors.Primary,
                    activeTrackColor = AppleTVColors.Primary,
                    inactiveTrackColor = AppleTVColors.SurfaceVariant
                )
            )
            
            Text(
                text = formatTime(duration),
                color = AppleTVColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AppleTVQualityMenuDialog(
    currentQuality: Int,
    onQualitySelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val qualities = listOf(
        -1 to "自动",
        1080 to "高清 1080p",
        720 to "高清 720p",
        480 to "标清 480p",
        360 to "流畅 360p"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .wrapContentHeight(),
            shape = AppleTVShapes.CardLarge,
            color = AppleTVColors.Surface,
            tonalElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "选择画质",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTVColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(qualities) { (qualityValue, qualityName) ->
                        var isFocused by remember { mutableStateOf(false) }
                        
                        val bgColor by animateColorAsState(
                            targetValue = when {
                                qualityValue == currentQuality -> AppleTVColors.Primary.copy(alpha = 0.2f)
                                isFocused -> AppleTVColors.SurfaceElevated
                                else -> AppleTVColors.SurfaceVariant
                            },
                            animationSpec = tween(150, easing = FastOutSlowInEasing),
                            label = "bgColor"
                        )
                        
                        val scale by animateFloatAsState(
                            targetValue = if (isFocused) 1.02f else 1f,
                            animationSpec = tween(150, easing = FastOutSlowInEasing),
                            label = "scale"
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
                            onClick = { onQualitySelect(qualityValue) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = qualityName,
                                    color = if (qualityValue == currentQuality) AppleTVColors.Primary else AppleTVColors.TextPrimary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (qualityValue == currentQuality) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = AppleTVColors.Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppleTVErrorDialog(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.width(500.dp),
            shape = AppleTVShapes.CardLarge,
            color = AppleTVColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = AppleTVColors.Error,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "播放错误",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleTVColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppleTVColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var isRetryFocused by remember { mutableStateOf(false) }
                    var isDismissFocused by remember { mutableStateOf(false) }
                    
                    Surface(
                        modifier = Modifier
                            .focusable()
                            .onFocusEvent { isRetryFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        color = AppleTVColors.SurfaceVariant,
                        border = if (isRetryFocused) {
                            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                        } else null,
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "关闭",
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp),
                            color = AppleTVColors.TextPrimary
                        )
                    }
                    
                    Surface(
                        modifier = Modifier
                            .focusable()
                            .onFocusEvent { isDismissFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        color = AppleTVColors.Primary,
                        border = if (isDismissFocused) {
                            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.FocusBorder)
                        } else null,
                        onClick = onRetry
                    ) {
                        Text(
                            text = "重试",
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp),
                            color = AppleTVColors.OnPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis < 0) return "00:00"
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = millis / (1000 * 60 * 60)
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
