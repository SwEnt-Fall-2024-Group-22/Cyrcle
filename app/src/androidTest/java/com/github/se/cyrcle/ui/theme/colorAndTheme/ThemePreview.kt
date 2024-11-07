package com.github.se.cyrcle.ui.theme.colorAndTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.Black
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.LightColorScheme
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.ExtendedFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.FloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.LargeFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.molecules.BooleanRadioButton
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar

@Preview(showBackground = true)
@Composable
fun ColorSchemePreviewPreview() {
  // CHANGE THIS TO PREVIEW ANOTHER THEME
  val cs = LightColorScheme // { DarkColorScheme, LightColorScheme }

  val mutableBoolToFill = remember { mutableStateOf(true) }
  MaterialTheme(colorScheme = cs) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
          item {
            // ========== COLORS ==========
            Text("Colors", color = cs.onBackground)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                  ColoredSquare(cs.primary, cs.onPrimary, "(on) primary")
                  ColoredSquare(cs.secondary, cs.onSecondary, "(on) secondary")
                  ColoredSquare(cs.tertiary, cs.onTertiary, "(on) tertiary")
                  ColoredSquare(cs.error, cs.onError, "(on) error")
                }
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                  ColoredSquare(
                      cs.primaryContainer, cs.onPrimaryContainer, "(on) primary container")
                  ColoredSquare(
                      cs.secondaryContainer, cs.onSecondaryContainer, "(on) secondary container")
                  ColoredSquare(
                      cs.tertiaryContainer, cs.onTertiaryContainer, "(on) tertiary container")
                  ColoredSquare(cs.errorContainer, cs.onErrorContainer, "(on) error container")
                }

            // ========== SURFACES ==========
            Spacer(Modifier.height(20.dp))
            Text("Surface (with onSurface)", color = cs.onBackground)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                  ColoredSquare(cs.surfaceDim, cs.onSurface, "dim", 80.dp)
                  ColoredSquare(cs.surface, cs.onSurface, "surface", 80.dp)
                  ColoredSquare(cs.surfaceBright, cs.onSurface, "bright", 80.dp)
                  ColoredSquare(cs.surfaceTint, cs.onSurface, "tint", 80.dp)
                  ColoredSquare(cs.surfaceVariant, cs.onSurfaceVariant, "(on) variant", 80.dp)
                }
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                  ColoredSquare(cs.surfaceContainerLowest, cs.onSurface, "cont. lowest", 80.dp)
                  ColoredSquare(cs.surfaceContainerLow, cs.onSurface, "cont. low", 80.dp)
                  ColoredSquare(cs.surfaceContainer, cs.onSurface, "cont. (def.)", 80.dp)
                  ColoredSquare(cs.surfaceContainerHigh, cs.onSurface, "cont. high", 80.dp)
                  ColoredSquare(cs.surfaceContainerHighest, cs.onSurface, "cont. highest", 80.dp)
                }

            // ========== ATOM AND MOLECULES ==========
            Spacer(Modifier.height(20.dp))
            Text("Some Atom and Molecule", color = cs.onBackground)

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                  SmallFloatingActionButton(
                      Icons.Filled.Add, "", onClick = {}, colorLevel = ColorLevel.PRIMARY)
                  FloatingActionButton(
                      Icons.Filled.Add, "", onClick = {}, colorLevel = ColorLevel.SECONDARY)
                  LargeFloatingActionButton(
                      Icons.Filled.Add, "", onClick = {}, colorLevel = ColorLevel.TERTIARY)
                  ExtendedFloatingActionButton(
                      Icons.Filled.Add,
                      "",
                      onClick = {},
                      colorLevel = ColorLevel.ERROR,
                      text = "Extended")
                }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                  Button("Button", onClick = {})
                  BooleanRadioButton("Do you ?", mutableBoolToFill)
                }

            TopAppBar(NavigationActions(rememberNavController()), "TopAppBar")
            BottomNavigationBar(
                NavigationActions(rememberNavController()),
                onTabSelect = {},
                selectedItem = Route.MAP)
          }
        }
  }
}

@Composable
fun ColoredSquare(
    backgroundColor: Color,
    textColor: Color = Black,
    text: String = "",
    size: Dp = 100.dp
) {
  Box(
      modifier =
          Modifier.size(size)
              .background(backgroundColor)
              // .border(2.dp, Color(alpha = 0.5f, red = 0.5f, green = 0.5f, blue = 0.5f))
              .padding(8.dp),
      contentAlignment = Alignment.Center) {
        Text(text = text, textAlign = TextAlign.Center, color = textColor)
      }
}
