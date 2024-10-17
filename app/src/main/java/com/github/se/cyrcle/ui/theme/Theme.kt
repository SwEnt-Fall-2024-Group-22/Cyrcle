package com.github.se.cyrcle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val primaryColorLight = AccentColor(Cerulean)
private val secondaryColorLight = AccentColor(Madder)
private val tertiaryColorLight = AccentColor(GoldenBrown)
private val errorColorLight = AccentColor(Color.Red)
private val neutralColorsLight = NeutralColor(GrayLight, GrayDark)

private val primaryColorDark = AccentColor(Cerulean)
private val secondaryColorDark = AccentColor(Madder)
private val tertiaryColorDark = AccentColor(GoldenBrown)
private val errorColorDark = AccentColor(Color.Red)
private val neutralColorsDark = NeutralColor(GrayDark, GrayLight)

private val DarkColorScheme =
    darkColorScheme(
        primary = primaryColorLight.baseColor,
        onPrimary = primaryColorLight.onColor,
        primaryContainer = primaryColorLight.containerColor,
        onPrimaryContainer = primaryColorLight.onContainerColor,
        secondary = secondaryColorLight.baseColor,
        onSecondary = secondaryColorLight.onColor,
        secondaryContainer = secondaryColorLight.containerColor,
        onSecondaryContainer = secondaryColorLight.onContainerColor,
        tertiary = tertiaryColorLight.baseColor,
        onTertiary = tertiaryColorLight.onColor,
        tertiaryContainer = tertiaryColorLight.containerColor,
        onTertiaryContainer = tertiaryColorLight.onContainerColor,
        error = errorColorLight.baseColor,
        onError = errorColorLight.onColor,
        errorContainer = errorColorLight.containerColor,
        onErrorContainer = errorColorLight.onContainerColor,
        background = neutralColorsLight.backgroundColor,
        onBackground = neutralColorsLight.onBackgroundColor,
        surface = neutralColorsLight.surfaceColor,
        onSurface = neutralColorsLight.onSurfaceColor,
        surfaceVariant = neutralColorsLight.surfaceVariantColor,
        onSurfaceVariant = neutralColorsLight.onSurfaceVariantColor,
        // surfaceDim = ,
        // surfaceBright = ,
        // surfaceTint = ,
        outline = neutralColorsLight.outlineColor,
        outlineVariant = neutralColorsLight.outlineVariantColor,
        // inversePrimary = ,
        // inverseSurface = ,
        // inverseOnSurface = ,
        // surfaceContainer = ,
        // surfaceContainerLow = ,
        // surfaceContainerLowest = ,
        // surfaceContainerHigh = ,
        // surfaceContainerHighest = ,
        // scrim =
    )

private val LightColorScheme =
    lightColorScheme(
        primary = primaryColorDark.baseColor,
        onPrimary = primaryColorDark.onColor,
        primaryContainer = primaryColorDark.containerColor,
        onPrimaryContainer = primaryColorDark.onContainerColor,
        secondary = secondaryColorDark.baseColor,
        onSecondary = secondaryColorDark.onColor,
        secondaryContainer = secondaryColorDark.containerColor,
        onSecondaryContainer = secondaryColorDark.onContainerColor,
        tertiary = tertiaryColorDark.baseColor,
        onTertiary = tertiaryColorDark.onColor,
        tertiaryContainer = tertiaryColorDark.containerColor,
        onTertiaryContainer = tertiaryColorDark.onContainerColor,
        error = errorColorDark.baseColor,
        onError = errorColorDark.onColor,
        errorContainer = errorColorDark.containerColor,
        onErrorContainer = errorColorDark.onContainerColor,
        background = neutralColorsDark.backgroundColor,
        onBackground = neutralColorsDark.onBackgroundColor,
        surface = neutralColorsDark.surfaceColor,
        onSurface = neutralColorsDark.onSurfaceColor,
        surfaceVariant = neutralColorsDark.surfaceVariantColor,
        onSurfaceVariant = neutralColorsDark.onSurfaceVariantColor,
        // surfaceDim = ,
        // surfaceBright = ,
        // surfaceTint = ,
        outline = neutralColorsDark.outlineColor,
        outlineVariant = neutralColorsDark.outlineVariantColor,
        // inversePrimary = colorInverse(primaryColorDark.baseColor),
        // inverseSurface = colorInverse(neutralColorsDark.surfaceColor),
        // inverseOnSurface = colorInverse(neutralColorsDark.onSurfaceColor),
        // surfaceContainer = ,
        // surfaceContainerLow = ,
        // surfaceContainerLowest = ,
        // surfaceContainerHigh = ,
        // surfaceContainerHighest = ,
        // scrim =
    )

@Composable
fun CyrcleTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  val colorScheme =
      if (darkTheme) {
        DarkColorScheme
      } else {
        LightColorScheme
      }
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
