package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.disabledColor
import com.github.se.cyrcle.ui.theme.getButtonColors
import com.github.se.cyrcle.ui.theme.getColor
import com.github.se.cyrcle.ui.theme.getIconButtonColors
import com.github.se.cyrcle.ui.theme.getInvertedIconButtonColors
import com.github.se.cyrcle.ui.theme.getOnColor
import com.github.se.cyrcle.ui.theme.googleSignInButtonStyle
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
        Icon(
            icon,
            contentDescription,
            Modifier.testTag("${testTag}Icon"),
            tint = getOnColor(colorLevel))
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
        Icon(
            icon,
            contentDescription,
            Modifier.testTag("${testTag}Icon"),
            tint = getOnColor(colorLevel))
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
        Icon(
            icon,
            contentDescription,
            Modifier.testTag("${testTag}Icon"),
            tint = getOnColor(colorLevel))
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
      text = { Text(text, Modifier.testTag("${testTag}Text"), color = getOnColor(colorLevel)) },
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
        Text(text, Modifier.testTag("${testTag}Text"), color = getOnColor(colorLevel))
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
          else ButtonDefaults.buttonColors(containerColor = disabledColor())) {
        Text(
            text,
            Modifier.testTag("${testTag}Text"),
            color = if (value.value) getOnColor(colorLevel) else getColor(colorLevel))
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
          else ButtonDefaults.buttonColors(containerColor = disabledColor())) {
        Text(
            text,
            Modifier.testTag("${testTag}Text"),
            color = if (value) getOnColor(colorLevel) else getColor(colorLevel))
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
    testTag: String = "IconButton",
    inverted: Boolean = false
) {
  androidx.compose.material3.IconButton(
      modifier = modifier.testTag(testTag),
      onClick = onClick,
      enabled = enabled,
      colors =
          if (inverted) getInvertedIconButtonColors(colorLevel)
          else getIconButtonColors(colorLevel)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.testTag("${testTag}Icon"),
            tint = if (inverted) getColor(colorLevel) else getOnColor(colorLevel))
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
          Spacer(modifier = Modifier.width((5 * scale).dp)) // Reduced space between stars and text
          Text(
              text = it,
              color = starColor,
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = round(scale * 18).sp))
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

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
  Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.White),
      shape = RoundedCornerShape(50),
      border = BorderStroke(1.dp, Color.LightGray),
      modifier =
          Modifier.height(48.dp) // Adjust height as needed
              .testTag("AuthenticateButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {

              // Google Logo
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier = Modifier.size(30.dp).padding(end = 8.dp))

              // Text on Sign-In button
              Text(
                  text = stringResource(R.string.sign_in_google_button),
                  color = Color.Black,
                  style = googleSignInButtonStyle)
            }
      }
}

/**
 * Create a themed options menu, with simplified arguments.
 *
 * @param options A map of option names and their respective functions.
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param testTag The test tag of the object.
 *
 * When instantiated, it is recommended to use the modifier argument to align the content of the box
 * to the top right corner as:
 * ```
 * OptionsMenu(
 *    options = options,
 *    modifier = Modifier.align(Alignment.TopEnd).[...], // Other modifiers
 *    testTag = "OptionsMenu"
 *    )
 *    ```
 */
@Composable
fun OptionsMenu(
    options: Map<String, () -> Unit>,
    modifier: Modifier = Modifier,
    testTag: String = "OptionsMenu"
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    androidx.compose.material3.IconButton(
        onClick = { menuExpanded = true }, modifier = Modifier.testTag("${testTag}Button")) {
          Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "More options",
              tint = MaterialTheme.colorScheme.onSurface)
        }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
        offset = DpOffset((-40).dp, (-40).dp),
        modifier = Modifier.testTag("${testTag}Menu")) {
          options.forEach { (optionName, onClick) ->
            DropdownMenuItem(
                text = {
                  Text(
                      optionName,
                      style =
                          MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                },
                onClick = {
                  onClick()
                  menuExpanded = false
                },
                modifier = Modifier.testTag("${testTag}${optionName.replace(" ", "")}Item"))
          }
        }
  }
}
