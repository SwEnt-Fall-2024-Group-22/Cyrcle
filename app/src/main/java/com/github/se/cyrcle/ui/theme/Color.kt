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
val Cyan = Color(0xFF80DEF3)
val NormalBlue = Color(0xFF00D0FF)
val DarkBlue = Color(0xFF00A1FF)

/** A class containing the "levels" of colors for an element. */
enum class ColorScheme {
  PRIMARY,
  SECONDARY
}

/**
 * Get the color for a container from a ColorScheme.
 *
 * @param colorScheme The chosen ColorScheme
 */
@Composable
fun getContainerColor(colorScheme: ColorScheme): Color {
  return when (colorScheme) {
    ColorScheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    ColorScheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
  }
}

/**
 * Get the color for a content from a ColorScheme.
 *
 * @param colorScheme The chosen ColorScheme
 */
@Composable
fun getContentColor(colorScheme: ColorScheme): Color {
  return when (colorScheme) {
    ColorScheme.PRIMARY -> MaterialTheme.colorScheme.primary
    ColorScheme.SECONDARY -> MaterialTheme.colorScheme.secondary
  }
}

/**
 * Get the colors for a button from a ColorScheme.
 *
 * @param colorScheme The chosen ColorScheme
 */
@Composable
fun getButtonColors(colorScheme: ColorScheme): ButtonColors {
  return ButtonDefaults.buttonColors(
      containerColor = getContainerColor(colorScheme), contentColor = getContentColor(colorScheme))
}
