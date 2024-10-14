package com.github.se.cyrcle.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//val Black = Color(0xFF000000)
//val White = Color(0xFFFFFFFF)
val Cerulean = Color(0xFF22799B)
val LightBlue = Color(0xFFD1E7F8)
//val Cyan = Color(0xFF80DEF3)
//val NormalBlue = Color(0xFF00D0FF)
val DarkBlue = Color(0xFF00A1FF)
val Madder = Color(0xFF9B2228)
val GoldenBrown = Color(0xFF9B6C22)
val GrayLight = Color(0xFF9D9D9D)
val GrayDark = Color(0xFF555555)

/** A class containing the choice of color for an element. */
enum class ColorLevel {
  PRIMARY,
  SECONDARY,
  TERTIARY,
  ERROR
}

/**
 * Generate, based on https://developer.android.com/codelabs/jetpack-compose-theming#3, four
 * versions of the given accent color.
 *
 * @param accentColor The color from which the "palette" should be created
 */
class AccentColor(accentColor: Color) {
  val baseColor = accentColor.copy(alpha = 0.40f)
  val onColor = accentColor.copy(alpha = 1.00f)
  val containerColor = accentColor.copy(alpha = 0.90f)
  val onContainerColor = accentColor.copy(alpha = 0.10f)
}

/**
 * Generate, based on https://developer.android.com/codelabs/jetpack-compose-theming#3, different
 * versions of two neutral colors.
 *
 * @param neutralColor The color from which the "palette" should be created
 * @param neutralVariantColor A variant of the first.
 */
class NeutralColor(neutralColor: Color, neutralVariantColor: Color) {
  val backgroundColor = neutralColor.copy(alpha = 0.99f)
  val onBackgroundColor = neutralColor.copy(alpha = 0.10f)
  val surfaceColor = neutralColor.copy(alpha = 0.99f)
  val onSurfaceColor = neutralColor.copy(alpha = 0.10f)

  val surfaceVariantColor = neutralVariantColor.copy(alpha = 0.90f)
  val onSurfaceVariantColor = neutralVariantColor.copy(alpha = 0.30f)
  val outlineColor = neutralVariantColor.copy(alpha = 0.50f)
  val outlineVariantColor = neutralVariantColor.copy(alpha = 0.50f)
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
