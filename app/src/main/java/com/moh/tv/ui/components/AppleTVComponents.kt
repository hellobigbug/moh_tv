package com.moh.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moh.tv.ui.theme.AppleTVColors
import com.moh.tv.ui.theme.AppleTVShapes

@Composable
fun AppleTVCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    shape: Shape = AppleTVShapes.CardMedium,
    backgroundColor: Color = AppleTVColors.Surface,
    content: @Composable BoxScope.() -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 16.dp else 4.dp,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "elevation"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) AppleTVColors.FocusBorder else Color.Transparent,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "borderColor"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .shadow(elevation, shape, spotColor = if (isFocused) Color.Black.copy(alpha = 0.5f) else Color.Transparent)
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = borderColor,
                shape = shape
            )
            .focusable()
            .onFocusEvent { focusState ->
                isFocused = focusState.isFocused
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Composable
fun AppleTVFocusableCard(
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    shape: Shape = AppleTVShapes.CardMedium,
    backgroundColor: Color = AppleTVColors.Surface,
    focusedBackgroundColor: Color = AppleTVColors.SurfaceElevated,
    content: @Composable BoxScope.() -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 16.dp else 4.dp,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "elevation"
    )
    
    val bgColor by animateColorAsState(
        targetValue = if (isFocused) focusedBackgroundColor else backgroundColor,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) AppleTVColors.FocusBorder else Color.Transparent,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "borderColor"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .shadow(elevation, shape, spotColor = if (isFocused) Color.Black.copy(alpha = 0.5f) else Color.Transparent)
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = borderColor,
                shape = shape
            )
            .clip(shape)
            .background(bgColor)
    ) {
        content()
    }
}

@Composable
fun AppleTVChip(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            selected -> AppleTVColors.Primary
            isFocused -> AppleTVColors.SurfaceElevated
            else -> AppleTVColors.SurfaceVariant
        },
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    
    val textColor by animateColorAsState(
        targetValue = when {
            selected -> AppleTVColors.OnPrimary
            else -> AppleTVColors.TextPrimary
        },
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "textColor"
    )
    
    Surface(
        modifier = modifier
            .scale(scale)
            .focusable()
            .onFocusEvent { focusState ->
                isFocused = focusState.isFocused
            },
        shape = AppleTVShapes.Chip,
        color = backgroundColor,
        border = if (isFocused && !selected) BorderStroke(2.dp, AppleTVColors.FocusBorder) else null,
        onClick = onClick
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AppleTVButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused && enabled) 1.05f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val backgroundColor = when {
        !enabled -> AppleTVColors.SurfaceVariant
        isPrimary -> AppleTVColors.Primary
        else -> AppleTVColors.Surface
    }
    
    val contentColor = when {
        !enabled -> AppleTVColors.TextTertiary
        isPrimary -> AppleTVColors.OnPrimary
        else -> AppleTVColors.TextPrimary
    }
    
    Button(
        modifier = modifier
            .scale(scale)
            .focusable()
            .onFocusEvent { focusState ->
                isFocused = focusState.isFocused
            },
        shape = AppleTVShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = AppleTVColors.SurfaceVariant,
            disabledContentColor = AppleTVColors.TextTertiary
        ),
        border = if (isFocused && enabled && !isPrimary) BorderStroke(2.dp, AppleTVColors.FocusBorder) else null,
        enabled = enabled,
        onClick = onClick
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AppleTVIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused && enabled) 1.1f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused && enabled) AppleTVColors.SurfaceElevated else Color.Transparent,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    
    Surface(
        modifier = modifier
            .scale(scale)
            .size(48.dp)
            .focusable()
            .onFocusEvent { focusState ->
                isFocused = focusState.isFocused
            },
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = if (isFocused && enabled) BorderStroke(2.dp, AppleTVColors.FocusBorder) else null,
        onClick = onClick,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun AppleTVSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = AppleTVColors.TextPrimary
        )
        action?.invoke()
    }
}

@Composable
fun AppleTVGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    AppleTVColors.GradientStart,
                    AppleTVColors.GradientEnd
                )
            )
        )
    ) {
        content()
    }
}
