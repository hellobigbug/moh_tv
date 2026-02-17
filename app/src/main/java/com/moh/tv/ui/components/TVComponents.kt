package com.moh.tv.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moh.tv.ui.theme.AppleTVColors
import com.moh.tv.ui.theme.AppleTVShapes

/**
 * 增强的TV卡片组件，带有明显的遥控器选中标识
 */
@Composable
fun TVCard(
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onFocusChange: (Boolean) -> Unit = {},
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = when {
            isFocused -> 1.08f
            isSelected -> 1.03f
            else -> 1f
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "cardScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> AppleTVColors.RemoteFocusBackground
            isSelected -> AppleTVColors.SelectedBackground
            else -> AppleTVColors.Surface
        },
        animationSpec = tween(200),
        label = "cardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> AppleTVColors.RemoteFocusBorder
            isSelected -> AppleTVColors.SelectedBorder
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "cardBorder"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 16.dp else 4.dp,
        animationSpec = tween(200),
        label = "cardShadow"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(shadowElevation, AppleTVShapes.CardMedium)
            .clip(AppleTVShapes.CardMedium)
            .border(
                width = if (isFocused) 4.dp else if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = AppleTVShapes.CardMedium
            )
            .background(backgroundColor)
            .focusable()
            .onFocusEvent {
                onFocusChange(it.isFocused)
            },
        contentAlignment = Alignment.Center
    ) {
        // 聚焦时的发光效果
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = AppleTVColors.RemoteFocusGlow,
                        shape = AppleTVShapes.CardMedium
                    )
            )
        }

        content()
    }
}

/**
 * TV按钮组件，带有明显的选中状态
 */
@Composable
fun TVButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    isPrimary: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "buttonScale"
    )

    val backgroundColor = if (isPrimary) AppleTVColors.Primary else AppleTVColors.SurfaceVariant

    Surface(
        modifier = modifier
            .scale(scale)
            .focusable()
            .onFocusEvent { isFocused = it.isFocused },
        shape = AppleTVShapes.Button,
        color = backgroundColor,
        border = if (isFocused) {
            BorderStroke(3.dp, AppleTVColors.RemoteFocusBorder)
        } else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.invoke()
            Text(
                text = text,
                color = AppleTVColors.OnPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        // 聚焦指示器
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, AppleTVColors.RemoteFocusGlow, AppleTVShapes.Button)
            )
        }
    }
}

/**
 * TV列表项组件，带有明显的聚焦标识
 */
@Composable
fun TVListItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "listItemScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> AppleTVColors.RemoteFocusBackground
            isSelected -> AppleTVColors.SelectedBackground
            else -> AppleTVColors.Surface
        },
        animationSpec = tween(200),
        label = "listItemBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> AppleTVColors.RemoteFocusBorder
            isSelected -> AppleTVColors.SelectedBorder
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "listItemBorder"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .focusable()
            .onFocusEvent { isFocused = it.isFocused },
        shape = AppleTVShapes.CardSmall,
        color = backgroundColor,
        border = BorderStroke(
            width = if (isFocused) 3.dp else if (isSelected) 2.dp else 0.dp,
            color = borderColor
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isFocused || isSelected) AppleTVColors.Primary else AppleTVColors.TextPrimary,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTVColors.TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            trailingContent?.invoke()
        }

        // 左侧聚焦指示条
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(AppleTVColors.RemoteFocusBorder)
            )
        }
    }
}

/**
 * TV频道卡片组件
 */
@Composable
fun TVChannelCard(
    channelName: String,
    groupName: String,
    isFocused: Boolean = false,
    isFavorite: Boolean = false,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "channelScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) AppleTVColors.RemoteFocusBackground else AppleTVColors.Surface,
        animationSpec = tween(200),
        label = "channelBg"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .width(160.dp)
            .focusable()
            .onFocusEvent {
                // 处理聚焦事件
            }
            .border(
                width = if (isFocused) 4.dp else 0.dp,
                color = if (isFocused) AppleTVColors.RemoteFocusBorder else Color.Transparent,
                shape = AppleTVShapes.CardMedium
            )
            .clip(AppleTVShapes.CardMedium)
            .background(backgroundColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 频道图标占位
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AppleTVColors.SurfaceVariant,
                            AppleTVColors.SurfaceElevated
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = channelName.take(1),
                style = MaterialTheme.typography.headlineMedium,
                color = AppleTVColors.Primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = channelName,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isFocused) AppleTVColors.Primary else AppleTVColors.TextPrimary,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = groupName,
            style = MaterialTheme.typography.bodySmall,
            color = AppleTVColors.TextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (isFavorite) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = AppleTVColors.AccentPink.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "★ 收藏",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = AppleTVColors.AccentPink,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * TV导航指示器 - 显示当前聚焦位置
 */
@Composable
fun TVFocusIndicator(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .border(
                    width = 4.dp,
                    color = AppleTVColors.RemoteFocusBorder,
                    shape = AppleTVShapes.CardMedium
                )
                .padding(4.dp)
                .border(
                    width = 2.dp,
                    color = AppleTVColors.RemoteFocusGlow,
                    shape = AppleTVShapes.CardSmall
                )
        )
    }
}
