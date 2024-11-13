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
        secondary = YInMnBlue,
        onSecondary = White,
        secondaryContainer = YInMnBlue,
        onSecondaryContainer = White,
        // Tertiary
        tertiary = ShamrockGreen,
        onTertiary = White,
        tertiaryContainer = ShamrockGreen,
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
        surfaceTint = Aero,
        surfaceVariant = Aero,
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
        scrim = Color.Unspecified, // Semi-transparent overlay color // Unused
        outline = Cerulean,
        outlineVariant = Color.Unspecified // Unused
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
