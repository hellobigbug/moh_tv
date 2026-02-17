package com.moh.tv.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moh.tv.ui.theme.AppleTVColors
import com.moh.tv.ui.theme.AppleTVShapes

object TVAnimationSpecs {
    val FocusScale = 1.05f
    val FocusScaleLarge = 1.08f
    val FocusScaleSmall = 1.03f
    val FocusTransition = tween<Float>(200, easing = FastOutSlowInEasing)
    val QuickTransition = tween<Float>(150, easing = FastOutSlowInEasing)
    val ColorTransition = tween<Color>(200, easing = FastOutSlowInEasing)
    val DpTransition = tween<Dp>(200, easing = FastOutSlowInEasing)
}

@Composable
fun animateFocusScale(
    isFocused: Boolean,
    isSelected: Boolean = false,
    focusedScale: Float = TVAnimationSpecs.FocusScale,
    selectedScale: Float = TVAnimationSpecs.FocusScaleSmall
): Float {
    val scale by animateFloatAsState(
        targetValue = when {
            isFocused -> focusedScale
            isSelected -> selectedScale
            else -> 1f
        },
        animationSpec = TVAnimationSpecs.FocusTransition,
        label = "focusScale"
    )
    return scale
}

@Composable
fun animateFocusColor(
    isFocused: Boolean,
    isSelected: Boolean = false,
    focusedColor: Color = AppleTVColors.RemoteFocusBackground,
    selectedColor: Color = AppleTVColors.SelectedBackground,
    defaultColor: Color = AppleTVColors.Surface
): Color {
    val color by animateColorAsState(
        targetValue = when {
            isFocused -> focusedColor
            isSelected -> selectedColor
            else -> defaultColor
        },
        animationSpec = TVAnimationSpecs.ColorTransition,
        label = "focusColor"
    )
    return color
}

@Composable
fun animateFocusBorderColor(
    isFocused: Boolean,
    isSelected: Boolean = false
): Color {
    val color by animateColorAsState(
        targetValue = when {
            isFocused -> AppleTVColors.RemoteFocusBorder
            isSelected -> AppleTVColors.SelectedBorder
            else -> Color.Transparent
        },
        animationSpec = TVAnimationSpecs.ColorTransition,
        label = "focusBorderColor"
    )
    return color
}

@Composable
fun animateFocusElevation(
    isFocused: Boolean,
    focusedElevation: Dp = 16.dp,
    defaultElevation: Dp = 4.dp
): Dp {
    val elevation by animateDpAsState(
        targetValue = if (isFocused) focusedElevation else defaultElevation,
        animationSpec = TVAnimationSpecs.DpTransition,
        label = "focusElevation"
    )
    return elevation
}

@Composable
fun Modifier.tvFocusBorder(
    isFocused: Boolean,
    isSelected: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = AppleTVShapes.CardMedium
): Modifier {
    val borderWidth = when {
        isFocused -> 4.dp
        isSelected -> 2.dp
        else -> 0.dp
    }
    val borderColor = animateFocusBorderColor(isFocused, isSelected)
    
    return if (borderWidth > 0.dp) {
        this.border(borderWidth, borderColor, shape)
    } else {
        this
    }
}
