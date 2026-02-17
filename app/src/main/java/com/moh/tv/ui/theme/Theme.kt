package com.moh.tv.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppleTVColors {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF1C1C1E)
    val SurfaceVariant = Color(0xFF2C2C2E)
    val SurfaceElevated = Color(0xFF3A3A3C)
    
    val Primary = Color(0xFF2997FF)
    val PrimaryVariant = Color(0xFF0A84FF)
    val OnPrimary = Color.White
    
    val Secondary = Color(0xFF30D158)
    val SecondaryVariant = Color(0xFF32D74B)
    val OnSecondary = Color.White
    
    val Accent = Color(0xFF5E5CE6)
    val AccentPink = Color(0xFFFF375F)
    val AccentOrange = Color(0xFFFF9F0A)
    val AccentYellow = Color(0xFFFFD60A)
    
    val OnBackground = Color.White
    val OnSurface = Color.White
    val OnSurfaceVariant = Color(0xFFEBEBF5)
    
    val Border = Color(0xFF38383A)
    val Divider = Color(0xFF3A3A3C)
    
    val Error = Color(0xFFFF453A)
    val OnError = Color.White
    
    val CardFocused = Color(0xFF3A3A3C)
    val CardUnfocused = Color(0xFF1C1C1E)
    
    val GradientStart = Color(0xFF1C1C1E)
    val GradientEnd = Color(0xFF000000)
    
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFEBEBF5)
    val TextTertiary = Color(0xFF8E8E93)
    val TextQuaternary = Color(0xFF636366)
    
    val FocusGlow = Color(0xFFFFFFFF)
    val FocusBorder = Color(0xFFFFFFFF)
    
    val SelectedBackground = Color(0xFF2997FF).copy(alpha = 0.15f)
    val SelectedBorder = Color(0xFF2997FF)
    val SelectedGlow = Color(0xFF2997FF).copy(alpha = 0.3f)
    
    val HoverBackground = Color(0xFF3A3A3C)
}

object AppleTVShapes {
    val CardSmall = RoundedCornerShape(12.dp)
    val CardMedium = RoundedCornerShape(16.dp)
    val CardLarge = RoundedCornerShape(20.dp)
    val CardExtraLarge = RoundedCornerShape(24.dp)
    val Button = RoundedCornerShape(12.dp)
    val Dialog = RoundedCornerShape(20.dp)
    val Chip = RoundedCornerShape(20.dp)
}

object AppleTVElevation {
    val Card = 4.dp
    val CardFocused = 12.dp
    val Dialog = 24.dp
}

object AppleTVAnimation {
    val FocusScale = 1.05f
    val FocusTransitionSpec = TweenSpec<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
}

private val AppleTVColorScheme = androidx.compose.material3.darkColorScheme(
    primary = AppleTVColors.Primary,
    onPrimary = AppleTVColors.OnPrimary,
    primaryContainer = AppleTVColors.SurfaceVariant,
    onPrimaryContainer = AppleTVColors.OnSurface,
    secondary = AppleTVColors.Secondary,
    onSecondary = AppleTVColors.OnSecondary,
    secondaryContainer = AppleTVColors.SurfaceVariant,
    onSecondaryContainer = AppleTVColors.OnSurface,
    tertiary = AppleTVColors.Accent,
    onTertiary = AppleTVColors.OnPrimary,
    background = AppleTVColors.Background,
    onBackground = AppleTVColors.OnBackground,
    surface = AppleTVColors.Surface,
    onSurface = AppleTVColors.OnSurface,
    surfaceVariant = AppleTVColors.SurfaceVariant,
    onSurfaceVariant = AppleTVColors.OnSurfaceVariant,
    error = AppleTVColors.Error,
    onError = AppleTVColors.OnError,
    outline = AppleTVColors.Border,
    outlineVariant = AppleTVColors.Divider
)

@Composable
fun MOHTVTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppleTVColorScheme,
        shapes = androidx.compose.material3.Shapes(
            small = AppleTVShapes.CardSmall,
            medium = AppleTVShapes.CardMedium,
            large = AppleTVShapes.CardLarge
        ),
        content = content
    )
}

val MaterialTheme.appleTVColors: AppleTVColors
    @Composable
    @ReadOnlyComposable
    get() = AppleTVColors

val MaterialTheme.appleTVShapes: AppleTVShapes
    @Composable
    @ReadOnlyComposable
    get() = AppleTVShapes

val MaterialTheme.appleTVElevation: AppleTVElevation
    @Composable
    @ReadOnlyComposable
    get() = AppleTVElevation

val MaterialTheme.appleTVAnimation: AppleTVAnimation
    @Composable
    @ReadOnlyComposable
    get() = AppleTVAnimation
