package com.github.se.cyrcle.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val LightBlue = Color(0xFFD1E7F8)
val Red = Color(0xFFFF0000)
val DarkRed = Color(0xFFAA0000)
// val Green = Color(0xFF00FF00)
// val DarkGreen = Color(0xFF00AA00)
val Blue = Color(0xFF0000FF)
val DarkBlue = Color(0xFF0000AA)

val Cerulean = Color(0xFF22799B)
val CeruleanLowest = Color(0xFF134558)
val CeruleanLow = Color(0xFF1B5F7A)
val CeruleanHigh = Color(0xFF2687AE)
val CeruleanHighest = Color(0xFF2A96C0)
val CeruleanTint = Color(0xFF43ACD6)
val Madder = Color(0xFF9B2228)
val GoldenBrown = Color(0xFF9B6C22)

val DarkCerulean = Color(0xFF1D6785)
val DarkCeruleanLowest = Color(0xFF154A60)
val DarkCeruleanLow = Color(0xFF195973)
val DarkCeruleanHigh = Color(0xFF22789A)
val DarkCeruleanHighest = Color(0xFF2688AE)
val DarkCeruleanTint = Color(0xFF34A5D2)
val DarkMadder = Color(0xFF851D22)
val DarkGoldenBrown = Color(0xFF855D1D)

/** TODO */
fun invertColor(color: Color): Color {
  val r = 255 - (color.red * 255).toInt()
  val g = 255 - (color.green * 255).toInt()
  val b = 255 - (color.blue * 255).toInt()
  return Color(r, g, b, (color.alpha * 255).toInt())
}

/** A class containing the choice of color for an element. */
enum class ColorLevel {
  PRIMARY,
  SECONDARY,
  TERTIARY,
  ERROR
}

/**
 * Get the color for a content from a ColorLevel.
 *
 * @param colorLevel The chosen ColorLevel
 * @return The base color from the `colorLevel`.
 */
@Composable
fun getColor(colorLevel: ColorLevel): Color {
  return when (colorLevel) {
    ColorLevel.PRIMARY -> MaterialTheme.colorScheme.primary
    ColorLevel.SECONDARY -> MaterialTheme.colorScheme.secondary
    ColorLevel.TERTIARY -> MaterialTheme.colorScheme.tertiary
    ColorLevel.ERROR -> MaterialTheme.colorScheme.error
  }
}

/**
 * Get the color for a container from a ColorLevel.
 *
 * @param colorLevel The chosen ColorLevel
 * @return The container color from the `colorLevel`.
 */
@Composable
fun getOnColor(colorLevel: ColorLevel): Color {
  return when (colorLevel) {
    ColorLevel.PRIMARY -> MaterialTheme.colorScheme.onPrimary
    ColorLevel.SECONDARY -> MaterialTheme.colorScheme.onSecondary
    ColorLevel.TERTIARY -> MaterialTheme.colorScheme.onTertiary
    ColorLevel.ERROR -> MaterialTheme.colorScheme.onError
  }
}

/**
 * Get the color for a container from a ColorLevel.
 *
 * @param colorLevel The chosen ColorLevel
 * @return The container color from the `colorLevel`.
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
 * Get the color for a container from a ColorLevel.
 *
 * @param colorLevel The chosen ColorLevel
 * @return The container color from the `colorLevel`.
 */
@Composable
fun getOnContainerColor(colorLevel: ColorLevel): Color {
  return when (colorLevel) {
    ColorLevel.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    ColorLevel.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    ColorLevel.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    ColorLevel.ERROR -> MaterialTheme.colorScheme.onErrorContainer
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
      containerColor = getColor(colorLevel), contentColor = getOnColor(colorLevel))
}
