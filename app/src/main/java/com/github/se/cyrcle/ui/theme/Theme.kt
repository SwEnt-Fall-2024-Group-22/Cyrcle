package com.github.se.cyrcle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

const val primaryContainerAlphaModifier = 0.75f
const val surfaceContainerAlphaModifier = 0.75f

/**
 * Return a default OnColor (for text and/or icon), based on the system theme.
 *
 * @return A light color if the system is in light theme, or a dark one otherwise.
 */
@Composable
fun defaultOnColor(): Color {
  return if (isSystemInDarkTheme()) White else Black
}

/**
 * Return an OnColor (for text and/or icon), based on the luminance of the given color.
 *
 * @param backgroundColor The color on which the text/icon will be drawn
 * @return A light color if the luminance of `backgroundColor < 0.5`, or a dark one otherwise.
 */
@Composable
fun defaultOnColorFromLuminance(backgroundColor: Color): Color {
  return if (backgroundColor.luminance() < 0.5) White else Black
}

val DebugColorScheme =
    lightColorScheme(
        // Primary
        primary = Color(0xFF000011),
        onPrimary = Color(0xFF000022),
        primaryContainer = Color(0xFF000033),
        onPrimaryContainer = Color(0xFF000044),
        inversePrimary = Color(0xFF000055),
        // Secondary
        secondary = Color(0xFF000066),
        onSecondary = Color(0xFF000077),
        secondaryContainer = Color(0xFF000088),
        onSecondaryContainer = Color(0xFF000099),
        // Tertiary
        tertiary = Color(0xFF0000AA),
        onTertiary = Color(0xFF0000BB),
        tertiaryContainer = Color(0xFF0000CC),
        onTertiaryContainer = Color(0xFF0000DD),
        // Error
        error = Color(0xFF110000),
        onError = Color(0xFF220000),
        errorContainer = Color(0xFF330000),
        onErrorContainer = Color(0xFF440000),
        // Surfaces
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFFFF2200),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF884400),
        surfaceDim = Color(0xFF445500),
        surfaceBright = Color(0xFF0066FF),
        surfaceTint = Color(0xFF0077AA),
        surfaceVariant = Color(0xFF008888),
        onSurfaceVariant = Color(0xFF009944),
        inverseSurface = Color(0xFFFFAAFF),
        inverseOnSurface = Color(0xFF88BB88),
        surfaceContainer = Color(0xFF44CC44),
        surfaceContainerLowest = Color(0xFFFFDD44),
        surfaceContainerLow = Color(0xFFAAEE88),
        surfaceContainerHigh = Color(0xFF88FFAA),
        surfaceContainerHighest = Color(0xFF44FFFF),
        // Others
        scrim = Color(0xFF111111),
        outline = Color(0xFF222222),
        outlineVariant = Color(0xFF333333),
    )

val LightColorScheme =
    lightColorScheme(
        // Primary
        primary = Cerulean, // Background of a small element
        onPrimary = White, // Text over a small element
        primaryContainer = // Background of a group of element
        Cerulean,
        onPrimaryContainer = White, // Text over a group of element
        inversePrimary = invertColor(Cerulean),
        // Secondary
        secondary = Madder,
        onSecondary = White,
        secondaryContainer = Madder,
        onSecondaryContainer = White,
        // Tertiary
        tertiary = GoldenBrown,
        onTertiary = White,
        tertiaryContainer = GoldenBrown,
        onTertiaryContainer = White,
        // Error
        error = Red,
        onError = White,
        errorContainer = Red,
        onErrorContainer = White,
        // Surfaces
        background = White, // General background of the app
        onBackground = Black,
        surface = White, // The background for a big element
        onSurface = Cerulean,
        surfaceDim = CeruleanLow, // Darker version of surface
        surfaceBright = CeruleanHigh, // Lighter version of surface
        surfaceTint = CeruleanTint,
        surfaceVariant = Blue,
        onSurfaceVariant = White,
        inverseSurface = invertColor(Cerulean),
        inverseOnSurface = invertColor(White),
        surfaceContainer = // Background for a giant (lowest above background) element
        Cerulean,
        surfaceContainerLowest = CeruleanLowest,
        surfaceContainerLow = CeruleanLow,
        surfaceContainerHigh = CeruleanHigh,
        surfaceContainerHighest = CeruleanHighest,
        // Others
        scrim = White, // Semi-transparent overlay color // Unused
        outline = White, // Unused
        outlineVariant = White // Unused
        )

val DarkColorScheme = LightColorScheme
/*darkColorScheme(
// Primary
primary = DarkCerulean, // Background of a small element
onPrimary = White, // Text over a small element
primaryContainer = // Background of a group of element
DarkCerulean.copy(alpha = primaryContainerAlphaModifier),
onPrimaryContainer = White, // Text over a group of element
inversePrimary = invertColor(DarkCerulean),
// Secondary
secondary = DarkMadder,
onSecondary = White,
secondaryContainer = DarkMadder.copy(alpha = primaryContainerAlphaModifier),
onSecondaryContainer = White,
// Tertiary
tertiary = DarkGoldenBrown,
onTertiary = White,
tertiaryContainer = DarkGoldenBrown.copy(alpha = primaryContainerAlphaModifier),
onTertiaryContainer = White,
// Error
error = DarkRed,
onError = White,
errorContainer = DarkRed.copy(alpha = primaryContainerAlphaModifier),
onErrorContainer = White,
// Surfaces
background = Color(0xFF0A2530), // General background of the app
onBackground = White,
surface = DarkCerulean.copy(alpha = 0.5f), // The background for a big element
onSurface = White,
surfaceDim = DarkCeruleanLow, // Darker version of surface
surfaceBright = DarkCeruleanHigh, // Lighter version of surface
surfaceTint = DarkCeruleanTint,
surfaceVariant = DarkBlue.copy(alpha = 0.9f),
onSurfaceVariant = White,
inverseSurface = invertColor(DarkCerulean),
inverseOnSurface = invertColor(Black),
surfaceContainer = // Background for a giant (lowest above background) element
DarkCerulean.copy(alpha = 0.33f),
surfaceContainerLowest = DarkCeruleanLowest.copy(alpha = surfaceContainerAlphaModifier),
surfaceContainerLow = DarkCeruleanLow.copy(alpha = surfaceContainerAlphaModifier),
surfaceContainerHigh = DarkCeruleanHigh.copy(alpha = surfaceContainerAlphaModifier),
surfaceContainerHighest = DarkCeruleanHighest.copy(alpha = surfaceContainerAlphaModifier),
// Others
scrim = White, // Semi-transparent overlay color // Unused
outline = White, // Unused
outlineVariant = White // Unused
)*/

/**
 * Return a themed disabledColor, depending on the system theme.
 *
 * @return A themed color.
 */
@Composable
fun disabledColor(): Color {
  return if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray
}

/**
 * Return a themed onDisabled color, depending on the system theme.
 *
 * @return A themed color.
 */
@Composable
fun onDisabledColor(): Color {
  return if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
}

/** Generate the theme of the application. */
@Composable
fun CyrcleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
  MaterialTheme(
      colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
      typography = Typography,
      content = content)
}
