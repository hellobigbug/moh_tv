package com.moh.tv.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moh.tv.ui.theme.AppleTVColors
import com.moh.tv.ui.theme.AppleTVShapes
import kotlinx.coroutines.delay

/**
 * 快速搜索对话框
 */
@Composable
fun QuickSearchDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .focusable()
            .onFocusEvent {
                if (!it.isFocused && !it.hasFocus) {
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(500.dp)
                .padding(32.dp),
            shape = AppleTVShapes.CardLarge,
            color = AppleTVColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "快速搜索",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppleTVColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 搜索输入框
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppleTVShapes.CardMedium,
                    color = AppleTVColors.SurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        AppleTVColors.RemoteFocusBorder
                    )
                ) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .padding(16.dp),
                        textStyle = TextStyle(
                            color = AppleTVColors.TextPrimary,
                            fontSize = 20.sp
                        ),
                        cursorBrush = SolidColor(AppleTVColors.Primary),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search,
                            keyboardType = KeyboardType.Text
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = AppleTVColors.TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "输入频道名称...",
                                            color = AppleTVColors.TextTertiary,
                                            fontSize = 20.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "使用遥控器方向键输入，按确认键搜索",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleTVColors.TextTertiary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TVButton(
                        text = "取消",
                        onClick = onDismiss,
                        isPrimary = false
                    )

                    TVButton(
                        text = "搜索",
                        onClick = onSearch,
                        isPrimary = true
                    )
                }
            }
        }
    }
}

/**
 * 数字键快速选台组件
 */
@Composable
fun NumberPadSelector(
    visible: Boolean,
    onNumberInput: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    currentNumber: String
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
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
                    .width(400.dp)
                    .padding(24.dp),
                shape = AppleTVShapes.CardLarge,
                color = AppleTVColors.Surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "数字选台",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppleTVColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 显示当前输入的数字
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = AppleTVShapes.CardMedium,
                        color = AppleTVColors.SurfaceVariant
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (currentNumber.isEmpty()) "--" else currentNumber,
                                style = MaterialTheme.typography.headlineLarge,
                                color = if (currentNumber.isEmpty()) AppleTVColors.TextTertiary else AppleTVColors.Primary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 数字键盘
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1-3
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NumberButton("1") { onNumberInput(1) }
                            NumberButton("2") { onNumberInput(2) }
                            NumberButton("3") { onNumberInput(3) }
                        }
                        // 4-6
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NumberButton("4") { onNumberInput(4) }
                            NumberButton("5") { onNumberInput(5) }
                            NumberButton("6") { onNumberInput(6) }
                        }
                        // 7-9
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NumberButton("7") { onNumberInput(7) }
                            NumberButton("8") { onNumberInput(8) }
                            NumberButton("9") { onNumberInput(9) }
                        }
                        // 0
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NumberButton("清除", isWide = true) { onNumberInput(-1) }
                            NumberButton("0") { onNumberInput(0) }
                            NumberButton("确认", isWide = true) { onConfirm() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberButton(
    text: String,
    isWide: Boolean = false,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = tween(150),
        label = "numberScale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .width(if (isWide) 100.dp else 60.dp)
            .height(50.dp)
            .focusable()
            .onFocusEvent { isFocused = it.isFocused },
        shape = RoundedCornerShape(8.dp),
        color = if (isFocused) AppleTVColors.Primary else AppleTVColors.SurfaceVariant,
        border = if (isFocused) {
            androidx.compose.foundation.BorderStroke(2.dp, AppleTVColors.RemoteFocusBorder)
        } else null,
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isFocused) AppleTVColors.OnPrimary else AppleTVColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 频道信息浮层（显示当前节目信息）
 */
@Composable
fun ChannelInfoOverlay(
    channelName: String,
    currentProgram: String?,
    nextProgram: String?,
    progress: Float,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 48.dp, vertical = 32.dp)
        ) {
            Column {
                Text(
                    text = channelName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppleTVColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                if (currentProgram != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "正在播放: $currentProgram",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppleTVColors.TextSecondary
                    )

                    // 进度条
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AppleTVColors.Primary,
                        trackColor = AppleTVColors.SurfaceVariant
                    )
                }

                if (nextProgram != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "即将播放: $nextProgram",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppleTVColors.TextTertiary
                    )
                }
            }
        }
    }
}
