package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.github.se.cyrcle.ui.theme.ColorScheme
import com.github.se.cyrcle.ui.theme.getButtonColors
import com.github.se.cyrcle.ui.theme.getContainerColor
import com.github.se.cyrcle.ui.theme.getContentColor

/**
 * Create a themed small floating action button, with simplified arguments.
 *
 * @param icon The icon to display.
 * @param contentDescription Accessibility description
 * @param onClick The function describing what should happen on click.
 * @param colorScheme The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun SmallFloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    colorScheme: ColorScheme = ColorScheme.PRIMARY,
    testTag: String = "SmallFab"
) {
  SmallFloatingActionButton(
      onClick = { onClick() },
      modifier = Modifier.testTag(testTag),
      containerColor = getContainerColor(colorScheme),
      contentColor = getContentColor(colorScheme)) {
        Icon(icon, contentDescription)
      }
}

/**
 * Create a themed normal floating action button, with simplified arguments.
 *
 * @param icon The icon to display.
 * @param contentDescription Accessibility description
 * @param onClick The function describing what should happen on click.
 * @param colorScheme The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun FloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    colorScheme: ColorScheme = ColorScheme.PRIMARY,
    testTag: String = "Fab"
) {
  FloatingActionButton(
      onClick = { onClick() },
      modifier = Modifier.testTag(testTag),
      containerColor = getContainerColor(colorScheme),
      contentColor = getContentColor(colorScheme)) {
        Icon(icon, contentDescription)
      }
}

/**
 * Create a themed large floating action button, with simplified arguments.
 *
 * @param icon The icon to display.
 * @param contentDescription Accessibility description
 * @param onClick The function describing what should happen on click.
 * @param colorScheme The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun LargeFloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    colorScheme: ColorScheme = ColorScheme.PRIMARY,
    testTag: String = "LargeFab"
) {
  LargeFloatingActionButton(
      onClick = { onClick() },
      modifier = Modifier.testTag(testTag),
      containerColor = getContainerColor(colorScheme),
      contentColor = getContentColor(colorScheme)) {
        Icon(icon, contentDescription)
      }
}

/**
 * Create a themed extended floating action button, with simplified arguments.
 *
 * @param icon The icon to display.
 * @param contentDescription Accessibility description
 * @param onClick The function describing what should happen on click.
 * @param text The text display in the button.
 * @param colorScheme The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun ExtendedFloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    text: String,
    colorScheme: ColorScheme = ColorScheme.PRIMARY,
    testTag: String = "ExtendedFab"
) {
  ExtendedFloatingActionButton(
      onClick = { onClick() },
      modifier = Modifier.testTag(testTag),
      icon = { Icon(icon, contentDescription) },
      text = { Text(text) },
      containerColor = getContainerColor(colorScheme),
      contentColor = getContentColor(colorScheme))
}

/**
 * Create a themed button, with simplified arguments.
 *
 * @param text
 * @param onClick
 * @param colorScheme The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    colorScheme: ColorScheme = ColorScheme.PRIMARY,
    testTag: String = "PrimaryButton"
) {
  Button(
      onClick = { onClick() },
      modifier = Modifier.testTag(testTag),
      colors = getButtonColors(colorScheme)) {
        Text(text)
      }
}
