package io.github.satz93.focusapp.ui.theme

import androidx.compose.ui.graphics.Color

/** Design tokens from the v1 brief. Use these instead of raw hex values in UI code. */
object FocusColors {
    // Light surfaces
    val Background = Color(0xFFFAFAF7)
    val Surface = Color(0xFFFFFFFF)
    val Border = Color(0xFFE6E6E0)
    val MutedFill = Color(0xFFEEEEE8)

    // Text
    val Text = Color(0xFF16181D)
    val TextSecondary = Color(0xFF5B6170)

    // Accent
    val Accent = Color(0xFF3B4BDB)
    val AccentDark = Color(0xFF2B38A8)
    val AccentTint = Color(0xFFE9EBFB)
    val AccentOnDark = Color(0xFF8F9BFF)

    // Status
    val Warning = Color(0xFFD97706)
    val OverLimit = Color(0xFFC2410C)
    val OverLimitTint = Color(0xFFFDF0E7)

    // Dark screens (focus timer, guard, notch)
    val DarkBackground = Color(0xFF111318)
    val DarkDeep = Color(0xFF0B0C0F)
    val DarkCard = Color(0xFF1E2029)
    val DarkLine = Color(0xFF2A2D36)
    val OnDark = Color(0xFFFFFFFF)
    val OnDarkSecondary = Color(0xFFA9AEBB)

    /** Progress colour for "used vs limit": blue, then amber at [warnAt], orange at 100%. */
    fun usage(fraction: Float, warnAt: Float = 0.85f): Color = when {
        fraction >= 1f -> OverLimit
        fraction >= warnAt -> Warning
        else -> Accent
    }
}
