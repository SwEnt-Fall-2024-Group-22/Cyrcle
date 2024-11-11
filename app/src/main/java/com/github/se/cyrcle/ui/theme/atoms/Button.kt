package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.disabledColor
import com.github.se.cyrcle.ui.theme.getButtonColors
import com.github.se.cyrcle.ui.theme.getColor
import com.github.se.cyrcle.ui.theme.getIconButtonColors
import com.github.se.cyrcle.ui.theme.getOnColor
import com.github.se.cyrcle.ui.theme.onDisabledColor
import kotlin.math.floor
import kotlin.math.round

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
 * @param enabled If the button should be enabled (the onClick won't be triggered)
 * @param testTag The test tag of the object.
 */
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    enabled: Boolean = true,
    testTag: String = "Button"
) {
  Button(
      onClick = { if (enabled) onClick() },
      modifier = modifier.testTag(testTag),
      colors = getButtonColors(colorLevel),
      enabled = enabled) {
        Text(text, Modifier.testTag("${testTag}Text"))
      }
}

/**
 * Create a themed toggle button, with simplified arguments. This button will change the value of
 * the mutable boolean when clicked. Also, the color of the button will change based on the value of
 * the boolean (true = enabled, false = disabled).
 *
 * @param value The mutable boolean that this toggle represent and modify.
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param colorLevel The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun ToggleButton(
    text: String,
    value: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    testTag: String = "ToggleButton"
) {
  Button(
      onClick = { value.value = !value.value },
      modifier = modifier.testTag(testTag),
      colors =
          if (value.value) getButtonColors(colorLevel)
          else
              ButtonDefaults.buttonColors(
                  containerColor = disabledColor(), contentColor = onDisabledColor())) {
        Text(text, Modifier.testTag("${testTag}Text"))
      }
}

/**
 * Create a themed toggle button. This overload offers more flexibility.
 *
 * @param text The text to display.
 * @param value The boolean that this toggle represent.
 * @param onClick The function describing what should happen on click.
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param colorLevel The color scheme of the object.
 * @param testTag The test tag of the object.
 */
@Composable
fun ToggleButton(
    text: String,
    value: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    testTag: String = "ToggleButton"
) {
  Button(
      onClick = onClick,
      modifier = modifier.testTag(testTag),
      colors =
          if (value) getButtonColors(colorLevel)
          else
              ButtonDefaults.buttonColors(
                  containerColor = disabledColor(), contentColor = onDisabledColor())) {
        Text(text, Modifier.testTag("${testTag}Text"))
      }
}

@Composable
fun IconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorLevel: ColorLevel = ColorLevel.PRIMARY,
    testTag: String = "IconButton"
) {
  androidx.compose.material3.IconButton(
      modifier = modifier.testTag(testTag),
      onClick = onClick,
      enabled = enabled,
      colors = getIconButtonColors(colorLevel)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.testTag("${testTag}Icon"))
      }
}

@Composable
fun ScoreStars(
    score: Double,
    maxStars: Int = 5,
    starColor: Color = MaterialTheme.colorScheme.primary,
    scale: Float = 1.0f,
    text: String? = null,
    testTag: String = "StarButton"
) {
  val roundedScore = (round(score * 2) / 2).coerceIn(0.0, maxStars.toDouble())
  val fullStars = floor(roundedScore).toInt()
  val hasHalfStar = (roundedScore - fullStars) >= 0.5

  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(0.dp) // Smaller spacing between stars
      ) {
        for (i in 1..maxStars) {
          when {
            i <= fullStars ->
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Full Star",
                    tint = starColor,
                    modifier = Modifier.size((30 * scale).dp).testTag("${testTag}Icon$i"))
            i == fullStars + 1 && hasHalfStar ->
                Icon(
                    imageVector = Icons.Filled.StarHalf,
                    contentDescription = "Half Star",
                    tint = starColor,
                    modifier = Modifier.size((30 * scale).dp).testTag("${testTag}Icon$i"))
            else ->
                Icon(
                    imageVector = Icons.Filled.StarBorder,
                    contentDescription = "Empty Star",
                    tint = starColor,
                    modifier = Modifier.size((30 * scale).dp).testTag("${testTag}Icon$i"))
          }
        }

        text?.let {
          Spacer(modifier = Modifier.size((4 * scale).dp)) // Reduced space between stars and text
          Text(text = it, color = starColor, style = MaterialTheme.typography.bodyMedium)
        }
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
