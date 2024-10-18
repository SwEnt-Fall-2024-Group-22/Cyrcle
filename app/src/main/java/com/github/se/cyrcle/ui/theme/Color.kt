package com.github.se.cyrcle.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Cerulean = Color(0xFF22799B)
val LightBlue = Color(0xFFD1E7F8)

/** A class containing the choice of color for an element. */
enum class ColorLevel {
  PRIMARY,
  SECONDARY,
  TERTIARY,
  ERROR
}

/**
 * Get the color for a container from a ColorScheme.
 *
 * @param colorLevel The chosen ColorScheme
 * @return The container color from the `colorScheme`.
 */
@Composable
fun getContainerColor(colorLevel: ColorLevel): Color {
  return when (colorLevel) {
    ColorLevel.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    ColorLevel.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
    ColorLevel.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    ColorLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
  }
}

/**
 * Get the color for a content from a ColorScheme.
 *
 * @param colorLevel The chosen ColorScheme
 * @return The base color from the `colorScheme`.
 */
@Composable
fun getContentColor(colorLevel: ColorLevel): Color {
  return when (colorLevel) {
    ColorLevel.PRIMARY -> MaterialTheme.colorScheme.primary
    ColorLevel.SECONDARY -> MaterialTheme.colorScheme.secondary
    ColorLevel.TERTIARY -> MaterialTheme.colorScheme.tertiary
    ColorLevel.ERROR -> MaterialTheme.colorScheme.error
  }
}

/**
 * Get the colors for a button from a ColorScheme.
 *
 * @param colorLevel The chosen ColorScheme
 * @return The `ButtonColors` from the `colorScheme`
 */
@Composable
fun getButtonColors(colorLevel: ColorLevel): ButtonColors {
  return ButtonDefaults.buttonColors(
      containerColor = getContainerColor(colorLevel), contentColor = getContentColor(colorLevel))
}
