package com.github.se.cyrcle.ui.theme.atoms

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.theme.defaultOnColor

/**
 * Create a themed text, with simplified arguments. TODO
 *
 * @param text The text to display
 * @param modifier Chained modifier. `.testTag` will be overwritten, use the `testTag` for this.
 * @param style The style of the text. Existing style can be found in `Type.kt` under typography.
 * @param color The color scheme of the object.
 * @param textAlign The text alignment.
 * @param testTag The test tag of the object.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = defaultOnColor(),
    textAlign: TextAlign = TextAlign.Center,
    testTag: String = "Text"
) {
  Text(
      text = text,
      modifier = modifier.testTag(testTag),
      style = style,
      textAlign = textAlign,
      color = color)
}

/**
 * A composable function that displays a text with a bullet point symbol in a horizontal row.
 *
 * This function is useful for creating lists where each item is preceded by a bullet point,
 * ensuring a consistent and visually appealing layout.
 *
 * @param text The text content to be displayed next to the bullet point.
 * @param testTag A tag used for testing purposes, allowing this composable to be identified in UI
 *   tests.
 */
@Composable
fun BulletPoint(text: String, testTag: String) {
  Row(modifier = Modifier.fillMaxWidth().testTag(testTag), verticalAlignment = Alignment.Top) {
    com.github.se.cyrcle.ui.theme.atoms.Text(
        text = "\u2022",
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(end = 8.dp))
    com.github.se.cyrcle.ui.theme.atoms.Text(
        text = text, style = MaterialTheme.typography.bodyLarge)
  }
}
