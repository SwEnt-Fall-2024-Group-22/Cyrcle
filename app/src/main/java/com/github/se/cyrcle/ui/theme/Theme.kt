package com.github.se.cyrcle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(primary = Cerulean, secondary = White, tertiary = Black)

private val LightColorScheme =
    lightColorScheme(primary = Cerulean, secondary = LightBlue, tertiary = Black)

/* Other default colors to override if/when needed
background = Color(0xFFFFFBFE),
surface = Color(0xFFFFFBFE),
onPrimary = Color.White,
onSecondary = Color.White,
onTertiary = Color.White,
onBackground = Color(0xFF1C1B1F),
onSurface = Color(0xFF1C1B1F),
*/

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
