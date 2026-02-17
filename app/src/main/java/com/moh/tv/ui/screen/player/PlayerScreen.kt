package com.moh.tv.ui.screen.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.moh.tv.player.PlayerManager
import com.moh.tv.ui.viewmodel.PlayerViewModel

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

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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

        if (uiState.showControls) {
            PlayerControls(
                channelName = uiState.currentChannel?.name ?: channelName,
                isPlaying = uiState.playerState.isPlaying,
                isBuffering = uiState.playerState.isBuffering,
                currentPosition = uiState.playerState.currentPosition,
                duration = uiState.playerState.duration,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSeekForward = { viewModel.seekForward() },
                onSeekBack = { viewModel.seekBack() },
                onBack = onBack,
                onQualityClick = { viewModel.showQualityMenu() },
                onVolumeClick = { }
            )
        }

        if (uiState.showQualityMenu) {
            QualityMenuDialog(
                currentQuality = uiState.playerState.currentQuality,
                onQualitySelect = {
                    viewModel.setQuality(it)
                    viewModel.hideQualityMenu()
                },
                onDismiss = { viewModel.hideQualityMenu() }
            )
        }

        if (uiState.showError) {
            ErrorDialog(
                message = uiState.errorMessage ?: "播放错误",
                onRetry = { viewModel.retry() },
                onDismiss = { viewModel.dismissError() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusable(enabled = true)
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
fun PlayerControls(
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
    onVolumeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        TopControl(
            channelName = channelName,
            onBack = onBack,
            onQualityClick = onQualityClick
        )

        Spacer(modifier = Modifier.weight(1f))

        CenterControls(
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            onPlayPause = onPlayPause,
            onSeekForward = onSeekForward,
            onSeekBack = onSeekBack
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomControls(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek,
            onVolumeClick = onVolumeClick
        )
    }
}

@Composable
fun TopControl(
    channelName: String,
    onBack: () -> Unit,
    onQualityClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = channelName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }

        IconButton(onClick = onQualityClick) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "清晰度",
                tint = Color.White
            )
        }
    }
}

@Composable
fun CenterControls(
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
        IconButton(
            onClick = onSeekBack,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                Icons.Default.Replay10,
                contentDescription = "后退10秒",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )
        } else {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        IconButton(
            onClick = onSeekForward,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                Icons.Default.Forward10,
                contentDescription = "前进10秒",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun BottomControls(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    onVolumeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp)
    ) {
        Slider(
            value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            onValueChange = { onSeek((it * duration).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatTime(duration),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun QualityMenuDialog(
    currentQuality: Int,
    onQualitySelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val qualities = listOf(-1 to "自动", 1280 to "1080P", 720 to "720P", 480 to "480P", 360 to "360P")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择清晰度") },
        text = {
            Column {
                qualities.forEach { (quality, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = quality == currentQuality,
                            onClick = { onQualitySelect(quality) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
fun ErrorDialog(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("播放错误") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text("重试")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
