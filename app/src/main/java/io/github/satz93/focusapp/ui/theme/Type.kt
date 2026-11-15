package io.github.satz93.focusapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.satz93.focusapp.R

// Both fonts are bundled variable fonts (SIL OFL), so no network is needed.
@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, weight: Int) = Font(
    resId = res,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Headings and big numbers. */
val SpaceGrotesk = FontFamily(
    variable(R.font.space_grotesk, 400),
    variable(R.font.space_grotesk, 500),
    variable(R.font.space_grotesk, 600),
    variable(R.font.space_grotesk, 700),
)

/** Body text. */
val Inter = FontFamily(
    variable(R.font.inter, 400),
    variable(R.font.inter, 500),
    variable(R.font.inter, 600),
    variable(R.font.inter, 700),
)

private val Heading = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em)
private val Body = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal)

val FocusTypography = Typography(
    // Big numbers (unlock count, timer)
    displayLarge = Heading.copy(fontSize = 64.sp, lineHeight = 68.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.03).em),
    displayMedium = Heading.copy(fontSize = 48.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    displaySmall = Heading.copy(fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    // Screen titles
    headlineLarge = Heading.copy(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    headlineMedium = Heading.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    headlineSmall = Heading.copy(fontSize = 20.sp, lineHeight = 26.sp),
    // Stat values in cards
    titleLarge = Heading.copy(fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = Body.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = Body.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = Body.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = Body.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = Body.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = Body.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = Body.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = Body.copy(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
)
