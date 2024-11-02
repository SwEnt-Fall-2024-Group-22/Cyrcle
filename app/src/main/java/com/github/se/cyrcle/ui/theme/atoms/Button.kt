package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.disabledColor
import com.github.se.cyrcle.ui.theme.getButtonColors
import com.github.se.cyrcle.ui.theme.getColor
import com.github.se.cyrcle.ui.theme.getOnColor

/**
 * Create a themed small floating action button, with simplified arguments.
 *
 * @param icon The icon to display.
 * @param contentDescription Accessibility description
 * @param onClick The function describing what should happen on click.
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param colorLevel The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun SmallFloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    testTag: String = "SmallFab"
) {
  SmallFloatingActionButton(
      onClick = { onClick() },
      modifier = modifier.testTag(testTag),
      containerColor = getColor(colorLevel),
      contentColor = getOnColor(colorLevel)) {
        Icon(icon, contentDescription, Modifier.testTag("${testTag}Icon"))
      }
}

/**
 * Create a themed normal floating action button, with simplified arguments.
 *
 * @param icon The icon to display.
 * @param contentDescription Accessibility description
 * @param onClick The function describing what should happen on click.
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param colorLevel The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun FloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    testTag: String = "Fab"
) {
  FloatingActionButton(
      onClick = { onClick() },
      modifier = modifier.testTag(testTag),
      containerColor = getColor(colorLevel),
      contentColor = getOnColor(colorLevel)) {
        Icon(icon, contentDescription, Modifier.testTag("${testTag}Icon"))
      }
}

/**
 * Create a themed large floating action button, with simplified arguments.
 *
 * @param icon The icon to display.
 * @param contentDescription Accessibility description
 * @param onClick The function describing what should happen on click.
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param colorLevel The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun LargeFloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    testTag: String = "LargeFab"
) {
  LargeFloatingActionButton(
      onClick = { onClick() },
      modifier = modifier.testTag(testTag),
      containerColor = getColor(colorLevel),
      contentColor = getOnColor(colorLevel)) {
        Icon(icon, contentDescription, Modifier.testTag("${testTag}Icon"))
      }
}

/**
 * Create a themed extended floating action button, with simplified arguments.
 *
 * @param icon The icon to display.
 * @param contentDescription Accessibility description
 * @param onClick The function describing what should happen on click.
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param text The text display in the button.
 * @param colorLevel The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun ExtendedFloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    testTag: String = "ExtendedFab"
) {
  ExtendedFloatingActionButton(
      onClick = { onClick() },
      modifier = modifier.testTag(testTag),
      icon = { Icon(icon, contentDescription, Modifier.testTag("${testTag}Icon")) },
      text = { Text(text, Modifier.testTag("${testTag}Text")) },
      containerColor = getColor(colorLevel),
      contentColor = getOnColor(colorLevel))
}

/**
 * Create a themed button, with simplified arguments.
 *
 * @param text
 * @param onClick
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param colorLevel The color scheme of the object.
 * @param disabled If the button should be disabled (the onClick won't be triggered)
 * @param testTag The test tag of the object.
 */
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    disabled: MutableState<Boolean> = mutableStateOf(false),
    testTag: String = "Button"
) {
  Button(
      onClick = { if (!disabled.value) onClick() },
      modifier = modifier.testTag(testTag),
      colors = getButtonColors(colorLevel),
      enabled = !disabled.value) {
        Text(text, Modifier.testTag("${testTag}Text"))
      }
}

/**
 * Create a themed radio button, with simplified arguments. This should be only used inside a
 * `BooleanRadioButton`
 *
 * @param selected
 * @param onClick
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param colorLevel The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun RadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    testTag: String = "RadioButton"
) {
  RadioButton(
      selected = selected,
      onClick = onClick,
      modifier = modifier.testTag(testTag),
      colors =
          RadioButtonDefaults.colors(
              selectedColor = getColor(colorLevel), unselectedColor = disabledColor()))
}
