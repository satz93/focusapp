package io.github.satz93.focusapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object FocusDimens {
    val ScreenPadding = 24.dp
    val ButtonHeight = 56.dp
    val MinTouch = 48.dp
    val CardRadius = 16.dp
    val ButtonRadius = 16.dp
}

private val LightScheme = lightColorScheme(
    primary = FocusColors.Text, // primary buttons are near-black
    onPrimary = FocusColors.Surface,
    primaryContainer = FocusColors.AccentTint,
    onPrimaryContainer = FocusColors.AccentDark,
    secondary = FocusColors.Accent,
    onSecondary = FocusColors.Surface,
    secondaryContainer = FocusColors.AccentTint,
    onSecondaryContainer = FocusColors.AccentDark,
    tertiary = FocusColors.Warning,
    error = FocusColors.OverLimit,
    errorContainer = FocusColors.OverLimitTint,
    background = FocusColors.Background,
    onBackground = FocusColors.Text,
    surface = FocusColors.Surface,
    onSurface = FocusColors.Text,
    surfaceVariant = FocusColors.MutedFill,
    onSurfaceVariant = FocusColors.TextSecondary,
    surfaceContainer = FocusColors.Surface,
    surfaceContainerLow = FocusColors.Background,
    outline = FocusColors.Border,
    outlineVariant = FocusColors.Border,
)

private val FocusShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/**
 * The app is light-only in v1; the focus timer, guard and notch are explicitly dark
 * and use the dark tokens in [FocusColors] directly.
 */
@Composable
fun FocusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        typography = FocusTypography,
        shapes = FocusShapes,
        content = content,
    )
}
